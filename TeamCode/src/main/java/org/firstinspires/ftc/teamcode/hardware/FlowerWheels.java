package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Constants;

/**
 * The two front lateral wheels that pull POLLEN out of a FLOWER's retrieval opening.
 *
 * Rule G418: only POLLEN may be removed from a FLOWER, and only from the bottom. Never use these
 * to force NECTAR out of a FLOWER; the manual lists that as a STRATEGIC foul.
 */
public class FlowerWheels {

    public final boolean present;
    private final ContinuousServo left, right;
    private boolean running;

    public FlowerWheels(HardwareMap hw) {
        left = new ContinuousServo(hw, Constants.Config.SERVO_LEFT);
        right = new ContinuousServo(hw, Constants.Config.SERVO_RIGHT);
        present = left.present || right.present;
    }

    public void pullIn() {
        running = true;
        left.setPower(Constants.FlowerWheels.LEFT_IN);
        right.setPower(Constants.FlowerWheels.RIGHT_IN);
    }

    /** fraction 0..1 pulls inward at that share of full speed, 0..-1 pushes outward (Ball Path Test). */
    public void drive(double fraction) {
        running = fraction != 0;
        left.setPower(Constants.FlowerWheels.LEFT_IN * fraction);
        right.setPower(Constants.FlowerWheels.RIGHT_IN * fraction);
    }

    public void stop() {
        running = false;
        left.stop();
        right.stop();
    }

    public boolean isRunning() { return running; }
    public boolean leftPresent() { return left.present; }
    public boolean rightPresent() { return right.present; }
    public boolean configuredAsPositional() {
        return left.configuredAsPositional() || right.configuredAsPositional();
    }
}
