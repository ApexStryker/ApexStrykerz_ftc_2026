package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Intake;
import org.firstinspires.ftc.teamcode.hardware.Launcher;
import org.firstinspires.ftc.teamcode.hardware.Windmill;

/**
 * Launcher Tuner: find the speed presets and tune the launcher's speed control on the field.
 *
 * Gamepad 1:
 *   CROSS (A)             launcher on / off
 *   D-pad UP / DOWN       target speed +/- 100 tps
 *   RIGHT / LEFT BUMPER   target speed +/- 20 tps
 *   CIRCLE (B)            choose which term to change: KV, KS, KP, KI (our own loop,
 *                         Constants.Launcher.OWN_SPEED_LOOP) or P, I, D, F (the hub's PIDF)
 *   D-pad RIGHT / LEFT    selected term x1.25 / /1.25
 *   TRIANGLE (Y)          CALIBRATE (our loop only): runs the wheel at two fixed powers for a few
 *                         seconds each, works out KV and KS from the speeds, uses them at once
 *                         and writes them to the log. Wheel must be free; takes about 6 s.
 *   RIGHT TRIGGER         feed one ball (one windmill step, intake pushes gently)
 *
 * Order for our loop: calibrate first, then with the launcher on at the shooting speed raise KP
 * until the speed holds steadily without swinging, then KI until it settles right on the target;
 * then feed balls and watch the dip and the recovery time. Copy the final numbers into
 * Constants.Launcher (the log line "launcher gains" has them all).
 *
 * Every 50 ms "time,target,actual,power,volts" is written to the robot log (tag LauncherTune),
 * so a run can be graphed afterwards:  adb logcat -s LauncherTune
 */
@TeleOp(name = "Launcher Tuner", group = "Test")
public class LauncherTuner extends LinearOpMode {

    private static final String[] HUB_TERMS = { "P", "I", "D", "F" };
    private static final String[] OWN_TERMS = { "KV", "KS", "KP", "KI" };

