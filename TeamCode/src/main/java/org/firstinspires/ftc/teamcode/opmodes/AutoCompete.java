package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.PathFollower;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.util.Debug;
import org.firstinspires.ftc.teamcode.util.FieldMap;

import java.util.ArrayList;
import java.util.List;

/**
 * The match autonomous: shoot the 4 preloads into our hive, then PARK in our Loading Zone.
 *
 *  1. START at Constants.Compete.START_POSE: intake against a wall, launcher facing the hive.
 *  2. Drive to SHOOT_POSE if it is somewhere else (the same = shoot without moving).
 *  3. Spin up; the windmill feeds while the wheel is at full speed and pauses after each shot.
 *  4. Drive to PARK_POSE in the Loading Zone, around the hive frame and flowers (util/FieldMap).
 *
 * Time: leaves for the Loading Zone no later than PARK_RESERVE_S before the end of AUTO, or
 * earlier if the trip needs it, even if not every ball is gone.
 * Bumps: if the robot stops getting closer (a robot or something else in the way), it backs off,
 * steps sideways, plans again and retries, up to MAX_RETRIES times.
 *
 * Two entries on the Driver Station, one per start slot:
 *  - "Auto Compete Right": start against the RIGHT-hand side wall (the poses in Constants.Compete
 *    as written; the one the team tested first);
 *  - "Auto Compete Left": the mirror image. The start, route, park spot and the field map (hive
 *    legs, flowers, Loading Zone) are all flipped left-right: y -> 144 - y, heading -> -heading.
 * Everything is in ARENA coordinates. Only appears on the Driver Station when
 * Constants.USE_PINPOINT is true.
 */
public abstract class AutoCompete extends LinearOpMode {

    @OpModeRegistrar
    public static void register(OpModeManager manager) {
        if (!Constants.USE_PINPOINT) return;
        manager.register(meta("Auto Compete Right"), Right.class);
        manager.register(meta("Auto Compete Left"), Left.class);
    }

    private static OpModeMeta meta(String name) {
        return new OpModeMeta.Builder()
                .setName(name)
                .setGroup("ApexStrykerz")
                .setFlavor(OpModeMeta.Flavor.AUTONOMOUS)
                .setTransitionTarget("Main TeleOp")
                .build();
    }

    /** Start against the right-hand side wall: Constants.Compete as written. */
    public static class Right extends AutoCompete { public Right() { super(false); } }

    /** Start against the left-hand side wall: everything mirrored. */
    public static class Left extends AutoCompete { public Left() { super(true); } }

    /** True for the Left slot: mirror everything. */
    private final boolean left;

    protected AutoCompete(boolean left) { this.left = left; }

    /** A pose {x, y, heading} or point {x, y} for this slot: as written for Right, mirrored for Left. */
    static double[] forSlot(double[] p, boolean left) {
        if (!left) return p.clone();
        double[] m = p.clone();
        m[1] = Constants.Auto.ARENA_SIZE_IN - p[1];
        if (m.length > 2) m[2] = AngleUnit.normalizeDegrees(-p[2]);
        return m;
    }

    /** How a drive ended. */
    private enum Drive { ARRIVED, BUMPED, UNSAFE, OUT_OF_TIME }

