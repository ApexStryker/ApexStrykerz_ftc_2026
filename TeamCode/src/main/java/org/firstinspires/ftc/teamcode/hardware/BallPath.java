package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.util.Debug;

/**
 * Coordinates intake, windmill, launcher and flower wheels so they never fight each other.
 *
 * The operator (or autonomous) only states INTENT each loop: collect, fire, flower, clear,
 * launcher on. BallPath turns that into motor and servo commands.
 *
 *   Mode     Intake      Windmill            Launcher          Ends when
 *   IDLE     off         holds               as requested      a request
 *   COLLECT  in          holds (queue)       as requested      released, or 4 balls (stops itself)
 *   FIRE     gentle in   one step per ball   spun up           released; feeds ONLY when at speed
 *   FLOWER   in          holds               as requested      released, or 4 balls
 *   CLEAR    out         reverse             off               released
 *   REJECT   out         holds               as requested      after REJECT_MS (opponent NECTAR)
 *
 * The rule that matters most: the windmill never feeds a ball unless launcher.atSpeed().
 *
 * Ball counting needs the intake color sensor. Without it the count is unknown: the 4-ball
 * limit is not enforced automatically and FIRE keeps feeding while held.
 */
public class BallPath {

    public enum Mode { IDLE, COLLECT, FIRE, FLOWER, CLEAR, REJECT }

    private enum FireStep { WAIT_SPEED, FEEDING, WAIT_SHOT, WAIT_RECOVER }

    private final Intake intake;
    private final Windmill windmill;
    private final Launcher launcher;
    private final FlowerWheels flower;
    private final BallSensor sensor;
    private Constants.Alliance alliance = Constants.DEFAULT_ALLIANCE;

    private boolean collectReq, fireReq, flowerReq, clearReq, launcherReq;
    private Mode mode = Mode.IDLE;
    private FireStep fireStep = FireStep.WAIT_SPEED;
    private final ElapsedTime fireTimer = new ElapsedTime();
    private final ElapsedTime rejectTimer = new ElapsedTime();
    private boolean rejecting, shotThisFeed;

    private int ballCount, shotsFired, emptyFeeds;
    private boolean lastReady, lastFull, readyEvent, fullEvent, rejectEvent;

    public BallPath(Intake intake, Windmill windmill, Launcher launcher, FlowerWheels flower, BallSensor sensor) {
        this.intake = intake;
        this.windmill = windmill;
        this.launcher = launcher;
        this.flower = flower;
        this.sensor = sensor;
    }

    // ---- Intent, set every loop -------------------------------------------------------------
    public void setCollect(boolean on) { collectReq = on; }
    public void setFire(boolean on) { fireReq = on; }
    public void setFlower(boolean on) { flowerReq = on; }
    public void setClear(boolean on) { clearReq = on; }
    public void setLauncher(boolean on) { launcherReq = on; }
    public void setAlliance(Constants.Alliance a) { alliance = a; }
    /** Autonomous starts with known preloads. */
    public void setBallCount(int n) { ballCount = Math.max(0, Math.min(Constants.BallSensor.MAX_BALLS, n)); }

    /** Call once per loop, after setting intent. */
    public void update() {
        sensor.update();
        launcher.update();
        windmill.update();
        countBalls();

        if (rejecting && rejectTimer.milliseconds() >= Constants.BallSensor.REJECT_MS) rejecting = false;

        Mode next;
        if (clearReq) next = Mode.CLEAR;
        else if (rejecting) next = Mode.REJECT;
        else if (fireReq) next = Mode.FIRE;
        else if (flowerReq) next = Mode.FLOWER;
        else if (collectReq) next = Mode.COLLECT;
        else next = Mode.IDLE;

        if (next != mode) {
            Debug.log("BallPath: %s -> %s", mode, next);
            if (mode == Mode.FIRE) { launcher.disarmShot(); fireStep = FireStep.WAIT_SPEED; }
            mode = next;
        }

        // Launcher: spun up when asked for, always while firing, never while clearing.
        boolean wantLauncher = (launcherReq || mode == Mode.FIRE) && mode != Mode.CLEAR;
        if (wantLauncher && !launcher.isEnabled()) launcher.spinUp();
        if (!wantLauncher && launcher.isEnabled()) launcher.stop();

        switch (mode) {
            case IDLE:
                intake.stop(); windmill.hold(); flower.stop();
                break;
            case COLLECT:
                flower.stop(); windmill.hold();
                if (isFull()) intake.stop(); else intake.in();
                break;
            case FLOWER:
                flower.pullIn(); windmill.hold();
                if (isFull()) intake.stop(); else intake.in();
                break;
            case FIRE:
                flower.stop(); intake.feed();
                runFire();
                break;
            case CLEAR:
                flower.stop(); intake.out(); windmill.reverse();
                break;
            case REJECT:
                flower.stop(); windmill.hold(); intake.out();
                break;
        }

        boolean ready = launcher.atSpeed();
        if (ready && !lastReady) readyEvent = true;
        lastReady = ready;
        boolean full = isFull();
        if (full && !lastFull) { fullEvent = true; Debug.log("BallPath: full, %d balls", ballCount); }
        lastFull = full;
    }

