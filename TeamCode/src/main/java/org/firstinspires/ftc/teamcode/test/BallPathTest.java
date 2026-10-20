package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.HiveCamera;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.opmodes.DriverControls;
import org.firstinspires.ftc.teamcode.opmodes.OperatorControls;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.List;

/**
 * Ball Path Test: Main TeleOp's controls (DriverControls, OperatorControls) plus the fine
 * +-25 tps launcher steps and the camera, for testing and tuning.
 *
 * GAMEPAD 1 - DRIVER, same as Main TeleOp
 *   Left stick drive and strafe, right stick X rotate, hold LEFT TRIGGER precision,
 *   SHARE debug page on / off (every motor, servo and sensor value)
 *
 * GAMEPAD 2 - OPERATOR, same as Main TeleOp plus D-pad right/left
 *   Hold RIGHT BUMPER     intake + side wheels pull in, while held
 *   CROSS (A)             intake + side wheels on / off
 *   RIGHT STICK up / down intake, side wheels and windmill together, as fast as pushed:
 *                         up = toward the launcher, down = backward (push balls out)
 *   Hold RIGHT TRIGGER    shoot: launcher spins up, then the windmill turns once it is at speed
 *   LEFT TRIGGER          launcher on / off
 *   LEFT BUMPER           windmill on / off
 *   D-pad UP / DOWN       launcher speed +100 / -100 tps
 *   D-pad RIGHT / LEFT    launcher speed +25 / -25 tps (fine)
 *   CIRCLE (B)            jump to the other speed preset (near / far)
 *
 * The speed changes at once, even while spinning. When a speed lands well, copy the number from
 * the screen into Constants.Launcher.PRESETS_TPS. Every change is also written to the robot log.
 *
 * CAMERA (optional, "webcam" in the configuration): during INIT the live picture is on the
 * Driver Station (menu > Camera Stream). INIT and running both show the HIVE AprilTags it sees
 * and the closest one's distance and angles (approximate until the C615 is calibrated).
 *
 * The on/off switches are independent, so the windmill can be turned on with the launcher off;
 * the screen warns when it is.
 * Short rumble on gamepad 2 = launcher at speed. Hub light: green ready, yellow spinning up.
 */
@TeleOp(name = "Ball Path Test", group = "Test")
public class BallPathTest extends LinearOpMode {

    private final Gamepad prev1 = new Gamepad(), prev2 = new Gamepad();

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap);
        HiveCamera camera = new HiveCamera(hardwareMap);

        // INIT: the live picture is on the Driver Station (menu > Camera Stream) until START.
        while (opModeInInit()) {
            telemetry.addLine("Ball Path Test. Gamepad 2: hold RB or CROSS intake, right stick up/down whole path, hold RT shoot,");
            telemetry.addLine("LT launcher on/off, LB windmill on/off, D-pad and circle launcher speed.");
            telemetry.addData("Camera", "%s   (Driver Station menu > Camera Stream for the picture)", camera.state());
            showHiveTags(camera);
            robot.reportMissing(telemetry);
            if (!camera.present) telemetry.addLine("MISSING: " + Constants.Config.WEBCAM);
            telemetry.update();
        }

        DriverControls driver = new DriverControls(robot);
        OperatorControls operator = new OperatorControls(robot, true);   // true: with the +-25 steps
        boolean debugPage = false;

        while (opModeIsActive()) {
            robot.updateManual();
            operator.update(gamepad2, prev2);
            driver.update(gamepad1, prev1);

            if (gamepad1.share && !prev1.share) debugPage = !debugPage;
            prev1.copy(gamepad1);
            prev2.copy(gamepad2);

            if (debugPage) {
                robot.addDebugTelemetry(telemetry);
                telemetry.update();
                continue;
            }
            operator.addTelemetry(telemetry);
            driver.addTelemetry(telemetry);
            showHiveTags(camera);
            robot.reportMissing(telemetry);
            if (!camera.present) telemetry.addLine("MISSING: " + Constants.Config.WEBCAM);
            telemetry.update();
        }
        robot.stop();
        camera.close();
    }

    /** The HIVE AprilTags the camera sees; the closest one with its distance and angles. */
    private void showHiveTags(HiveCamera camera) {
        if (!camera.present) return;
        List<AprilTagSingleDetection> seen = camera.hiveTags();
        StringBuilder ids = new StringBuilder();
        for (AprilTagSingleDetection d : seen) ids.append(d.id).append(HiveCamera.isRed(d.id) ? "R " : "B ");
        telemetry.addData("HIVE tags", seen.isEmpty() ? "none" : ids.toString().trim());
        AprilTagSingleDetection near = camera.nearestHiveTag();
        if (near != null) {
            telemetry.addData("Nearest", "%d %s  range %.1f in  bearing %.0f°  elevation %.0f°  (approx.)",
                    near.id, HiveCamera.isRed(near.id) ? "red" : "blue",
                    near.ftcPose.range, near.ftcPose.bearing, near.ftcPose.elevation);
        }
    }

}
