package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Constants;

import java.util.List;

/**
 * The launcher wheel, run at a set SPEED (encoder ticks per second), not a set power, so shots
 * stay consistent as the battery drains.
 *
 * Two ways to hold the speed (Constants.Launcher.OWN_SPEED_LOOP):
 *  - our own loop (true), computed here every update():
 *      power = (KS + KV x target) x (NOMINAL_VOLTS / battery)     base power, battery-corrected
 *            + KP x error + KI x (sum of error over time)           correction toward the target
 *      and full power while the wheel is more than BOOST_BELOW_TPS slow (right after a ball),
 *      so it gets back up to speed as fast as it can;
 *  - the hub's built-in velocity PIDF (false), as before 2026-11-04.
 * Our loop only runs when update() is called every loop (robot.update() / updateManual() do).
 *
 * atSpeed() is the readiness gate: nothing may be fed into the wheel unless it is true.
 * Shot detection: after armShot(), a dip of SHOT_DIP_TPS below target means a ball went through.
 */
public class Launcher {

    public final boolean present;
    private final DcMotorEx motor;
    private final List<VoltageSensor> batteries;
    private final boolean ownLoop = Constants.Launcher.OWN_SPEED_LOOP;
    private final ElapsedTime inTolerance = new ElapsedTime();
    private final ElapsedTime loopTimer = new ElapsedTime();
    private final ElapsedTime voltsTimer = new ElapsedTime();
    private boolean wasInTolerance;
    private boolean enabled;
    private double targetTps;
    private boolean armed, shotSeen;
    private double velocity;
    private int presetIndex;

    // Our own speed loop: gains (start from Constants, Launcher Tuner can change them live).
    private double kv = Constants.Launcher.KV, ks = Constants.Launcher.KS;
    private double kp = Constants.Launcher.KP, ki = Constants.Launcher.KI;
    private double integral, power, volts = Constants.Launcher.NOMINAL_VOLTS;
    private boolean boosting;
    /** Fixed power for calibration (Launcher Tuner); NaN = normal speed control. */
    private double openLoopPower = Double.NaN;

    public Launcher(HardwareMap hw) {
        motor = hw.tryGet(DcMotorEx.class, Constants.Config.LAUNCHER);
        present = motor != null;
        batteries = hw.getAll(VoltageSensor.class);
        if (present) {
            motor.setDirection(Constants.Launcher.DIRECTION);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);   // flywheel coasts down
            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            if (ownLoop) {
                // We set the power ourselves; the encoder still counts and reports velocity.
                motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                motor.setPower(0);
            } else {
                motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                if (Constants.Launcher.USE_CUSTOM_PIDF) {
                    setPidf(Constants.Launcher.P, Constants.Launcher.I, Constants.Launcher.D, Constants.Launcher.F);
                }
                motor.setVelocity(0);
            }
        }
        readVolts();
        selectPreset(0);
    }

    /** Call once per loop. */
    public void update() {
        velocity = present ? Math.abs(motor.getVelocity()) : 0;
        boolean inTol = enabled && Math.abs(velocity - targetTps) <= Constants.Launcher.READY_TOLERANCE_TPS;
        if (inTol && !wasInTolerance) inTolerance.reset();
        wasInTolerance = inTol;
        if (armed && enabled && velocity < targetTps - Constants.Launcher.SHOT_DIP_TPS) shotSeen = true;

        double dt = Math.min(loopTimer.seconds(), 0.1);   // a long pause must not wind up the integral
        loopTimer.reset();
        if (ownLoop) runOwnLoop(dt);
    }

    private void runOwnLoop(double dt) {
        if (voltsTimer.milliseconds() >= Constants.Launcher.VOLTS_READ_MS) readVolts();
        if (!Double.isNaN(openLoopPower)) {
            power = openLoopPower;
        } else if (!enabled || targetTps <= 0) {
            power = 0;
            integral = 0;
            boosting = false;
        } else {
            double error = targetTps - velocity;
            boosting = error > Constants.Launcher.BOOST_BELOW_TPS;
            if (boosting) {
                power = 1.0;                       // well below speed: full power to recover fast
            } else {
                integral = Range.clip(integral + error * dt,
                        -Constants.Launcher.I_LIMIT / Math.max(ki, 1e-9), Constants.Launcher.I_LIMIT / Math.max(ki, 1e-9));
                power = feedForward(targetTps) + kp * error + ki * integral;
            }
        }
        power = Range.clip(power, 0, 1);
        if (present) motor.setPower(power);
    }

    /** Base power for a speed, corrected for the battery: what holds that speed with no error. */
    private double feedForward(double tps) {
        return (ks + kv * tps) * Constants.Launcher.NOMINAL_VOLTS / volts;
    }

    private void readVolts() {
        voltsTimer.reset();
        double min = 0;
        for (VoltageSensor v : batteries) {
            double reading = v.getVoltage();
            if (reading > 0 && (min == 0 || reading < min)) min = reading;
        }
        if (min > 6) volts = min;      // ignore a missing or nonsense reading
    }

    public void spinUp() {
        enabled = true;
        if (!present) return;
        if (ownLoop) {
            integral = 0;
            loopTimer.reset();
            runOwnLoop(0);                 // start now, even before the next update()
        } else {
            motor.setVelocity(targetTps);
        }
    }

    public void stop() {
        enabled = false;
        armed = false;
        openLoopPower = Double.NaN;
        if (!present) return;
        if (ownLoop) { power = 0; integral = 0; motor.setPower(0); }
        else motor.setVelocity(0);
    }

    public void setTargetTps(double tps) {
        targetTps = Math.max(0, tps);
        if (enabled && present && !ownLoop) motor.setVelocity(targetTps);
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

    // ---- Tuning (Launcher Tuner) ---------------------------------------------------------------

    /** True when our own speed loop is in use (Constants.Launcher.OWN_SPEED_LOOP). */
    public boolean usesOwnLoop() { return ownLoop; }

    /** Our loop's gains, live: KV, KS, KP, KI. */
    public void setGains(double kv, double ks, double kp, double ki) {
        this.kv = kv; this.ks = ks; this.kp = kp; this.ki = ki;
    }
    public double[] getGains() { return new double[] { kv, ks, kp, ki }; }

    /** Calibration: run at a fixed power (our loop only), ignoring the target. NaN = back to normal. */
    public void setOpenLoopPower(double p) {
        openLoopPower = p;
        if (!Double.isNaN(p)) enabled = true;
        loopTimer.reset();
        if (ownLoop) runOwnLoop(0);
    }

    public double getPower() { return power; }
    public double getVolts() { return volts; }
    public boolean isBoosting() { return boosting; }

    /** The hub's built-in velocity PIDF (only used when OWN_SPEED_LOOP is false). */
    public void setPidf(double p, double i, double d, double f) {
        if (present && !ownLoop) motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(p, i, d, f));
    }

    public PIDFCoefficients getPidf() {
        return present && !ownLoop ? motor.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER) : new PIDFCoefficients();
    }

    public boolean isEnabled() { return enabled; }
    public double getTargetTps() { return targetTps; }
    public double getVelocityTps() { return velocity; }
    public double getRpm() { return velocity * 60.0 / Constants.Launcher.TICKS_PER_REV; }
}
