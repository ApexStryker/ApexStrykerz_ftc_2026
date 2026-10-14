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
 * Launcher Tuner: find the speed presets and velocity PIDF on the practice field.
 *
 * Gamepad 1:
 *   CROSS (A)             launcher on / off
 *   D-pad UP / DOWN       target speed +/- 100 tps
 *   RIGHT / LEFT BUMPER   target speed +/- 20 tps
 *   CIRCLE (B)            choose which PIDF term to change (P, I, D, F)
 *   D-pad RIGHT / LEFT    selected term x1.25 / /1.25
 *   RIGHT TRIGGER         feed one ball (one windmill step, intake pushes gently)
 *
 * Watch the speed dip on each shot and how long it takes to recover; that sets the shot rate.
 * When a speed lands in the HIVE cell, copy it into Constants.Launcher.PRESETS_TPS, and any PIDF
 * you changed into Constants.Launcher (with USE_CUSTOM_PIDF = true).
 *
 * Every 50 ms "time,target,actual" is written to the robot log (tag LauncherTune), so a run can
 * be graphed afterwards:  adb logcat -s LauncherTune
 */
@TeleOp(name = "Launcher Tuner", group = "Test")
public class LauncherTuner extends LinearOpMode {

    private static final String[] TERMS = { "P", "I", "D", "F" };

    @Override
    public void runOpMode() {
        Launcher launcher = new Launcher(hardwareMap);
        Windmill windmill = new Windmill(hardwareMap);
        Intake intake = new Intake(hardwareMap);
        Gamepad prev = new Gamepad();

        PIDFCoefficients pidf = launcher.getPidf();
        double[] k = { pidf.p, pidf.i, pidf.d, pidf.f };
        int term = 0;
        double target = Constants.Launcher.PRESETS_TPS[0];
        launcher.setTargetTps(target);

        telemetry.addLine(launcher.present ? "Launcher Tuner ready." : "MISSING " + Constants.Config.LAUNCHER);
        telemetry.update();
        waitForStart();

        ElapsedTime clock = new ElapsedTime(), logTimer = new ElapsedTime(), sinceShot = new ElapsedTime();
        double minSinceFeed = Double.NaN, recoveryMs = Double.NaN;
        boolean watchingShot = false;

        while (opModeIsActive()) {
            if (gamepad1.cross && !prev.cross) { if (launcher.isEnabled()) launcher.stop(); else launcher.spinUp(); }
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
            if (changed) launcher.setPidf(k[0], k[1], k[2], k[3]);

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
                RobotLog.ii("LauncherTune", "%.3f,%.0f,%.0f", clock.seconds(), target, v);
            }

            telemetry.addData("Launcher", launcher.isEnabled() ? (launcher.atSpeed() ? "READY" : "spinning up") : "off");
            telemetry.addData("Target", "%.0f tps", target);
            telemetry.addData("Actual", "%.0f tps  (%.0f RPM)  error %+.0f", v, launcher.getRpm(), v - target);
            telemetry.addData("Last shot", Double.isNaN(minSinceFeed) ? "-"
                    : String.format("dipped to %.0f tps, recovered in %s", minSinceFeed,
                    Double.isNaN(recoveryMs) ? "..." : String.format("%.0f ms", recoveryMs)));
            telemetry.addData("PIDF", "P %.3f  I %.3f  D %.3f  F %.3f", k[0], k[1], k[2], k[3]);
            telemetry.addData("Changing", "%s   (circle = next term, D-pad left/right = change)", TERMS[term]);
            telemetry.addData("Windmill", "%s%s", windmill.present ? "ok" : "MISSING",
                    windmill.hasIndexSwitch ? ", index switch" : ", timed steps");
            telemetry.update();
        }
        launcher.stop();
        intake.stop();
        windmill.hold();
    }
}
