package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.localization.PinpointLocalizer;

/**
 * Pinpoint Test: set up and check the goBILDA Pinpoint before switching it on for real.
 * Works while Constants.USE_PINPOINT is still false.
 *
 * KEEP THE ROBOT STILL while pressing INIT: the Pinpoint calibrates its IMU then.
 *
 * Gamepad 1:
 *   Left stick / right stick X   drive slowly, robot-centric (to drive around and come back)
 *   CROSS (A)                     zero the position (robot standing still!)
 *
 * The four checks (the Pinpoint slides in the deck):
 *   1. Push forward: x goes UP.  Push left: y goes UP.  Wrong way? Flip PINPOINT_X_DIR / _Y_DIR.
 *   2. Spin one turn LEFT on the spot: heading +360, x and y stay within ~4 in.  More? A or B sign.
 *   3. Tape-measure a straight drive: the distance must match.  If not: PINPOINT_POD type.
 *   4. Drive around, come back to the start: within ~0.5 in.
 */
@TeleOp(name = "Pinpoint Test", group = "Test")
public class PinpointTest extends LinearOpMode {

    /** Gentle driving for the checks. */
    private static final double DRIVE_SCALE = Constants.Drive.PRECISION_SCALE;

    @Override
    public void runOpMode() {
        PinpointLocalizer pinpoint = new PinpointLocalizer(hardwareMap);
        Drivetrain drive = new Drivetrain(hardwareMap);
        Gamepad prev = new Gamepad();
        double turns = 0, lastHeading = 0;

        while (opModeInInit()) {
            pinpoint.update();
            telemetry.addLine("Pinpoint Test. Keep the robot STILL until the status is READY.");
            telemetry.addData("Status", pinpoint.statusText());
            telemetry.addData("Settings", "pod %s, A %.1f mm, B %.1f mm, directions %s / %s",
                    Constants.Odometry.PINPOINT_POD, Constants.Odometry.PINPOINT_X_POD_OFFSET_MM,
                    Constants.Odometry.PINPOINT_Y_POD_OFFSET_MM, Constants.Odometry.PINPOINT_X_DIR,
                    Constants.Odometry.PINPOINT_Y_DIR);
            if (!pinpoint.present) telemetry.addLine("Add it on the Driver Hub: I2C bus 1, 'goBILDA Pinpoint Odometry Computer', name 'pinpoint'.");
            telemetry.update();
        }

        while (opModeIsActive()) {
            pinpoint.update();
            if (gamepad1.cross && !prev.cross) { pinpoint.resetPosAndIMU(); turns = 0; lastHeading = 0; }
            prev.copy(gamepad1);

            drive.driveRobotCentric(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x, DRIVE_SCALE);

            // Count whole turns, so check 2 (heading +360) is visible.
            double heading = Math.toDegrees(pinpoint.getHeading());
            double change = heading - lastHeading;
            if (change < -180) turns++;
            if (change > 180) turns--;
            lastHeading = heading;

            Pose2D pose = pinpoint.getPose();
            telemetry.addData("Status", pinpoint.statusText());
            telemetry.addData("x (forward)", "%.2f in  (%.0f mm)", pose.getX(DistanceUnit.INCH), pose.getX(DistanceUnit.MM));
            telemetry.addData("y (left)", "%.2f in  (%.0f mm)", pose.getY(DistanceUnit.INCH), pose.getY(DistanceUnit.MM));
            telemetry.addData("Heading", "%.1f°  (total %.0f° since zero)", heading, turns * 360 + heading);
            telemetry.addData("Raw pod counts", "X %d   Y %d", pinpoint.rawX(), pinpoint.rawY());
            telemetry.addLine("Push forward: x up. Push left: y up. Spin left one turn: total +360.");
            telemetry.addLine("CROSS = zero the position (robot still).");
            telemetry.update();
        }
        drive.stop();
    }
}