    @Override
    public void runOpMode() {
        Launcher launcher = new Launcher(hardwareMap);
        Windmill windmill = new Windmill(hardwareMap);
        Intake intake = new Intake(hardwareMap);
        Gamepad prev = new Gamepad();

        boolean own = launcher.usesOwnLoop();
        String[] terms = own ? OWN_TERMS : HUB_TERMS;
        PIDFCoefficients pidf = launcher.getPidf();
        double[] k = own ? launcher.getGains() : new double[] { pidf.p, pidf.i, pidf.d, pidf.f };
        int term = 0;
        double target = Constants.Launcher.PRESETS_TPS[0];
        launcher.setTargetTps(target);

        telemetry.addLine(launcher.present ? "Launcher Tuner ready." : "MISSING " + Constants.Config.LAUNCHER);
        telemetry.update();
        waitForStart();

        ElapsedTime clock = new ElapsedTime(), logTimer = new ElapsedTime(), sinceShot = new ElapsedTime();
        double minSinceFeed = Double.NaN, recoveryMs = Double.NaN;
        boolean watchingShot = false;
        // Calibration: 0 = off, 1 = low power, 2 = high power. Speeds averaged over each step's last second.
        int cal = 0;
        ElapsedTime calTimer = new ElapsedTime();
        double calSum = 0, calVolts = 0, calLowTps = 0, calLowVolts = 0;
        int calN = 0;
        String calResult = "-";

        while (opModeIsActive()) {
            if (cal == 0 && gamepad1.cross && !prev.cross) { if (launcher.isEnabled()) launcher.stop(); else launcher.spinUp(); }
            if (gamepad1.dpad_up && !prev.dpad_up) target += 100;
            if (gamepad1.dpad_down && !prev.dpad_down) target -= 100;
            if (gamepad1.right_bumper && !prev.right_bumper) target += 20;
            if (gamepad1.left_bumper && !prev.left_bumper) target -= 20;
            target = Math.max(0, target);
            launcher.setTargetTps(target);

            if (gamepad1.circle && !prev.circle) term = (term + 1) % 4;
            boolean changed = false;
            if (gamepad1.dpad_right && !prev.dpad_right) { k[term] = k[term] == 0 ? 0.1 : k[term] * 1.25; changed = true; }
            if (gamepad1.dpad_left && !prev.dpad_left) { k[term] = k[term] / 1.25; changed = true; }
            if (changed) {
                if (own) launcher.setGains(k[0], k[1], k[2], k[3]);
                else launcher.setPidf(k[0], k[1], k[2], k[3]);
                RobotLog.ii("Apex", "launcher gains: %s %.6f, %s %.4f, %s %.5f, %s %.5f",
                        terms[0], k[0], terms[1], k[1], terms[2], k[2], terms[3], k[3]);
            }

            if (own && gamepad1.triangle && !prev.triangle && cal == 0) {
                cal = 1;
                calTimer.reset();
                calSum = 0; calVolts = 0; calN = 0;
                launcher.setOpenLoopPower(Constants.Launcher.CAL_POWER_LOW);
                calResult = "running...";
            }
            if (cal > 0) {
                if (calTimer.seconds() >= Constants.Launcher.CAL_STEP_S - 1.0) {
                    calSum += launcher.getVelocityTps();
                    calVolts += launcher.getVolts();
                    calN++;
                }
                if (calTimer.seconds() >= Constants.Launcher.CAL_STEP_S && calN > 0) {
                    double tps = calSum / calN, v = calVolts / calN;
                    if (cal == 1) {
                        calLowTps = tps; calLowVolts = v;
                        cal = 2;
                        calTimer.reset();
                        calSum = 0; calVolts = 0; calN = 0;
                        launcher.setOpenLoopPower(Constants.Launcher.CAL_POWER_HIGH);
                    } else {
                        // Power as if at NOMINAL_VOLTS, then a straight line through both points.
                        double pLow = Constants.Launcher.CAL_POWER_LOW * calLowVolts / Constants.Launcher.NOMINAL_VOLTS;
                        double pHigh = Constants.Launcher.CAL_POWER_HIGH * v / Constants.Launcher.NOMINAL_VOLTS;
                        cal = 0;
                        launcher.setOpenLoopPower(Double.NaN);
                        launcher.stop();
                        if (tps - calLowTps > 50) {
                            k[0] = (pHigh - pLow) / (tps - calLowTps);
                            k[1] = Math.max(0, pLow - k[0] * calLowTps);
                            launcher.setGains(k[0], k[1], k[2], k[3]);
                            calResult = String.format("KV %.6f  KS %.4f  (%.0f tps @ %.2f, %.0f tps @ %.2f, %.1f V)",
                                    k[0], k[1], calLowTps, Constants.Launcher.CAL_POWER_LOW, tps,
                                    Constants.Launcher.CAL_POWER_HIGH, v);
                        } else {
                            calResult = String.format("FAILED: speeds %.0f and %.0f tps barely differ", calLowTps, tps);
                        }
                        RobotLog.ii("Apex", "launcher calibration: %s", calResult);
                    }
                }
            }

            boolean rt = gamepad1.right_trigger > 0.5, rtPrev = prev.right_trigger > 0.5;
            if (rt && !rtPrev && windmill.requestStep()) {
                minSinceFeed = launcher.getVelocityTps();
                watchingShot = true;
                sinceShot.reset();
                recoveryMs = Double.NaN;
            }
            prev.copy(gamepad1);

            launcher.update();
            windmill.update();
            if (windmill.isStepping()) intake.feed(); else intake.stop();

            double v = launcher.getVelocityTps();
            if (watchingShot) {
                minSinceFeed = Math.min(minSinceFeed, v);
                if (sinceShot.milliseconds() > 100 && launcher.atSpeed()) {
                    recoveryMs = sinceShot.milliseconds();
                    watchingShot = false;
                }
            }
            if (logTimer.milliseconds() >= 50) {
                logTimer.reset();
                RobotLog.ii("LauncherTune", "%.3f,%.0f,%.0f,%.3f,%.2f", clock.seconds(), target, v,
                        launcher.getPower(), launcher.getVolts());
            }

            telemetry.addData("Launcher", launcher.isEnabled() ? (launcher.atSpeed() ? "READY" : "spinning up") : "off");
            telemetry.addData("Target", "%.0f tps", target);
            telemetry.addData("Actual", "%.0f tps  (%.0f RPM)  error %+.0f", v, launcher.getRpm(), v - target);
            telemetry.addData("Last shot", Double.isNaN(minSinceFeed) ? "-"
                    : String.format("dipped to %.0f tps, recovered in %s", minSinceFeed,
                    Double.isNaN(recoveryMs) ? "..." : String.format("%.0f ms", recoveryMs)));
            if (own) {
                telemetry.addData("Own loop", "KV %.6f  KS %.4f  KP %.5f  KI %.5f", k[0], k[1], k[2], k[3]);
                telemetry.addData("Power", "%.2f%s   battery %.2f V", launcher.getPower(),
                        launcher.isBoosting() ? " (BOOST)" : "", launcher.getVolts());
                telemetry.addData("Calibrate", "%s   (triangle, wheel free)", cal > 0 ? "RUNNING step " + cal : calResult);
            } else {
                telemetry.addData("Hub PIDF", "P %.3f  I %.3f  D %.3f  F %.3f", k[0], k[1], k[2], k[3]);
            }
            telemetry.addData("Changing", "%s   (circle = next term, D-pad left/right = change)", terms[term]);
            telemetry.addData("Windmill", "%s%s", windmill.present ? "ok" : "MISSING",
                    windmill.hasIndexSwitch ? ", index switch" : ", timed steps");
            telemetry.update();
        }
        launcher.stop();
        intake.stop();
        windmill.hold();
    }
}
