package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * Point-to-point autonomous on the Localizer: drives through Constants.Auto.PATH with a simple
 * proportional controller on position and heading. No path library yet, on purpose.
 *
 * Only appears on the Driver Station when Constants.USE_PINPOINT is true (see register() below):
 * drive-encoder position slips too much to steer by.
 */
public class AutoPathed extends LinearOpMode {

    /** Adds this OpMode to the Driver Station list only when the Pinpoint is in use. */
    @OpModeRegistrar
    public static void register(OpModeManager manager) {
        if (!Constants.USE_PINPOINT) return;
        manager.register(new OpModeMeta.Builder()
                .setName("Auto Pathed")
                .setGroup("ApexStrykerz")
                .setFlavor(OpModeMeta.Flavor.AUTONOMOUS)
                .setTransitionTarget("Main TeleOp")
                .build(), AutoPathed.class);
    }

    private Robot robot;

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap);
        while (opModeInInit()) {
            telemetry.addData("Localizer", "%s  %s", robot.localizer.name(),
                    robot.localizer.isTrusted() ? "OK" : "NOT TRUSTED");
            telemetry.addData("Waypoints", Constants.Auto.PATH.length);
            robot.reportMissing(telemetry);
            telemetry.update();
        }
        if (!opModeIsActive()) return;

        robot.setPose(0, 0, 0);
        for (double[] wp : Constants.Auto.PATH) {
            if (!opModeIsActive() || !goTo(wp[0], wp[1], wp[2])) break;
        }
        robot.stop();
        while (opModeIsActive()) idle();
    }

    /** Drive to a field point; false if the localizer stopped being trustworthy. */
    private boolean goTo(double xIn, double yIn, double headingDeg) {
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < Constants.Auto.WAYPOINT_TIMEOUT_S) {
            robot.update();
            if (!robot.localizer.isTrusted()) {
                robot.drive.stop();
                telemetry.addLine("Localizer not trusted - stopping");
                telemetry.update();
                return false;
            }
            Pose2D pose = robot.localizer.getPose();
            double ex = xIn - pose.getX(DistanceUnit.INCH);
            double ey = yIn - pose.getY(DistanceUnit.INCH);
            double eh = AngleUnit.normalizeDegrees(headingDeg - pose.getHeading(AngleUnit.DEGREES));
            if (Math.hypot(ex, ey) < Constants.Auto.POSITION_TOLERANCE_IN
                    && Math.abs(eh) < Constants.Auto.HEADING_TOLERANCE_DEG) break;

            double max = Constants.Auto.PATH_MAX_POWER;
            double forward = Range.clip(ex * Constants.Auto.POSITION_KP, -max, max);
            double left = Range.clip(ey * Constants.Auto.POSITION_KP, -max, max);
            // Heading error positive means we need to turn left (counter-clockwise) = negative yaw.
            double turn = Range.clip(-eh * Constants.Auto.HEADING_KP, -max, max);
            robot.drive.driveFieldCentric(forward, -left, turn, robot.localizer.getHeading(), 1.0);

            telemetry.addData("Target", "%.1f, %.1f, %.0f°", xIn, yIn, headingDeg);
            telemetry.addData("Pose", "%.1f, %.1f, %.0f°", pose.getX(DistanceUnit.INCH),
                    pose.getY(DistanceUnit.INCH), pose.getHeading(AngleUnit.DEGREES));
            telemetry.update();
        }
        robot.drive.stop();
        return true;
    }
}
