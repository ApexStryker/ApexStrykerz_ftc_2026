package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.PathFollower;
import org.firstinspires.ftc.teamcode.hardware.Robot;

import java.util.ArrayList;
import java.util.List;

/**
 * Point-to-point autonomous on the Localizer: drives through Constants.Auto.PATH with the shared
 * PathFollower (moves and turns at once, with timeout, runaway and fence stops). No path library
 * yet, on purpose.
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
    private PathFollower follower;

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap);
        follower = new PathFollower(robot.drive, robot.localizer);
        List<PathFollower.Waypoint> path = new ArrayList<>();
        for (double[] wp : Constants.Auto.PATH) path.add(new PathFollower.Waypoint(wp[0], wp[1], wp[2]));
        follower.setFenceAround(path);
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

    /** Drive to a field point with the shared PathFollower; false if it had to stop for safety. */
    private boolean goTo(double xIn, double yIn, double headingDeg) {
        follower.start(new PathFollower.Waypoint(xIn, yIn, headingDeg));
        PathFollower.Result r = PathFollower.Result.RUNNING;
        while (opModeIsActive() && r == PathFollower.Result.RUNNING) {
            robot.update();
            r = follower.update();
            Pose2D pose = robot.localizer.getPose();
            telemetry.addData("Target", "%.1f, %.1f, %.0f°", xIn, yIn, headingDeg);
            telemetry.addData("Pose", "%.1f, %.1f, %.0f°", pose.getX(DistanceUnit.INCH),
                    pose.getY(DistanceUnit.INCH), pose.getHeading(AngleUnit.DEGREES));
            telemetry.addData("Status", r);
            telemetry.update();
        }
        if (!opModeIsActive()) follower.abort();
        // A waypoint that timed out is skipped; anything unsafe ends the path.
        return r == PathFollower.Result.ARRIVED || r == PathFollower.Result.TIMED_OUT;
    }
}
