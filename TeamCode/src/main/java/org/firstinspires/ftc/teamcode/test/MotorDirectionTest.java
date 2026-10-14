package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Drivetrain;

/**
 * Motor Direction Test: spins each drive wheel on its own so you can check that positive power
 * rolls the robot FORWARD, and checks the IMU mounting.
 *
 * PUT THE ROBOT ON BLOCKS.
 *
 * Gamepad 1 (hold):
 *   D-pad UP     front_left alone        D-pad RIGHT  front_right alone
 *   D-pad LEFT   back_left alone         D-pad DOWN   back_right alone
 *   CROSS (A)    all four forward        CIRCLE (B)   strafe right      RIGHT BUMPER  turn right
 *
 * For each wheel: it must turn so it would roll the robot FORWARD, and its encoder count must go
 * UP. If the wheel turns backward, flip its *_DIR in Constants.Drive. If the wheel is right but
 * the count goes DOWN, the encoder cable is on the wrong motor port.
 *
 * IMU: turn the robot to the LEFT by hand; yaw must go UP. If not, fix Constants.Hub.
 */
@TeleOp(name = "Motor Direction Test", group = "Test")
public class MotorDirectionTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Drivetrain drive = new Drivetrain(hardwareMap);
        IMU imu = hardwareMap.tryGet(IMU.class, Constants.Config.IMU);
        if (imu != null) {
            imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(Constants.Hub.LOGO, Constants.Hub.USB)));
            imu.resetYaw();
        }
        String[] names = { Constants.Config.FRONT_LEFT, Constants.Config.FRONT_RIGHT,
                Constants.Config.BACK_LEFT, Constants.Config.BACK_RIGHT };

        telemetry.addLine("Motor Direction Test. ROBOT ON BLOCKS.");
        telemetry.addLine(drive.present ? "All four drive motors found." : "MISSING a drive motor!");
        telemetry.update();
        waitForStart();

        int[] start = drive.getPositions();
        String active = "none";
        while (opModeIsActive()) {
            double p = Constants.Drive.TEST_POWER;
            double fl = 0, fr = 0, bl = 0, br = 0;
            if (gamepad1.dpad_up) { fl = p; active = names[0]; }
            else if (gamepad1.dpad_right) { fr = p; active = names[1]; }
            else if (gamepad1.dpad_left) { bl = p; active = names[2]; }
            else if (gamepad1.dpad_down) { br = p; active = names[3]; }
            else if (gamepad1.cross) { double[] m = Drivetrain.mix(p, 0, 0); fl = m[0]; fr = m[1]; bl = m[2]; br = m[3]; active = "all forward"; }
            else if (gamepad1.circle) { double[] m = Drivetrain.mix(0, p, 0); fl = m[0]; fr = m[1]; bl = m[2]; br = m[3]; active = "strafe right"; }
            else if (gamepad1.right_bumper) { double[] m = Drivetrain.mix(0, 0, p); fl = m[0]; fr = m[1]; bl = m[2]; br = m[3]; active = "turn right"; }
            else { start = drive.getPositions(); active = "none"; }
            drive.setPowers(fl, fr, bl, br);

            int[] now = drive.getPositions();
            double[] cmd = { fl, fr, bl, br };
            telemetry.addData("Spinning", active);
            for (int i = 0; i < 4; i++) {
                int delta = now[i] - start[i];
                String check = cmd[i] == 0 ? "" : (Math.signum(delta) == Math.signum(cmd[i]) ? "  encoder OK"
                        : (delta == 0 ? "  encoder not moving?" : "  ENCODER BACKWARD"));
                telemetry.addData(names[i], "power %+.2f  counts %+6d%s", cmd[i], delta, check);
            }
            if (imu != null) {
                telemetry.addData("IMU yaw", "%.1f°  (turn robot LEFT by hand: must go UP)",
                        imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            } else {
                telemetry.addLine("IMU missing from configuration");
            }
            telemetry.addLine("Each wheel must roll the robot FORWARD. Wrong? Flip its *_DIR in Constants.Drive.");
            telemetry.update();
        }
        drive.stop();
    }
}
