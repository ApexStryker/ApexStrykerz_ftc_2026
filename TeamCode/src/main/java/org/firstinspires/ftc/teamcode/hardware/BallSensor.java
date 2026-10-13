package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Constants;

/**
 * REV Color Sensor V3 at the intake mouth ("intake_color"). Optional: without it, present is
 * false, nothing is counted, and opponent NECTAR is not rejected automatically.
 *
 * Reports whether a ball is in front of it and what it is. A new ball shows up as one
 * "arrival" (present going from false to true), which BallPath uses to count.
 */
public class BallSensor {

    public enum Ball { NONE, POLLEN, RED_NECTAR, BLUE_NECTAR, UNKNOWN }

    public final boolean present;
    private final ColorRangeSensor sensor;
    private boolean ballPresent, arrived;
    private Ball ball = Ball.NONE;
    private double distanceCm = Double.NaN, hue = Double.NaN;

    public BallSensor(HardwareMap hw) {
        sensor = hw.tryGet(ColorRangeSensor.class, Constants.Config.INTAKE_COLOR);
        present = sensor != null;
        if (present) sensor.setGain(Constants.BallSensor.GAIN);
    }

    /** Call once per loop. */
    public void update() {
        arrived = false;
        if (!present) return;
        distanceCm = sensor.getDistance(DistanceUnit.CM);
        boolean now = distanceCm < Constants.BallSensor.PRESENT_CM;
        if (now && !ballPresent) arrived = true;
        ballPresent = now;
        if (!now) { ball = Ball.NONE; hue = Double.NaN; return; }
        NormalizedRGBA c = sensor.getNormalizedColors();
        hue = hueDegrees(c.red, c.green, c.blue);
        ball = classify(hue);
    }

    static Ball classify(double hue) {
        if (Double.isNaN(hue)) return Ball.UNKNOWN;
        if (hue >= Constants.BallSensor.POLLEN_HUE_MIN && hue <= Constants.BallSensor.POLLEN_HUE_MAX) return Ball.POLLEN;
        if (hue >= Constants.BallSensor.BLUE_HUE_MIN && hue <= Constants.BallSensor.BLUE_HUE_MAX) return Ball.BLUE_NECTAR;
        if (hue <= Constants.BallSensor.RED_HUE_LOW_MAX || hue >= Constants.BallSensor.RED_HUE_HIGH_MIN) return Ball.RED_NECTAR;
        return Ball.UNKNOWN;
    }

    /** Hue 0-360 from RGB, or NaN for grey. */
    static double hueDegrees(double r, double g, double b) {
        double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        double delta = max - min;
        if (delta <= 1e-6) return Double.NaN;
        double h;
        if (max == r) h = ((g - b) / delta) % 6;
        else if (max == g) h = (b - r) / delta + 2;
        else h = (r - g) / delta + 4;
        h *= 60;
        return h < 0 ? h + 360 : h;
    }

    /** True if this ball is NECTAR of the other alliance (rule G408: do not control it). */
    public static boolean isOpponentNectar(Ball b, Constants.Alliance us) {
        return (us == Constants.Alliance.RED && b == Ball.BLUE_NECTAR)
                || (us == Constants.Alliance.BLUE && b == Ball.RED_NECTAR);
    }

    public boolean ballPresent() { return ballPresent; }
    /** True for the single loop in which a new ball appeared. */
    public boolean ballArrived() { return arrived; }
    public Ball getBall() { return ball; }
    public double getDistanceCm() { return distanceCm; }
    public double getHue() { return hue; }
}
