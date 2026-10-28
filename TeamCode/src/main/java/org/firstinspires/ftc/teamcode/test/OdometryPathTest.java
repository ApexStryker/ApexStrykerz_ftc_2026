package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.hardware.PathFollower;
import org.firstinspires.ftc.teamcode.hardware.PathFollower.Waypoint;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.localization.DriveEncoderLocalizer;
import org.firstinspires.ftc.teamcode.localization.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.util.Debug;
import org.firstinspires.ftc.teamcode.util.OdometryMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Odometry Path Test: the robot drives a path on its own with the Pinpoint, moving, strafing and
 * turning, and comes back to where it started. Run Odometry Check first.
 *
 * The robot always THINKS it got back to the start, because it steered there by the Pinpoint.
 * The real test is a ruler: mark the start with tape, and measure how far the robot ends up
 * from the mark. goBILDA's guide: within about 1/2 inch.
 *
 * INIT (gamepad 1):  D-pad UP / DOWN = choose the path,  D-pad LEFT / RIGHT = laps (1 to 5).
 * Running:           ANY button on either gamepad stops the robot at once.
 *
 * The paths go FORWARD and mostly to the LEFT of the start (the diamond also 2 ft to the right);
 * INIT shows exactly how much room each needs. Square paths need about 4 x 4 ft; the field loop
 * needs 8 x 8 ft: start about 2 ft from the back-right corner of the 12 ft field, facing forward.
 *
 * Also shown and logged: where the drive encoders and the Control Hub IMU think the robot is,
 * as a second opinion. Stops by itself if the robot drives the wrong way (RUNAWAY), leaves the
 * box (OUT_OF_BOUNDS), takes too long, or the Pinpoint stops being READY.
 */
@Autonomous(name = "Odometry Path Test", group = "Test")
public class OdometryPathTest extends LinearOpMode {

    enum Path {
        SQUARE("Square: forward, strafe left, back, strafe right; no turning"),
        DIAMOND("Diamond: diagonals only, driving and strafing at once"),
        SQUARE_TURNS("Square with turns: 90° turn at every corner while moving"),
        FIELD_LOOP("Field loop: long straights, turns and a diagonal across the field");

        final String description;
        Path(String d) { description = d; }
    }

    /** The waypoints for one lap; every lap ends back at (0, 0, 0). */
    static List<Waypoint> lap(Path path) {
        double s = Constants.Odometry.TEST_SQUARE_IN, h = s / 2, l = Constants.Odometry.TEST_FIELD_LOOP_IN;
        List<Waypoint> w = new ArrayList<>();
        switch (path) {
            case SQUARE:
                w.add(new Waypoint(s, 0, 0)); w.add(new Waypoint(s, s, 0));
                w.add(new Waypoint(0, s, 0)); w.add(new Waypoint(0, 0, 0));
                break;
            case DIAMOND:
                w.add(new Waypoint(h, h, 0)); w.add(new Waypoint(s, 0, 0));
                w.add(new Waypoint(h, -h, 0)); w.add(new Waypoint(0, 0, 0));
                break;
            case SQUARE_TURNS:
                w.add(new Waypoint(s, 0, 90)); w.add(new Waypoint(s, s, 180));
                w.add(new Waypoint(0, s, -90)); w.add(new Waypoint(0, 0, 0));
                break;
            case FIELD_LOOP:
                w.add(new Waypoint(l, 0, 0)); w.add(new Waypoint(l, l, 90));
                w.add(new Waypoint(l / 2, l / 2, 0)); w.add(new Waypoint(0, l, 0));
                w.add(new Waypoint(0, 0, 0));
                break;
        }
        return w;
    }

    static List<Waypoint> route(Path path, int laps) {
        List<Waypoint> all = new ArrayList<>();
        for (int i = 0; i < laps; i++) all.addAll(lap(path));
        return all;
    }

    private final Gamepad prev = new Gamepad();

