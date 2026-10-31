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

    /**
     * Works out the pod offsets from a spin on the spot. While the robot turns left by theta
     * (radians, from heading 0 at the zeroed start), offsets that are wrong by dA and dB make the
     * Pinpoint report a position of
     *   x = -dA sin(theta) + dB (cos(theta) - 1),   y = -dA (1 - cos(theta)) + dB sin(theta),
     * because the X pod, dA to the left of the centre, rolls backwards as the robot turns left,
     * and the Y pod, dB in front of it, rolls to the left. add() every loop; dA() and dB() are the
     * least-squares fit, in the same units as x and y. New offset = current offset + the fit.
     */
    public static final class OffsetFit {
        private double s11, s12, s22, r1, r2;
        private int n;

        public void add(double thetaRad, double x, double y) {
            double s = Math.sin(thetaRad), c = Math.cos(thetaRad);
            double a1x = -s, a1y = -(1 - c), a2x = c - 1, a2y = s;
            s11 += a1x * a1x + a1y * a1y;
            s12 += a1x * a2x + a1y * a2y;
            s22 += a2x * a2x + a2y * a2y;
            r1 += a1x * x + a1y * y;
            r2 += a2x * x + a2y * y;
            n++;
        }

        private double det() { return s11 * s22 - s12 * s12; }
        /** Enough turning to tell dA and dB apart. */
        public boolean ok() { return n > 10 && Math.abs(det()) > 1e-6; }
        public double dA() { return ok() ? (r1 * s22 - r2 * s12) / det() : 0; }
        public double dB() { return ok() ? (r2 * s11 - r1 * s12) / det() : 0; }
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
