package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * Drive two servos with the two joysticks on gamepad 1.
 *
 * Control Hub wiring and Driver Station configuration (Configure Robot -> Control Hub -> Servos):
 *
 *   Servo port 0 : "servo0"   (type: Servo)
 *   Servo port 1 : "servo1"   (type: Servo)
 *
 * Gamepad 1:
 *   Left stick  up / down  -> moves servo0
 *   Right stick up / down  -> moves servo1
 *
 * Pushing a stick moves its servo; the further you push, the faster it moves.
 * Let go and the servo holds where it is.
 */
@TeleOp(name = "Two Servo TeleOp", group = "ApexStrykerz")
public class TwoServoTeleOp extends LinearOpMode {

    // How far a servo moves per second with the stick pushed all the way.
    // 0.5 means a full 0-to-1 sweep takes 2 seconds.
    private static final double SPEED = 0.5;

    // Ignore tiny stick wobble around the center.
    private static final double DEADZONE = 0.05;

    @Override
    public void runOpMode() {
        Servo servo0 = hardwareMap.get(Servo.class, "servo0");
        Servo servo1 = hardwareMap.get(Servo.class, "servo1");

        telemetry.addData("Status", "Initialized - press START");
        telemetry.update();

        waitForStart();

        // Start both servos in the middle of their range.
        double position0 = 0.5;
        double position1 = 0.5;
        servo0.setPosition(position0);
        servo1.setPosition(position1);

        ElapsedTime loopTimer = new ElapsedTime();

        while (opModeIsActive()) {
            // Seconds since the last loop, so movement speed doesn't depend on loop rate.
            double dt = loopTimer.seconds();
            loopTimer.reset();

            // Pushing a stick up gives a negative number, so flip it: up = positive.
            double stick0 = -gamepad1.left_stick_y;
            double stick1 = -gamepad1.right_stick_y;

            if (Math.abs(stick0) > DEADZONE) {
                position0 += stick0 * SPEED * dt;
            }
            if (Math.abs(stick1) > DEADZONE) {
                position1 += stick1 * SPEED * dt;
            }

            // A servo position must stay between 0 and 1.
            position0 = Range.clip(position0, 0.0, 1.0);
            position1 = Range.clip(position1, 0.0, 1.0);

            servo0.setPosition(position0);
            servo1.setPosition(position1);

            telemetry.addData("servo0 (left stick)", "%.2f", position0);
            telemetry.addData("servo1 (right stick)", "%.2f", position1);
            telemetry.update();
        }
    }
}