    @Override
    public void runOpMode() {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        PinpointLocalizer pinpoint = new PinpointLocalizer(hardwareMap);
        Drivetrain drive = new Drivetrain(hardwareMap);
        IMU imu = Robot.initImu(hardwareMap);
        DriveEncoderLocalizer encoders = new DriveEncoderLocalizer(drive, imu);
        PathFollower follower = new PathFollower(drive, pinpoint);

        Path[] paths = Path.values();
        int choice = 0, laps = 1;
        while (opModeInInit()) {
            pinpoint.update();
            if (pressed(gamepad1.dpad_up, prev.dpad_up)) choice = (choice + paths.length - 1) % paths.length;
            if (pressed(gamepad1.dpad_down, prev.dpad_down)) choice = (choice + 1) % paths.length;
            if (pressed(gamepad1.dpad_right, prev.dpad_right)) laps = Math.min(5, laps + 1);
            if (pressed(gamepad1.dpad_left, prev.dpad_left)) laps = Math.max(1, laps - 1);
            prev.copy(gamepad1);

            List<Waypoint> one = lap(paths[choice]);
            telemetry.addLine("ODOMETRY PATH TEST. Keep the robot STILL until READY.");
            telemetry.addData("Pinpoint", pinpoint.statusText());
            for (int i = 0; i < paths.length; i++) telemetry.addLine((i == choice ? ">> " : "    ") + paths[i].description);
            telemetry.addData("Laps", "%d   (D-pad left / right)", laps);
            telemetry.addData("Needs", "%.0f in ahead, %.0f in to the left, %.0f in to the right",
                    maxX(one), Math.max(0, maxY(one)), Math.max(0, -minY(one)));
            telemetry.addLine("Mark the start with tape. Robot faces forward. ANY button stops it.");
            telemetry.update();
        }
        if (!opModeIsActive()) return;

        Path path = paths[choice];
        List<Waypoint> route = route(path, laps);
        if (!pinpoint.isTrusted()) {
            Debug.warn("Odometry Path Test: not started, %s", pinpoint.statusText());
            while (opModeIsActive()) {
                telemetry.addLine("NOT STARTED: the Pinpoint is not READY.");
                telemetry.addData("Pinpoint", pinpoint.statusText());
                telemetry.addLine("Run Odometry Check first.");
                telemetry.update();
                idle();
            }
            return;
        }

        // A button still held from choosing the path would count as "stop": wait until released.
        while (opModeIsActive() && (OdometryCheck.anyButton(gamepad1) || OdometryCheck.anyButton(gamepad2))) {
            telemetry.addLine("Let go of the gamepad buttons to start.");
            telemetry.update();
        }

        Pose2D zero = new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0);
        pinpoint.setPose(zero);
        encoders.setPose(zero);
        follower.setFenceAround(route);
        Debug.log("Odometry Path Test: %s, %d lap(s), %d waypoints", path, laps, route.size());

        double lastP = 0, lastI = imuDeg(imu), turnP = 0, turnI = 0;
        List<String> rows = new ArrayList<>();
        String outcome = "done";
        ElapsedTime total = new ElapsedTime();

        for (int i = 0; i < route.size() && opModeIsActive(); i++) {
            Waypoint w = route.get(i);
            follower.start(w);
            PathFollower.Result r = PathFollower.Result.RUNNING;
            while (opModeIsActive() && r == PathFollower.Result.RUNNING) {
                pinpoint.update();
                encoders.update();
                double p = Math.toDegrees(pinpoint.getHeading()), im = imuDeg(imu);
                turnP += OdometryMath.headingChange(lastP, p); lastP = p;
                turnI += OdometryMath.headingChange(lastI, im); lastI = im;
                if (OdometryCheck.anyButton(gamepad1) || OdometryCheck.anyButton(gamepad2)) {
                    follower.abort();
                    outcome = "STOPPED by a button";
                    break;
                }
                r = follower.update();
                showLive(i, route.size(), w, pinpoint, encoders, follower);
            }
            if (!outcome.equals("done")) break;
            if (!opModeIsActive()) { follower.abort(); break; }
            if (r != PathFollower.Result.ARRIVED) {
                outcome = "STOPPED at waypoint " + (i + 1) + ": " + r + explain(r);
                break;
            }

            // Settle, then record where everything thinks the robot is.
            ElapsedTime settle = new ElapsedTime();
            while (opModeIsActive() && settle.seconds() < Constants.Auto.WAYPOINT_SETTLE_S) {
                pinpoint.update(); encoders.update();
                double p = Math.toDegrees(pinpoint.getHeading()), im = imuDeg(imu);
                turnP += OdometryMath.headingChange(lastP, p); lastP = p;
                turnI += OdometryMath.headingChange(lastI, im); lastI = im;
            }
            Pose2D pp = pinpoint.getPose(), en = encoders.getPose();
            String row = String.format("%d %s: pinpoint %.1f, %.1f, %.0f°   encoders %.1f, %.1f   off by %.1f in",
                    i + 1, w, pp.getX(DistanceUnit.INCH), pp.getY(DistanceUnit.INCH), pp.getHeading(AngleUnit.DEGREES),
                    en.getX(DistanceUnit.INCH), en.getY(DistanceUnit.INCH),
                    Math.hypot(pp.getX(DistanceUnit.INCH) - en.getX(DistanceUnit.INCH), pp.getY(DistanceUnit.INCH) - en.getY(DistanceUnit.INCH)));
            rows.add(row);
            Debug.log("Odometry Path Test: %s", row);
        }
        drive.stop();

