package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * Drive one servo and one motor with the two joysticks on gamepad 1.
 *
 * Driver Station configuration (Configure Robot):
 *
 *   Servo port 0 : "servo0"      (type: Servo)
 *   Motor port 0 : "front_left"  (type: goBILDA 5202/5203/5204 series)
 *
 * The code only uses the names; the configuration decides which port each is on.
 *
 * Gamepad 1:
 *   Left stick  up / down  -> moves servo0. Let go and it holds its position.
 *   Right stick up / down  -> runs front_left forward / backward. Let go and it stops.
 */
@TeleOp(name = "Servo + Motor TeleOp", group = "ApexStrykerz")
public class ServoMotorTeleOp extends LinearOpMode {

    // How far the servo moves per second with the stick pushed all the way.
    // 0.5 means a full 0-to-1 sweep takes 2 seconds.
    private static final double SERVO_SPEED = 0.5;

    // Ignore tiny stick wobble around the center.
    private static final double DEADZONE = 0.05;

    // Top motor power, from 0 to 1. Start gentle while testing a new mechanism.
    private static final double MAX_MOTOR_POWER = 0.6;

    @Override
    public void runOpMode() {
        Servo servo0 = hardwareMap.get(Servo.class, "servo0");
        DcMotor motor = hardwareMap.get(DcMotor.class, "front_left");

        // Hold position when the stick is released instead of coasting.
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        telemetry.addData("Status", "Initialized - press START");
        telemetry.update();

        waitForStart();

        // Start the servo in the middle of its range.
        double servoPosition = 0.5;
        servo0.setPosition(servoPosition);

        ElapsedTime loopTimer = new ElapsedTime();

        while (opModeIsActive()) {
            // Seconds since the last loop, so servo speed doesn't depend on loop rate.
            double dt = loopTimer.seconds();
            loopTimer.reset();

            // Pushing a stick up gives a negative number, so flip it: up = positive.
            double servoStick = -gamepad1.left_stick_y;
            double motorStick = -gamepad1.right_stick_y;

            // Servo: the stick sets how fast the position changes.
            if (Math.abs(servoStick) > DEADZONE) {
                servoPosition += servoStick * SERVO_SPEED * dt;
            }
            servoPosition = Range.clip(servoPosition, 0.0, 1.0);
            servo0.setPosition(servoPosition);

            // Motor: the stick sets the power directly.
            double motorPower = 0.0;
            if (Math.abs(motorStick) > DEADZONE) {
                motorPower = motorStick * MAX_MOTOR_POWER;
            }
            motor.setPower(motorPower);

            telemetry.addData("servo0 position (left stick)", "%.2f", servoPosition);
            telemetry.addData("front_left power (right stick)", "%.2f", motorPower);
            telemetry.addData("front_left encoder ticks", motor.getCurrentPosition());
            telemetry.update();
        }
    }
}
