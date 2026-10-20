package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.BallPath;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * PARKED (2026-10-19): the first Main TeleOp, with BallPath's smart firing: one ball per
 * windmill step, only when the launcher is back at speed, plus the automatic 4-ball stop and
 * NECTAR reject once the color sensor is fitted. Main TeleOp now runs the ball path by hand
 * (OperatorControls). Bring this back when the windmill index sensor makes each 90-degree step
 * exact: delete @Disabled, check the controls below match what the drivers want, rename.
 *
 * Match TeleOp for two drivers.
 *
 * DURING INIT (either gamepad): D-pad LEFT = red alliance, D-pad RIGHT = blue alliance.
 *
 * GAMEPAD 1 - DRIVER
 *   Left stick            drive and strafe (field-centric: up always moves away from you)
 *   Right stick X         rotate
 *   Hold LEFT BUMPER      robot-centric instead
 *   Hold LEFT TRIGGER     precision (40% speed)
 *   OPTIONS               re-zero field-centric (point the robot away from you first)
 *   SHARE                 debug page on / off (every motor, servo and sensor value)
 *
 * GAMEPAD 2 - OPERATOR
 *   RIGHT BUMPER          intake on / off
 *   Hold LEFT BUMPER      clear a jam (intake and windmill reverse)
 *   LEFT TRIGGER          launcher spin up / down
 *   Hold RIGHT TRIGGER    fire: one ball each time the launcher is back at speed
 *   Hold CROSS (A)        Flower mode: pull POLLEN out of a flower's bottom opening
 *   D-pad UP / DOWN       launcher speed preset (near / far)
 *
 * Rumble: short = launcher ready, long = holding 4 balls (intake stopped itself).
 * Hub LEDs: green = launcher ready, yellow = spinning up, blue = idle.
 * Square and triangle are not used: on gamepad 2 they never registered in testing.
 */
@Disabled
@TeleOp(name = "Smart Fire TeleOp", group = "ApexStrykerz")
public class SmartFireTeleOp extends LinearOpMode {

    private final Gamepad prev1 = new Gamepad(), prev2 = new Gamepad();

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap);
        Constants.Alliance alliance = Constants.DEFAULT_ALLIANCE;

        while (opModeInInit()) {
            if (gamepad1.dpad_left || gamepad2.dpad_left) alliance = Constants.Alliance.RED;
            if (gamepad1.dpad_right || gamepad2.dpad_right) alliance = Constants.Alliance.BLUE;
            telemetry.addData("Alliance", "%s   (D-pad left = RED, right = BLUE)", alliance);
            robot.reportMissing(telemetry);
            telemetry.addLine("Ready - press START");
            telemetry.update();
        }
        robot.ballPath.setAlliance(alliance);

        boolean collect = false, launcherOn = false;
        boolean debugPage = false;
        double driverZero = robot.localizer.getHeading();
        ElapsedTime loopTimer = new ElapsedTime();

        while (opModeIsActive()) {
            double loopMs = loopTimer.milliseconds();
            loopTimer.reset();
            robot.update();

            // ---------------- Operator ----------------
            if (gamepad2.right_bumper && !prev2.right_bumper) collect = !collect;
            boolean ltNow = gamepad2.left_trigger > 0.5, ltPrev = prev2.left_trigger > 0.5;
            if (ltNow && !ltPrev) launcherOn = !launcherOn;
            if (gamepad2.dpad_up && !prev2.dpad_up) robot.launcher.selectPreset(robot.launcher.getPresetIndex() + 1);
            if (gamepad2.dpad_down && !prev2.dpad_down) robot.launcher.selectPreset(robot.launcher.getPresetIndex() - 1);

            BallPath bp = robot.ballPath;
            bp.setCollect(collect);
            bp.setClear(gamepad2.left_bumper);
            bp.setFire(gamepad2.right_trigger > 0.5);
            bp.setFlower(gamepad2.cross);
            bp.setLauncher(launcherOn);

            if (bp.takeReadyEvent()) gamepad2.rumble(150);
            if (bp.takeFullEvent()) { gamepad2.rumble(500); gamepad1.rumble(300); collect = false; }
            bp.takeRejectEvent();
            robot.setHubLeds(bp.launcherReady() ? Robot.LED_READY
                    : robot.launcher.isEnabled() ? Robot.LED_BUSY : Robot.LED_IDLE);

            // ---------------- Driver ----------------
            if (gamepad1.options && !prev1.options) driverZero = robot.localizer.getHeading();
            double forward = -gamepad1.left_stick_y;
            double right = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;
            double scale = gamepad1.left_trigger > 0.5 ? Constants.Drive.PRECISION_SCALE : 1.0;
            if (bp.getMode() == BallPath.Mode.FLOWER) scale = Math.min(scale, Constants.Drive.FLOWER_SCALE);

            boolean fieldCentric = Constants.Drive.FIELD_CENTRIC_DEFAULT && robot.imu != null && !gamepad1.left_bumper;
            if (fieldCentric) {
                double heading = AngleUnit.normalizeRadians(robot.localizer.getHeading() - driverZero);
                robot.drive.driveFieldCentric(forward, right, turn, heading, scale);
            } else {
                robot.drive.driveRobotCentric(forward, right, turn, scale);
            }

            if (gamepad1.share && !prev1.share) debugPage = !debugPage;
            prev1.copy(gamepad1);
            prev2.copy(gamepad2);

            // ---------------- Telemetry ----------------
            if (debugPage) {
                robot.addDebugTelemetry(telemetry);
                telemetry.update();
                continue;
            }
            Pose2D pose = robot.localizer.getPose();
            telemetry.addData("Loop", "%.0f ms", loopMs);
            telemetry.addData("Drive", "%s%s", fieldCentric ? "FIELD" : "ROBOT",
                    scale < 1 ? String.format(" x%.2f", scale) : "");
            telemetry.addData("Pose", "x %.1f  y %.1f in  heading %.1f°  %s",
                    pose.getX(DistanceUnit.INCH), pose.getY(DistanceUnit.INCH),
                    pose.getHeading(AngleUnit.DEGREES), robot.localizer.isTrusted() ? "" : "(untrusted)");
            telemetry.addData("Mode", "%s %s   intake %s", bp.getMode(), bp.getFireStep(), collect ? "ON" : "off");
            telemetry.addData("Balls", bp.countKnown()
                    ? String.format("%d / %d   shots %d", bp.getBallCount(), Constants.BallSensor.MAX_BALLS, bp.getShotsFired())
                    : "unknown (no color sensor)   shots " + bp.getShotsFired());
            telemetry.addData("Launcher", "%s  target %.0f  actual %.0f tps  %s",
                    robot.launcher.getPresetName(), robot.launcher.getTargetTps(), robot.launcher.getVelocityTps(),
                    bp.launcherReady() ? "READY" : robot.launcher.isEnabled() ? "spinning up" : "off");
            if (robot.ballSensor.present) {
                telemetry.addData("Intake sensor", "%s  %.1f cm", robot.ballSensor.getBall(), robot.ballSensor.getDistanceCm());
            }
            telemetry.addData("Alliance", alliance);
            robot.reportMissing(telemetry);
            telemetry.update();
        }
        robot.stop();
    }
}