        Pose2D pp = pinpoint.getPose(), en = encoders.getPose();
        String finalPin = String.format("x %.2f, y %.2f in, heading %.1f°", pp.getX(DistanceUnit.INCH), pp.getY(DistanceUnit.INCH), pp.getHeading(AngleUnit.DEGREES));
        String finalEnc = String.format("%.1f in from the start", Math.hypot(en.getX(DistanceUnit.INCH), en.getY(DistanceUnit.INCH)));
        String headings = imu == null ? "no IMU" : String.format("Pinpoint turned %.0f°, IMU %.0f° (differ %.1f°)", turnP, turnI, turnP - turnI);
        Debug.log("Odometry Path Test: %s in %.0f s. Pinpoint end %s; encoders %s; %s", outcome, total.seconds(), finalPin, finalEnc, headings);

        while (opModeIsActive()) {
            telemetry.addData("Result", outcome);
            telemetry.addLine(String.format("NOW MEASURE: robot centre to the tape mark. Goal: %.1f in or less.", Constants.Odometry.RETURN_MAX_ERROR_IN));
            telemetry.addData("Pinpoint thinks", finalPin);
            telemetry.addData("Drive encoders think", finalEnc);
            telemetry.addData("Headings", headings);
            for (int i = Math.max(0, rows.size() - 8); i < rows.size(); i++) telemetry.addLine(rows.get(i));
            telemetry.update();
            idle();
        }
    }

    private void showLive(int i, int n, Waypoint w, PinpointLocalizer pinpoint, DriveEncoderLocalizer encoders, PathFollower f) {
        Pose2D p = pinpoint.getPose(), e = encoders.getPose();
        telemetry.addData("Waypoint", "%d of %d  %s   (ANY button stops)", i + 1, n, w);
        telemetry.addData("Pinpoint", "x %.1f  y %.1f in  heading %.0f°", p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH), p.getHeading(AngleUnit.DEGREES));
        telemetry.addData("Drive encoders", "x %.1f  y %.1f in", e.getX(DistanceUnit.INCH), e.getY(DistanceUnit.INCH));
        telemetry.addData("To go", "%.1f in, %.0f°, %.1f s", f.distanceToGo(), f.headingToGo(), f.elapsed());
        telemetry.update();
    }

    private static String explain(PathFollower.Result r) {
        switch (r) {
            case RUNAWAY: return " (driving away from the target: run Odometry Check, a direction may be flipped)";
            case OUT_OF_BOUNDS: return " (left the path's box)";
            case TIMED_OUT: return " (too slow: blocked, or power too low)";
            case LOST: return " (Pinpoint stopped being READY)";
            default: return "";
        }
    }

    private static double imuDeg(IMU imu) { return imu == null ? 0 : imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES); }

    private static boolean pressed(boolean now, boolean before) { return now && !before; }

    private static double maxX(List<Waypoint> w) { double m = 0; for (Waypoint p : w) m = Math.max(m, p.x); return m; }
    private static double maxY(List<Waypoint> w) { double m = 0; for (Waypoint p : w) m = Math.max(m, p.y); return m; }
    private static double minY(List<Waypoint> w) { double m = 0; for (Waypoint p : w) m = Math.min(m, p.y); return m; }
}
