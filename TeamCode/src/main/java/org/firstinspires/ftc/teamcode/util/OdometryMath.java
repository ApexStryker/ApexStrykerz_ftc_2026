package org.firstinspires.ftc.teamcode.util;

import org.firstinspires.ftc.teamcode.Constants;

/**
 * The judging behind Odometry Check: pure maths, no hardware, so it can be tested on a laptop.
 * Every limit comes from Constants.Odometry.
 */
public final class OdometryMath {

    private OdometryMath() {}

    /** Outcome of a push-by-hand direction test. */
    public enum Push {
        PASS("PASS"),
        TOO_SHORT("push further"),
        WRONG_WAY("counts went the wrong way: flip this pod's direction in Constants.Odometry"),
        SWAPPED("the other axis moved: X and Y pods swapped (cables or ports), or the pod is turned 90°");

        public final String advice;
        Push(String advice) { this.advice = advice; }
    }

    /**
     * Judge a push. along = movement in the direction pushed (x for a forward push, y for a left
     * push), across = movement in the other direction. Both in inches, from zero.
     */
    public static Push judgePush(double along, double across) {
        double moved = Math.hypot(along, across);
        if (moved < Constants.Odometry.PUSH_MIN_IN) return Push.TOO_SHORT;
        if (Math.abs(across) > Math.abs(along)) return Push.SWAPPED;
        if (along < 0) return Push.WRONG_WAY;
        if (Math.abs(across) > Constants.Odometry.PUSH_MAX_CROSS * along) return Push.SWAPPED;
        return Push.PASS;
    }

    /** Stays-put test: drift in inches and degrees over STILL_TEST_S. */
    public static boolean judgeStill(double driftIn, double driftDeg) {
        return driftIn <= Constants.Odometry.STILL_MAX_DRIFT_IN && Math.abs(driftDeg) <= Constants.Odometry.STILL_MAX_DRIFT_DEG;
    }

    /**
     * Spin test. If an offset (A or B) is wrong by e, the reported position traces a circle of
     * radius e while the robot spins on the spot, so the furthest it gets from the start is 2e.
     * Returns the estimated offset error in mm from that furthest distance in inches.
     */
    public static double offsetErrorMm(double maxExcursionIn) {
        return maxExcursionIn / 2.0 * 25.4;
    }

    public static boolean judgeSpin(double maxExcursionIn, double pinpointTurnDeg, double imuTurnDeg) {
        return maxExcursionIn <= Constants.Odometry.SPIN_MAX_EXCURSION_IN
                && Math.abs(pinpointTurnDeg - imuTurnDeg) <= Constants.Odometry.SPIN_MAX_IMU_DIFF_DEG;
    }

    /**
     * Add up a heading that wraps at +-180, so whole turns can be counted.
     * @param previous last raw heading, degrees (-180..180)
     * @param now      this raw heading, degrees (-180..180)
     * @return the change, degrees, taking the short way round
     */
    public static double headingChange(double previous, double now) {
        double d = now - previous;
        while (d > 180) d -= 360;
        while (d < -180) d += 360;
        return d;
    }
}