    private Robot robot;
    private PathFollower follower;
    private FieldMap map;
    private final ElapsedTime match = new ElapsedTime();
    private String step = "";
    private int shots;
    private double leaveAt = Constants.Compete.AUTO_LENGTH_S - Constants.Compete.PARK_RESERVE_S;
    /** The point follow() was last driving to, for backing off after a bump. */
    private double[] lastTarget = { 0, 0 };

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap);
        follower = new PathFollower(robot.drive, robot.localizer);
        map = FieldMap.compete(left);
        double[] start = forSlot(Constants.Compete.START_POSE, left);
        double[] shoot = forSlot(Constants.Compete.SHOOT_POSE, left);
        double[] park = forSlot(Constants.Compete.PARK_POSE, left);
        String slot = left ? "LEFT" : "RIGHT";
        String problems = checkPlan(start, shoot, park);

        while (opModeInInit()) {
            telemetry.addData("Localizer", "%s  %s", robot.localizer.name(),
                    robot.localizer.isTrusted() ? "OK" : "NOT TRUSTED");
            telemetry.addLine(slot + " SLOT: intake against the " + slot + " side wall, launcher facing the hive");
            telemetry.addData("Start", "%.0f, %.0f, %.0f°", start[0], start[1], start[2]);
            telemetry.addData("Plan", "shoot %d at %.0f, %.0f, then park at %.0f, %.0f",
                    Constants.Compete.PRELOADS, shoot[0], shoot[1], park[0], park[1]);
            telemetry.addLine(problems.isEmpty() ? "Map check: OK" : "MAP CHECK: " + problems);
            robot.reportMissing(telemetry);
            telemetry.update();
        }
        if (!opModeIsActive()) return;

        match.reset();
        robot.setPose(start[0], start[1], start[2]);
        robot.launcher.selectPreset(Constants.Compete.SHOOT_PRESET);
        robot.launcher.spinUp();    // spins up while driving to the shooting spot
        Debug.log("compete: %s slot, start %.0f, %.0f, %.0f°", slot, start[0], start[1], start[2]);

        if (Math.hypot(shoot[0] - start[0], shoot[1] - start[1]) > Constants.Auto.POSITION_TOLERANCE_IN
                || Math.abs(AngleUnit.normalizeDegrees(shoot[2] - start[2])) > Constants.Auto.HEADING_TOLERANCE_DEG) {
            step = "to the shooting spot";
            driveTo(shoot, true);
        }
        planLeaveTime(park);
        if (opModeIsActive() && match.seconds() < leaveAt) shoot();
        robot.windmill.hold();
        robot.launcher.stop();

        step = "to the Loading Zone";
        Drive parked = Drive.ARRIVED;
        for (double[] v : Constants.Compete.PARK_VIA) {
            double[] w = forSlot(v, left);
            parked = driveTo(new double[] { w[0], w[1], park[2] }, false);
            if (parked != Drive.ARRIVED) break;
        }
        if (parked == Drive.BUMPED) Debug.warn("compete: could not follow the walls, going straight for the zone");
        if (parked == Drive.ARRIVED || parked == Drive.BUMPED) parked = driveTo(park, false);
        if (parked == Drive.ARRIVED) squeezeAgainstWall();
        robot.stop();
        Pose2D p = robot.localizer.getPose();
        boolean inZone = touchesLoadingZone(p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH), left);
        Debug.log("compete: done at %.0f s, %d shots, drive %s, %s", match.seconds(), shots, parked,
                inZone ? "PARKED in the Loading Zone" : "NOT in the Loading Zone");
        while (opModeIsActive()) {
            telemetry.addData("Done", "%d shots, %s", shots, inZone ? "PARKED" : "NOT in the Loading Zone");
            telemetry.update();
            idle();
        }
    }

    // ---- Shooting ------------------------------------------------------------------------------

    /**
     * The windmill turns only while the wheel is at full speed, and pauses as soon as a ball goes
     * through (a dip of SHOT_DIP_TPS) until the wheel has recovered. If the wheel never gets up to
     * speed in SHOOT_MAX_WAIT_S (low battery), feed anyway. Stops after PRELOADS shots, FEED_MAX_S,
     * or the leave time.
     */
    private void shoot() {
        step = "shooting";
        ElapsedTime feeding = new ElapsedTime(), waiting = new ElapsedTime();
        boolean started = false, inDip = false;
        double lowest = Double.MAX_VALUE;
        while (opModeIsActive() && match.seconds() < leaveAt && shots < Constants.Compete.PRELOADS) {
            robot.updateManual();
            double v = robot.launcher.getVelocityTps(), target = robot.launcher.getTargetTps();
            boolean ready = v >= target - Constants.Launcher.READY_TOLERANCE_TPS;
            if (!started) {
                if (robot.launcher.atSpeed() || waiting.seconds() >= Constants.Windmill.SHOOT_MAX_WAIT_S) {
                    started = true;
                    feeding.reset();
                    Debug.log("compete: feeding at %.0f tps", v);
                }
            } else {
                if (feeding.seconds() > Constants.Compete.FEED_MAX_S) {
                    Debug.warn("compete: feeding time up after %d shots", shots);
                    break;
                }
                if (!inDip && v < target - Constants.Compete.SHOT_DIP_TPS) {
                    inDip = true;               // a ball went through: pause until the wheel recovers
                    lowest = v;
                    waiting.reset();
                } else if (inDip) {
                    lowest = Math.min(lowest, v);
                    if (ready || waiting.seconds() >= Constants.Windmill.SHOOT_MAX_WAIT_S) {
                        inDip = false;
                        shots++;
                        Debug.log("compete: shot %d at %.1f s, wheel dipped to %.0f tps", shots, match.seconds(), lowest);
                    }
                }
                if (inDip) robot.windmill.hold();
                else robot.windmill.run();
            }
            show("%d of %d shots, launcher %.0f / %.0f tps", shots, Constants.Compete.PRELOADS, v, robot.launcher.getTargetTps());
        }
        robot.windmill.hold();
        if (match.seconds() >= leaveAt) Debug.warn("compete: out of time, leaving after %d shots", shots);
    }

    /** Leave for the Loading Zone at PARK_RESERVE_S before the end, or earlier if the trip is long. */
    private void planLeaveTime(double[] park) {
        Pose2D p = robot.localizer.getPose();
        double x = p.getX(DistanceUnit.INCH), y = p.getY(DistanceUnit.INCH);
        double len = 0;
        for (double[] v : parkLegs(park, left)) {
            List<double[]> route = map.route(x, y, v[0], v[1]);
            if (route != null) len += FieldMap.length(x, y, route);
            x = v[0];
            y = v[1];
        }
        double tripS = len / Constants.Compete.PARK_SPEED_IN_PER_S + Constants.Compete.PARK_MARGIN_S;
        leaveAt = Constants.Compete.AUTO_LENGTH_S - Math.max(Constants.Compete.PARK_RESERVE_S, tripS);
        Debug.log("compete: park trip about %.1f s, leave by %.1f s", tripS, leaveAt);
    }

    // ---- Driving -------------------------------------------------------------------------------

    /**
     * Plan a route around the field's obstacles and drive it, recovering from bumps.
     * @param stopAtLeaveTime true: give up when it is time to leave for the Loading Zone.
     */
    private Drive driveTo(double[] target, boolean stopAtLeaveTime) {
        for (int attempt = 0; attempt <= Constants.Compete.MAX_RETRIES; attempt++) {
            Pose2D p = robot.localizer.getPose();
            double x = p.getX(DistanceUnit.INCH), y = p.getY(DistanceUnit.INCH);
            List<double[]> route = map.route(x, y, target[0], target[1]);
            if (route == null) {
                Debug.warn("compete: no route from %.0f, %.0f to %.0f, %.0f", x, y, target[0], target[1]);
                return Drive.UNSAFE;
            }
            Drive d = Drive.ARRIVED;
            for (double[] corner : route) {
                d = follow(corner[0], corner[1], target[2], route, stopAtLeaveTime);
                if (d != Drive.ARRIVED) break;
            }
            if (d != Drive.BUMPED) return d;
            if (attempt < Constants.Compete.MAX_RETRIES) backOff(attempt);
        }
        Debug.warn("compete: still blocked after %d retries", Constants.Compete.MAX_RETRIES);
        return Drive.BUMPED;
    }

    /** Drive to one point with the PathFollower, watching for a stall (pushing against something). */
    private Drive follow(double x, double y, double headingDeg, List<double[]> route, boolean stopAtLeaveTime) {
        Pose2D p = robot.localizer.getPose();
        List<PathFollower.Waypoint> fence = new ArrayList<>();
        for (double[] c : route) fence.add(new PathFollower.Waypoint(c[0], c[1], headingDeg));
        follower.setFenceAround(fence, p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH));
        follower.start(new PathFollower.Waypoint(x, y, headingDeg));
        lastTarget = new double[] { x, y };
        ElapsedTime window = new ElapsedTime();
        double windowDist = Double.NaN;
        PathFollower.Result r = PathFollower.Result.RUNNING;
        while (opModeIsActive() && r == PathFollower.Result.RUNNING) {
            if (stopAtLeaveTime && match.seconds() >= leaveAt) { follower.abort(); return Drive.OUT_OF_TIME; }
            robot.updateManual();
            r = follower.update();
            double togo = follower.distanceToGo();
            if (Double.isNaN(windowDist)) windowDist = togo;
            if (window.seconds() >= Constants.Compete.STALL_WINDOW_S) {
                if (r == PathFollower.Result.RUNNING && togo > Constants.Compete.STALL_FAR_IN
                        && windowDist - togo < Constants.Compete.STALL_MIN_IN) {
                    follower.abort();
                    Debug.warn("compete: stuck %.1f in from %.0f, %.0f", togo, x, y);
                    return Drive.BUMPED;
                }
                window.reset();
                windowDist = togo;
            }
            show("to %.0f, %.0f: %.1f in to go, %s", x, y, togo, r);
        }
        if (!opModeIsActive()) { follower.abort(); return Drive.OUT_OF_TIME; }
        switch (r) {
            case ARRIVED: return Drive.ARRIVED;
            case TIMED_OUT:     // close enough counts; further away something was in the way
                return follower.distanceToGo() <= Constants.Compete.STALL_FAR_IN ? Drive.ARRIVED : Drive.BUMPED;
            case NO_PROGRESS: return Drive.BUMPED;
            default: return Drive.UNSAFE;          // RUNAWAY, OUT_OF_BOUNDS, LOST: do not keep driving
        }
    }

    /** After a bump: back off the way we came, then step sideways to whichever side is free. */
    private void backOff(int attempt) {
        Pose2D p = robot.localizer.getPose();
        double x = p.getX(DistanceUnit.INCH), y = p.getY(DistanceUnit.INCH), h = p.getHeading(AngleUnit.DEGREES);
        // Direction we were driving: toward the point we were heading for when we got stuck.
        double dx = 1, dy = 0;
        double d = Math.hypot(lastTarget[0] - x, lastTarget[1] - y);
        if (d > 0.1) { dx = (lastTarget[0] - x) / d; dy = (lastTarget[1] - y) / d; }
        double back = Constants.Compete.BACK_OFF_IN, side = Constants.Compete.SIDESTEP_IN;
        double bx = x - dx * back, by = y - dy * back;
        if (map.isFree(bx, by)) moveBriefly(bx, by, h, "backing off");
        else { bx = x; by = y; }
        // Alternate sides on each retry, but never into something.
        double sign = attempt % 2 == 0 ? 1 : -1;
        for (int k = 0; k < 2; k++, sign = -sign) {
            double sx = bx - dy * side * sign, sy = by + dx * side * sign;
            if (map.isFree(sx, sy)) { moveBriefly(sx, sy, h, "stepping aside"); return; }
        }
    }

    /** A short move with no stall watch (used by backOff). */
    private void moveBriefly(double x, double y, double headingDeg, String what) {
        step = what;
        Pose2D p = robot.localizer.getPose();
        List<PathFollower.Waypoint> fence = new ArrayList<>();
        fence.add(new PathFollower.Waypoint(x, y, headingDeg));
        follower.setFenceAround(fence, p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH));
        follower.start(new PathFollower.Waypoint(x, y, headingDeg));
        PathFollower.Result r = PathFollower.Result.RUNNING;
        ElapsedTime t = new ElapsedTime();
        while (opModeIsActive() && r == PathFollower.Result.RUNNING && t.seconds() < 2.0) {
            robot.updateManual();
            r = follower.update();
            show("%s: %.1f in to go", what, follower.distanceToGo());
        }
        follower.abort();
    }

    /** Push gently toward our wall (field -x) for a moment, so the robot ends flat against it. */
    private void squeezeAgainstWall() {
        step = "against the wall";
        ElapsedTime t = new ElapsedTime();
        while (opModeIsActive() && t.seconds() < Constants.Compete.WALL_SQUEEZE_S) {
            robot.updateManual();
            double h = robot.localizer.getPose().getHeading(AngleUnit.RADIANS);
            robot.drive.driveFieldCentric(-Constants.Compete.WALL_SQUEEZE_POWER, 0, 0, h, 1.0);
            show("pushing against our wall");
        }
        robot.drive.stop();
        Pose2D p = robot.localizer.getPose();
        Debug.log("compete: against the wall at %.1f, %.1f", p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH));
    }

    // ---- Checks and telemetry ------------------------------------------------------------------

    /** Problems with the plan, for INIT; empty if none. */
    private String checkPlan(double[] start, double[] shoot, double[] park) {
        StringBuilder s = new StringBuilder();
        String b = map.blockedBy(start[0], start[1]);
        if (b != null) s.append("start too close to ").append(b).append(". ");
        b = map.blockedBy(shoot[0], shoot[1]);
        if (b != null) s.append("shooting spot too close to ").append(b).append(". ");
        b = map.blockedBy(park[0], park[1]);
        if (b != null) s.append("park spot too close to ").append(b).append(". ");
        if (!touchesLoadingZone(park[0], park[1], left)) s.append("park spot is not in the Loading Zone. ");
        if (s.length() == 0) {
            if (map.route(start[0], start[1], shoot[0], shoot[1]) == null) s.append("no route to the shooting spot. ");
            double x = shoot[0], y = shoot[1];
            for (double[] v : parkLegs(park, left)) {
                if (map.blockedBy(v[0], v[1]) != null) { s.append("a park way point is too close to ").append(map.blockedBy(v[0], v[1])).append(". "); break; }
                if (map.route(x, y, v[0], v[1]) == null) { s.append("no route to the Loading Zone. "); break; }
                x = v[0];
                y = v[1];
            }
        }
        return s.toString().trim();
    }

    /** The way points to the Loading Zone for this slot, then the park spot itself. */
    static List<double[]> parkLegs(double[] park, boolean left) {
        List<double[]> legs = new ArrayList<>();
        for (double[] v : Constants.Compete.PARK_VIA) legs.add(forSlot(v, left));
        legs.add(park);
        return legs;
    }

    /** True if an 18 in robot centred at (x, y) is at least partly in our Loading Zone (AUTO PARK). */
    static boolean touchesLoadingZone(double x, double y, boolean left) {
        double[] z = FieldMap.forSlot(Constants.Field.LOADING_ZONE, left);
        double h = Constants.Field.ROBOT_HALF_IN;
        return x - h < z[2] && x + h > z[0] && y - h < z[3] && y + h > z[1];
    }

    private void show(String format, Object... args) {
        Pose2D p = robot.localizer.getPose();
        telemetry.addData("Time", "%.1f s  (leave for the Loading Zone by %.1f s)", match.seconds(), leaveAt);
        telemetry.addData("Step", step);
        telemetry.addData("Progress", format, args);
        telemetry.addData("Pose", "%.1f, %.1f, %.0f°", p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH),
                p.getHeading(AngleUnit.DEGREES));
        telemetry.update();
    }
}
