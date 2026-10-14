package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.hardware.Drivetrain;

/**
 * Drive-only fallback: robot-centric mecanum on gamepad 1, nothing else. Known-good and kept on
 * purpose, for when the full robot misbehaves and you just need to move the chassis.
 *
 * Motor names, directions and the mecanum math all come from Drivetrain and Constants, so this
 * drives exactly like Main TeleOp in robot-centric mode.
 *
 * Gamepad 1: left stick Y drive, left stick X strafe, right stick X rotate.
 */
@TeleOp(name = "Strafer TeleOp", group = "ApexStrykerz")
public class StraferTeleOp extends LinearOpMode {

    @Override
    public void runOpMode() {
        Drivetrain drive = new Drivetrain(hardwareMap);
        ElapsedTime runtime = new ElapsedTime();

        telemetry.addData("Status", drive.present ? "Initialized - press START" : "MISSING drive motor(s)");
        telemetry.update();
        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            double axial = -gamepad1.left_stick_y;   // stick up is negative, so flip it
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;
            drive.driveRobotCentric(axial, lateral, yaw, 1.0);

            double[] p = Drivetrain.mix(axial, lateral, yaw);
            telemetry.addData("Run time", runtime.toString());
            telemetry.addData("Drive / Strafe / Turn", "%4.2f  %4.2f  %4.2f", axial, lateral, yaw);
            telemetry.addData("Front L / R", "%4.2f  %4.2f", p[0], p[1]);
            telemetry.addData("Back  L / R", "%4.2f  %4.2f", p[2], p[3]);
            telemetry.update();
        }
        drive.stop();
    }
}
