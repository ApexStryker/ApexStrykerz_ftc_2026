package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * Simple autonomous that needs no odometry hardware. A starting point to grow from.
 *
 *   1. (Optional, Constants.Auto.FIRE_PRELOADS) spin up and fire the 4 preloaded POLLEN at the
 *      HIVE from the starting spot. Off until a start spot that reaches the HIVE is chosen.
 *   2. Drive straight forward LEAVE_DISTANCE_IN inches, using the drive encoders for distance
 *      and the IMU to hold heading. Earns LEAVE (3 points): no longer touching the wall.
 *   3. Stop and wait for TeleOp.
 *
 * Start position (rule G304): robot touching the perimeter wall on our side, not in the
 * LOADING ZONE, not touching a FLOWER, facing the open field, with 4 POLLEN preloaded.
 *
 * TODO: park in the LOADING ZONE (5 points) once the team picks a start spot.
 */
@Autonomous(name = "Auto Basic", group = "ApexStrykerz", preselectTeleOp = "Main TeleOp")
public class AutoBasic extends LinearOpMode {

    private Robot robot;

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap);
        Constants.Alliance alliance = Constants.DEFAULT_ALLIANCE;

        while (opModeInInit()) {
            if (gamepad1.dpad_left) alliance = Constants.Alliance.RED;
            if (gamepad1.dpad_right) alliance = Constants.Alliance.BLUE;
            telemetry.addData("Alliance", "%s   (gamepad 1 D-pad left = RED, right = BLUE)", alliance);
            telemetry.addData("Plan", "%sdrive forward %.0f in",
                    Constants.Auto.FIRE_PRELOADS ? "fire " + Constants.Auto.PRELOADS + ", then " : "",
                    Constants.Auto.LEAVE_DISTANCE_IN);
            robot.reportMissing(telemetry);
            telemetry.update();
        }
        if (!opModeIsActive()) return;

        robot.ballPath.setAlliance(alliance);
        robot.setPose(0, 0, 0);

        if (Constants.Auto.FIRE_PRELOADS && robot.launcher.present && robot.windmill.present) {
            firePreloads();
        }
        driveForward(Constants.Auto.LEAVE_DISTANCE_IN);

        robot.stop();
        while (opModeIsActive()) {
            telemetry.addLine("Auto done. Waiting for TeleOp.");
            telemetry.update();
            idle();
        }
    }

    /** Spin up and fire until the preloads are gone or time runs out. */
    private void firePreloads() {
        robot.ballPath.setBallCount(Constants.Auto.PRELOADS);
        robot.launcher.selectPreset(Constants.Auto.PRELOAD_PRESET);
        robot.ballPath.setFire(true);
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive()
                && robot.ballPath.getShotsFired() < Constants.Auto.PRELOADS
                && robot.ballPath.getEmptyFeeds() < Constants.Auto.PRELOADS
                && timer.seconds() < Constants.Auto.FIRE_TIMEOUT_S) {
            robot.update();
            telemetry.addData("Step", "firing preloads");
            telemetry.addData("Shots", robot.ballPath.getShotsFired());
            telemetry.addData("Launcher", "%.0f / %.0f tps", robot.launcher.getVelocityTps(), robot.launcher.getTargetTps());
            telemetry.update();
        }
        robot.ballPath.setFire(false);
        robot.ballPath.update();
        robot.launcher.stop();
    }

    /** Drive straight forward, holding the starting heading with the IMU. */
    private void driveForward(double inches) {
        double startIn = robot.drive.averageForwardInches();
        double targetHeading = robot.localizer.getHeading();
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive()
                && robot.drive.averageForwardInches() - startIn < inches
                && timer.seconds() < Constants.Auto.DRIVE_TIMEOUT_S) {
            robot.update();
            double errorDeg = Math.toDegrees(AngleUnit.normalizeRadians(robot.localizer.getHeading() - targetHeading));
            // Drifted counter-clockwise (positive error) -> turn right (positive yaw).
            double turn = Constants.Auto.HEADING_KP * errorDeg;
            robot.drive.driveRobotCentric(Constants.Auto.DRIVE_POWER, 0, turn, 1.0);
            telemetry.addData("Step", "leaving the wall");
            telemetry.addData("Driven", "%.1f / %.1f in", robot.drive.averageForwardInches() - startIn, inches);
            telemetry.addData("Heading error", "%.1f°", errorDeg);
            telemetry.update();
        }
        robot.drive.stop();
    }
}
