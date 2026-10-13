package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Constants;

/**
 * The launcher wheel, run at a set SPEED (encoder ticks per second), not a set power, so shots
 * stay consistent as the battery drains.
 *
 * atSpeed() is the readiness gate: nothing may be fed into the wheel unless it is true.
 * Shot detection: after armShot(), a dip of SHOT_DIP_TPS below target means a ball went through.
 */
public class Launcher {

    public final boolean present;
    private final DcMotorEx motor;
    private final ElapsedTime inTolerance = new ElapsedTime();
    private boolean wasInTolerance;
    private boolean enabled;
    private double targetTps;
    private boolean armed, shotSeen;
    private double velocity;
    private int presetIndex;

    public Launcher(HardwareMap hw) {
        motor = hw.tryGet(DcMotorEx.class, Constants.Config.LAUNCHER);
        present = motor != null;
        if (present) {
            motor.setDirection(Constants.Launcher.DIRECTION);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);   // flywheel coasts down
            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            if (Constants.Launcher.USE_CUSTOM_PIDF) {
                setPidf(Constants.Launcher.P, Constants.Launcher.I, Constants.Launcher.D, Constants.Launcher.F);
            }
            motor.setVelocity(0);
        }
        selectPreset(0);
    }

    /** Call once per loop. */
    public void update() {
        velocity = present ? Math.abs(motor.getVelocity()) : 0;
        boolean inTol = enabled && Math.abs(velocity - targetTps) <= Constants.Launcher.READY_TOLERANCE_TPS;
        if (inTol && !wasInTolerance) inTolerance.reset();
        wasInTolerance = inTol;
        if (armed && enabled && velocity < targetTps - Constants.Launcher.SHOT_DIP_TPS) shotSeen = true;
    }

    public void spinUp() {
        enabled = true;
        if (present) motor.setVelocity(targetTps);
    }

    public void stop() {
        enabled = false;
        armed = false;
        if (present) motor.setVelocity(0);
    }

    public void setTargetTps(double tps) {
        targetTps = Math.max(0, tps);
        if (enabled && present) motor.setVelocity(targetTps);
    }

    public void selectPreset(int index) {
        presetIndex = Math.max(0, Math.min(Constants.Launcher.PRESETS_TPS.length - 1, index));
        setTargetTps(Constants.Launcher.PRESETS_TPS[presetIndex]);
    }

    public int getPresetIndex() { return presetIndex; }
    public String getPresetName() { return Constants.Launcher.PRESET_NAMES[presetIndex]; }

    /** Speed for a distance, from the measured table; falls back to the current preset if empty. */
    public double speedForDistance(double inches) {
        double[] d = Constants.Launcher.TABLE_DISTANCE_IN, s = Constants.Launcher.TABLE_SPEED_TPS;
        if (d.length == 0 || d.length != s.length) return Constants.Launcher.PRESETS_TPS[presetIndex];
        if (inches <= d[0]) return s[0];
        for (int i = 1; i < d.length; i++) {
            if (inches <= d[i]) {
                double t = (inches - d[i - 1]) / (d[i] - d[i - 1]);
                return s[i - 1] + t * (s[i] - s[i - 1]);
            }
        }
        return s[s.length - 1];
    }

    /** True once the wheel has held target speed for READY_HOLD_MS. */
    public boolean atSpeed() {
        return enabled && wasInTolerance && inTolerance.milliseconds() >= Constants.Launcher.READY_HOLD_MS;
    }

    /** Start watching for the speed dip of a shot. */
    public void armShot() { armed = true; shotSeen = false; }

    /** True once, after a shot was detected since armShot(). */
    public boolean takeShot() {
        if (!shotSeen) return false;
        shotSeen = false;
        armed = false;
        return true;
    }

    public void disarmShot() { armed = false; shotSeen = false; }

    public void setPidf(double p, double i, double d, double f) {
        if (present) motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(p, i, d, f));
    }

    public PIDFCoefficients getPidf() {
        return present ? motor.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER) : new PIDFCoefficients();
    }

    public boolean isEnabled() { return enabled; }
    public double getTargetTps() { return targetTps; }
    public double getVelocityTps() { return velocity; }
    public double getRpm() { return velocity * 60.0 / Constants.Launcher.TICKS_PER_REV; }
}
