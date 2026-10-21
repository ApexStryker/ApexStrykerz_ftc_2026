package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.localization.Localizer;
import org.firstinspires.ftc.teamcode.util.Debug;

import java.util.List;

/**
 * Drives the robot to waypoints (x, y, heading) using a Localizer, moving and turning at the
 * same time: straight at the target, even diagonally, slowing down as it gets close.
 *
 *   follower.start(waypoint);
 *   while (opModeIsActive() && follower.update() == Result.RUNNING) { ... }
 *
 * Safety: it stops by itself and reports why when the target is reached (ARRIVED), time runs
 * out (TIMED_OUT), the robot gets FURTHER from the target than it has been (RUNAWAY, e.g. a pod
 * direction flipped), it leaves the fence box (OUT_OF_BOUNDS), or the localizer stops being
 * trustworthy (LOST). Numbers live in Constants.Auto.
 *
 * Field frame as everywhere: x forward, y left, heading counter-clockwise, all in inches and
 * degrees from where the localizer was zeroed.
 */
public class PathFollower {

    public enum Result { RUNNING, ARRIVED, TIMED_OUT, RUNAWAY, OUT_OF_BOUNDS, LOST }

    /** A target: x and y in inches, heading in degrees. */
    public static final class Waypoint {
        public final double x, y, headingDeg;
        public Waypoint(double x, double y, double headingDeg) { this.x = x; this.y = y; this.headingDeg = headingDeg; }
        @Override public String toString() { return String.format("(%.0f, %.0f, %.0f°)", x, y, headingDeg); }
    }

    /** What to drive this loop, in the FIELD frame (forward, right) plus turn. */
    public static final class Command {
        public final double forward, right, yaw, distanceIn, headingErrorDeg;
        Command(double forward, double right, double yaw, double distanceIn, double headingErrorDeg) {
            this.forward = forward; this.right = right; this.yaw = yaw;
            this.distanceIn = distanceIn; this.headingErrorDeg = headingErrorDeg;
        }
        public boolean arrived() {
            return distanceIn < Constants.Auto.POSITION_TOLERANCE_IN
                    && Math.abs(headingErrorDeg) < Constants.Auto.HEADING_TOLERANCE_DEG;
        }
    }

    /**
     * The control law, with no hardware so it can be tested on a laptop. Speed grows with the
     * distance (POSITION_KP), capped at PATH_MAX_POWER and never below PATH_MIN_POWER while still
     * away from the target; the direction is straight at the target. Turning is proportional to
     * the heading error (HEADING_KP).
     */
    public static Command compute(double x, double y, double headingDeg, Waypoint target) {
        double ex = target.x - x, ey = target.y - y;
        double dist = Math.hypot(ex, ey);
        double headingErr = AngleUnit.normalizeDegrees(target.headingDeg - headingDeg);
        double forward = 0, right = 0;
        if (dist >= Constants.Auto.POSITION_TOLERANCE_IN) {
            double speed = Range.clip(dist * Constants.Auto.POSITION_KP,
                    Constants.Auto.PATH_MIN_POWER, Constants.Auto.PATH_MAX_POWER);
            forward = ex / dist * speed;
            right = -ey / dist * speed;          // field y is LEFT
        }
        // Heading error positive = still to turn left (counter-clockwise) = negative yaw.
        double yaw = Range.clip(-headingErr * Constants.Auto.HEADING_KP,
                -Constants.Auto.PATH_MAX_POWER, Constants.Auto.PATH_MAX_POWER);
        return new Command(forward, right, yaw, dist, headingErr);
    }

    private final Drivetrain drive;
    private final Localizer localizer;
    private final ElapsedTime timer = new ElapsedTime();
    private Waypoint target;
    private double timeoutS, bestDist;
    private boolean fenced;
    private double fenceMinX, fenceMaxX, fenceMinY, fenceMaxY;
    private Command last;

    public PathFollower(Drivetrain drive, Localizer localizer) {
        this.drive = drive;
        this.localizer = localizer;
    }

    /** Stop if the robot leaves the box around these waypoints by more than FENCE_MARGIN_IN. */
    public void setFenceAround(List<Waypoint> path) {
        fenceMinX = fenceMaxX = 0;
        fenceMinY = fenceMaxY = 0;
        for (Waypoint w : path) {
            fenceMinX = Math.min(fenceMinX, w.x); fenceMaxX = Math.max(fenceMaxX, w.x);
            fenceMinY = Math.min(fenceMinY, w.y); fenceMaxY = Math.max(fenceMaxY, w.y);
        }
        double m = Constants.Auto.FENCE_MARGIN_IN;
        fenceMinX -= m; fenceMaxX += m; fenceMinY -= m; fenceMaxY += m;
        fenced = true;
    }

    public void start(Waypoint w) {
        target = w;
        Pose2D p = localizer.getPose();
        bestDist = Math.hypot(w.x - p.getX(DistanceUnit.INCH), w.y - p.getY(DistanceUnit.INCH));
        timeoutS = Constants.Auto.WAYPOINT_TIMEOUT_S + bestDist * Constants.Auto.PATH_TIMEOUT_PER_IN_S;
        timer.reset();
        Debug.log("path: heading for %s, %.1f in away", w, bestDist);
    }

    /** Once per loop, after the localizer has updated. Drives, or stops and says why. */
    public Result update() {
        if (target == null) return Result.ARRIVED;
        if (!localizer.isTrusted()) return finish(Result.LOST);
        Pose2D p = localizer.getPose();
        double x = p.getX(DistanceUnit.INCH), y = p.getY(DistanceUnit.INCH);
        last = compute(x, y, p.getHeading(AngleUnit.DEGREES), target);

        if (last.arrived()) return finish(Result.ARRIVED);
        if (timer.seconds() > timeoutS) return finish(Result.TIMED_OUT);
        bestDist = Math.min(bestDist, last.distanceIn);
        if (last.distanceIn > bestDist + Constants.Auto.RUNAWAY_IN) return finish(Result.RUNAWAY);
        if (fenced && (x < fenceMinX || x > fenceMaxX || y < fenceMinY || y > fenceMaxY)) return finish(Result.OUT_OF_BOUNDS);

        drive.driveFieldCentric(last.forward, last.right, last.yaw, localizer.getHeading(), 1.0);
        return Result.RUNNING;
    }

    /** Stop the wheels now (e.g. a button was pressed). */
    public void abort() {
        drive.stop();
        target = null;
    }

    public Waypoint getTarget() { return target; }
    public double distanceToGo() { return last == null ? 0 : last.distanceIn; }
    public double headingToGo() { return last == null ? 0 : last.headingErrorDeg; }
    public double elapsed() { return timer.seconds(); }

    private Result finish(Result r) {
        drive.stop();
        if (r == Result.ARRIVED) Debug.log("path: reached %s in %.1f s", target, timer.seconds());
        else Debug.warn("path: stopped going to %s: %s (%.1f in to go)", target, r, last == null ? -1 : last.distanceIn);
        target = null;
        return r;
    }
}
