package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * Match TeleOp for two drivers. The operator runs the ball path by hand, exactly as in
 * Ball Path Test (without its fine +-25 speed steps).
 *
 * DURING INIT (either gamepad): D-pad LEFT = red alliance, D-pad RIGHT = blue alliance.
 *
 * GAMEPAD 1 - DRIVER (DriverControls)
 *   Left stick            drive and strafe
 *   Right stick X         rotate
 *   Hold LEFT TRIGGER     precision (40% speed)
 *   Hold LEFT BUMPER      robot-centric instead (once field-centric is on)
 *   OPTIONS               re-zero field-centric (point the robot away from you first)
 *   SHARE                 debug page on / off
 *
 * GAMEPAD 2 - OPERATOR (OperatorControls)
 *   Hold RIGHT BUMPER     intake + side wheels pull in, while held
 *   CROSS (A)             intake + side wheels on / off
 *   RIGHT STICK up/down   intake, side wheels and windmill together, as fast as pushed
 *                         (up = toward the launcher, down = push balls out)
 *   Hold RIGHT TRIGGER    shoot: launcher spins up, then the windmill turns once it is at speed
 *   LEFT TRIGGER          launcher on / off
 *   LEFT BUMPER           windmill on / off
 *   D-pad UP / DOWN       launcher speed +100 / -100 tps
 *   CIRCLE (B)            jump to the other speed preset (near / far)
 *
 * Rumble: short = launcher at speed. Hub LEDs: green = ready, yellow = spinning up, blue = idle.
 * The earlier version with automatic one-ball-at-a-time firing is parked as Smart Fire TeleOp.
 */
@TeleOp(name = "Main TeleOp", group = "ApexStrykerz")
public class MainTeleOp extends LinearOpMode {

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

        DriverControls driver = new DriverControls(robot);
        OperatorControls operator = new OperatorControls(robot, false);
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
            telemetry.addData("Loop", "%.0f ms   (gamepad 1 SHARE = debug page)", robot.getLoopMs());
            operator.addTelemetry(telemetry);
            driver.addTelemetry(telemetry);
            Pose2D pose = robot.localizer.getPose();
            telemetry.addData("Pose", "x %.1f  y %.1f in  heading %.1f°  %s",
                    pose.getX(DistanceUnit.INCH), pose.getY(DistanceUnit.INCH),
                    pose.getHeading(AngleUnit.DEGREES), robot.localizer.isTrusted() ? "" : "(untrusted)");
            telemetry.addData("Alliance", alliance);
            robot.reportMissing(telemetry);
            telemetry.update();
        }
        robot.stop();
    }
}
