package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * Gamepad 1, the driver. One copy, used by Main TeleOp and Ball Path Test.
 *
 *   Left stick            drive and strafe
 *   Right stick X         rotate
 *   Hold LEFT TRIGGER     precision (40% speed)
 *   Hold LEFT BUMPER      robot-centric instead (only matters once field-centric is on)
 *   OPTIONS               re-zero field-centric (point the robot away from you first)
 *
 * Robot-centric (stick up = the way the robot faces) until Constants.Drive.FIELD_CENTRIC_DEFAULT
 * is turned back on after the IMU check.
 */
public class DriverControls {

    private final Robot robot;
    private double driverZero;
    private boolean fieldCentric;
    private double scale;

    public DriverControls(Robot robot) {
        this.robot = robot;
        driverZero = robot.localizer.getHeading();
    }

    /** Once per loop. prev = the gamepad as it was last loop. */
    public void update(Gamepad g, Gamepad prev) {
        if (g.options && !prev.options) driverZero = robot.localizer.getHeading();
        double forward = -g.left_stick_y;
        double right = g.left_stick_x;
        double turn = g.right_stick_x;
        scale = g.left_trigger > 0.5 ? Constants.Drive.PRECISION_SCALE : 1.0;

        fieldCentric = Constants.Drive.FIELD_CENTRIC_DEFAULT && robot.imu != null && !g.left_bumper;
        if (fieldCentric) {
            double heading = AngleUnit.normalizeRadians(robot.localizer.getHeading() - driverZero);
            robot.drive.driveFieldCentric(forward, right, turn, heading, scale);
        } else {
            robot.drive.driveRobotCentric(forward, right, turn, scale);
        }
    }

    public void addTelemetry(Telemetry t) {
        t.addData("Drive", "%s%s", fieldCentric ? "FIELD" : "ROBOT", scale < 1 ? String.format(" x%.2f", scale) : "");
    }
}
