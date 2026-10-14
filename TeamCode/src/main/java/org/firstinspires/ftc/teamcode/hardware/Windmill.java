package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Constants;

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
        servo.setPower(Constants.Windmill.FEED_SPEED);
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
        } else if (stepTimer.milliseconds() >= Constants.Windmill.STEP_MS) {
            finishStep(false);
        }
    }

    private void finishStep(boolean timedOut) {
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
    public void reverse() {
        stepping = false;
        reversing = true;
        servo.setPower(Constants.Windmill.REVERSE_SPEED);
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
