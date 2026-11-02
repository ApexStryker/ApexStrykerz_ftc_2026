package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.util.Debug;

/**
 * The 4-blade windmill that feeds one ball into the launcher per quarter turn.
 *
 * In continuous mode the servo cannot report where it is, so a step ends one of two ways:
 *  - with the optional magnetic index switch ("windmill_index"): when the next blade's magnet
 *    arrives, which is exact;
 *  - without it: after STEP_MS, which is approximate and drifts as the battery drains.
 * Between steps it is stopped, and a stopped blade holds the next ball back.
 */
public class Windmill {

    public final boolean present;
    public final boolean hasIndexSwitch;
    private final ContinuousServo servo;
    private final DigitalChannel indexSwitch;
    private final ElapsedTime stepTimer = new ElapsedTime();

    private boolean stepping, reversing, leftMagnet, lastStepTimedOut;
    private int stepsDone;
    /** Feed speed now: starts at FEED_SPEED, the operator's D-pad right / left changes it. */
    private double feedSpeed = Constants.Windmill.FEED_SPEED;

    public Windmill(HardwareMap hw) {
        servo = new ContinuousServo(hw, Constants.Config.WINDMILL);
        present = servo.present;
        indexSwitch = hw.tryGet(DigitalChannel.class, Constants.Config.WINDMILL_INDEX);
        hasIndexSwitch = indexSwitch != null;
        if (hasIndexSwitch) indexSwitch.setMode(DigitalChannel.Mode.INPUT);
    }

    /** Start one quarter-turn feed step. Returns false if one is already running. */
    public boolean requestStep() {
        if (!present || stepping) return false;
        stepping = true;
        reversing = false;
        // If a magnet sits at the switch now, it must pass before the next one counts.
        leftMagnet = !magnetAtSwitch();
        lastStepTimedOut = false;
        stepTimer.reset();
        servo.setPower(feedSpeed);
        return true;
    }

    /** Call once per loop. */
    public void update() {
        if (!stepping) return;
        if (hasIndexSwitch) {
            boolean magnet = magnetAtSwitch();
            if (!magnet) leftMagnet = true;
            if (leftMagnet && magnet) { finishStep(false); return; }
            if (stepTimer.milliseconds() >= Constants.Windmill.STEP_TIMEOUT_MS) finishStep(true);
        } else if (stepTimer.milliseconds() >= stepMs()) {
            finishStep(false);
        }
    }

    private void finishStep(boolean timedOut) {
        if (timedOut) Debug.warn("windmill: step timed out, no index magnet seen in %.0f ms", Constants.Windmill.STEP_TIMEOUT_MS);
        stepping = false;
        lastStepTimedOut = timedOut;
        stepsDone++;
        servo.stop();
    }

    /** Stop and hold balls back. An unfinished step is allowed to complete first. */
    public void hold() {
        reversing = false;
        if (!stepping) servo.stop();
    }

    /** Turn backward continuously, for clearing a jam. Cancels any step. */
    /** Spin continuously in the feed direction until hold() (used by Ball Path Test). */
    public void run() { drive(1.0); }

    /**
     * Spin continuously until hold(): fraction 0..1 of FEED_SPEED toward the launcher, or 0..-1
     * of REVERSE_SPEED backward (used by Ball Path Test's right stick).
     */
    public void drive(double fraction) {
        stepping = false;
        reversing = fraction < 0;
        servo.setPower(fraction >= 0 ? feedSpeed * fraction
                : Constants.Windmill.REVERSE_SPEED * -fraction);
    }

    public void reverse() {
        stepping = false;
        reversing = true;
        servo.setPower(Constants.Windmill.REVERSE_SPEED);
    }

    /**
     * Set how fast it feeds, 0..1 (the feed direction is kept from FEED_SPEED), clipped to
     * MIN_SPEED..MAX_SPEED. Used from the next step or run() / drive() call.
     */
    public void setFeedSpeed(double speed) {
        speed = Math.max(Constants.Windmill.MIN_SPEED, Math.min(Constants.Windmill.MAX_SPEED, speed));
        feedSpeed = Math.copySign(speed, Constants.Windmill.FEED_SPEED);
    }

    /** How fast it feeds now, 0..1. */
    public double getFeedSpeed() { return Math.abs(feedSpeed); }

    /** Without the index switch: a quarter turn takes longer when slower, so scale STEP_MS. */
    private double stepMs() {
        return Constants.Windmill.STEP_MS * Math.abs(Constants.Windmill.FEED_SPEED) / Math.abs(feedSpeed);
    }

    private boolean magnetAtSwitch() {
        if (!hasIndexSwitch) return false;
        boolean raw = indexSwitch.getState();
        return Constants.Windmill.INDEX_ACTIVE_LOW != raw;
    }

    public boolean isStepping() { return stepping; }
    public boolean isReversing() { return reversing; }
    public boolean lastStepTimedOut() { return lastStepTimedOut; }
    public int getStepsDone() { return stepsDone; }
    public boolean configuredAsPositional() { return servo.configuredAsPositional(); }
}
