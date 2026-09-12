package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Simple mecanum TeleOp for the goBILDA Strafer Chassis V5 (4 motors, 4 mecanum wheels).
 *
 * Hardware: goBILDA 5203 Yellow Jacket, 312 RPM, 19.2:1 (SKU 5203-2402-0019).
 *           537.7 encoder ticks per output revolution. Wheels driven through bevel gears.
 *
 * Control Hub wiring and Driver Station configuration (Configure Robot -> Control Hub -> Motors):
 *
 *   Port 0 : "front_left"   (type: goBILDA 5202/5203/5204 series)
 *   Port 1 : "front_right"  (type: goBILDA 5202/5203/5204 series)
 *   Port 2 : "back_left"    (type: goBILDA 5202/5203/5204 series)
 *   Port 3 : "back_right"   (type: goBILDA 5202/5203/5204 series)
 *
 * Gamepad 1:
 *   Left stick  Y  -> drive forward / backward
 *   Left stick  X  -> strafe left / right
 *   Right stick X  -> rotate left / right
 *
 * Mecanum wheels only work if the rollers form an "X" when viewed from above.
 * If the robot moves the wrong way, fix the wheel placement first, then adjust the
 * setDirection() calls below.
 */
@TeleOp(name = "Strafer TeleOp", group = "ApexStrykerz")
public class StraferTeleOp extends LinearOpMode {

    private final ElapsedTime runtime = new ElapsedTime();

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    @Override
    public void runOpMode() {
        // Names here must match the Driver Station robot configuration exactly.
        frontLeft  = hardwareMap.get(DcMotor.class, "front_left");
        frontRight = hardwareMap.get(DcMotor.class, "front_right");
        backLeft   = hardwareMap.get(DcMotor.class, "back_left");
        backRight  = hardwareMap.get(DcMotor.class, "back_right");

        // On the Strafer V5 the left and right motors are mirror images of each other,
        // so one side is reversed. Front and back motors on the same side match.
        // Test: put the robot on blocks, push the left stick forward, and watch each wheel.
        //   - All four wheels spin backward  -> swap FORWARD/REVERSE on all four lines.
        //   - One wheel spins the wrong way  -> swap only that wheel.
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        // Hold position when the sticks are released instead of coasting.
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Initialized - press START");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            // Pushing the stick forward gives a negative Y, so flip it.
            double drive  = -gamepad1.left_stick_y;
            double strafe =  gamepad1.left_stick_x;
            double turn   =  gamepad1.right_stick_x;

            // Standard mecanum mixing.
            double frontLeftPower  = drive + strafe + turn;
            double frontRightPower = drive - strafe - turn;
            double backLeftPower   = drive - strafe + turn;
            double backRightPower  = drive + strafe - turn;

            // Scale everything down so no wheel is asked for more than 100%.
            double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
            max = Math.max(max, Math.abs(backLeftPower));
            max = Math.max(max, Math.abs(backRightPower));
            if (max > 1.0) {
                frontLeftPower  /= max;
                frontRightPower /= max;
                backLeftPower   /= max;
                backRightPower  /= max;
            }

            frontLeft.setPower(frontLeftPower);
            frontRight.setPower(frontRightPower);
            backLeft.setPower(backLeftPower);
            backRight.setPower(backRightPower);

            telemetry.addData("Run time", runtime.toString());
            telemetry.addData("Drive / Strafe / Turn", "%4.2f  %4.2f  %4.2f", drive, strafe, turn);
            telemetry.addData("Front L / R", "%4.2f  %4.2f", frontLeftPower, frontRightPower);
            telemetry.addData("Back  L / R", "%4.2f  %4.2f", backLeftPower, backRightPower);
            telemetry.update();
        }
    }
}