    private void countBalls() {
        if (!sensor.present || !sensor.ballArrived()) return;
        if (BallSensor.isOpponentNectar(sensor.getBall(), alliance)) {
            rejecting = true;          // rule G408: spit it straight back out, do not count it
            rejectEvent = true;
            Debug.log("BallPath: rejecting opponent %s", sensor.getBall());
            rejectTimer.reset();
        } else if (intake.getPower() > 0) {
            ballCount = Math.min(Constants.BallSensor.MAX_BALLS + 1, ballCount + 1);
            Debug.log("BallPath: ball in (%s), count %d", sensor.getBall(), ballCount);
        } else if (intake.getPower() < 0) {
            ballCount = Math.max(0, ballCount - 1);
        }
    }

    private void runFire() {
        if (launcher.takeShot()) shotThisFeed = true;
        switch (fireStep) {
            case WAIT_SPEED:
                windmill.hold();
                if (launcher.atSpeed() && hasBallsToFire()) {
                    shotThisFeed = false;
                    launcher.armShot();
                    windmill.requestStep();
                    fireStep = FireStep.FEEDING;
                    fireTimer.reset();
                }
                break;
            case FEEDING:
                if (!windmill.isStepping()) {
                    fireTimer.reset();
                    fireStep = shotThisFeed ? afterShot() : FireStep.WAIT_SHOT;
                }
                break;
            case WAIT_SHOT:
                if (shotThisFeed) {
                    fireStep = afterShot();
                } else if (fireTimer.milliseconds() >= Constants.Launcher.SHOT_TIMEOUT_MS) {
                    launcher.disarmShot();   // nothing went through: empty pocket
                    emptyFeeds++;
                    Debug.log("BallPath: empty feed, no shot within %.0f ms (empty pockets %d)",
                            Constants.Launcher.SHOT_TIMEOUT_MS, emptyFeeds);
                    fireStep = FireStep.WAIT_RECOVER;
                }
                break;
            case WAIT_RECOVER:
                if (launcher.atSpeed()) fireStep = FireStep.WAIT_SPEED;
                break;
        }
    }

    private FireStep afterShot() {
        shotThisFeed = false;
        shotsFired++;
        Debug.log("BallPath: shot %d, launcher dipped to %.0f tps", shotsFired, launcher.getVelocityTps());
        if (sensor.present) ballCount = Math.max(0, ballCount - 1);
        return FireStep.WAIT_RECOVER;
    }

    private boolean hasBallsToFire() { return !sensor.present || ballCount > 0; }

    /** Rule G407. Only knowable with the color sensor. */
    public boolean isFull() { return sensor.present && ballCount >= Constants.BallSensor.MAX_BALLS; }

    public void stopAll() {
        collectReq = fireReq = flowerReq = clearReq = launcherReq = false;
        rejecting = false;
        intake.stop(); windmill.hold(); flower.stop(); launcher.stop();
        mode = Mode.IDLE;
        fireStep = FireStep.WAIT_SPEED;
    }

    // ---- State for telemetry, LEDs and rumble ----------------------------------------------
    public Mode getMode() { return mode; }
    public String getFireStep() { return mode == Mode.FIRE ? fireStep.name() : "-"; }
    public boolean countKnown() { return sensor.present; }
    public int getBallCount() { return ballCount; }
    public int getShotsFired() { return shotsFired; }
    public int getEmptyFeeds() { return emptyFeeds; }
    public boolean launcherReady() { return launcher.atSpeed(); }

    /** True once each time the launcher reaches speed. */
    public boolean takeReadyEvent() { boolean e = readyEvent; readyEvent = false; return e; }
    /** True once each time the robot becomes full (4 balls). */
    public boolean takeFullEvent() { boolean e = fullEvent; fullEvent = false; return e; }
    /** True once each time opponent NECTAR is rejected. */
    public boolean takeRejectEvent() { boolean e = rejectEvent; rejectEvent = false; return e; }
}
