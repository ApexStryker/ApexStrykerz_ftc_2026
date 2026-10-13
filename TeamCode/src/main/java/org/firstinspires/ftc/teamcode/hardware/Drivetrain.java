package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Constants;

/**
 * goBILDA Strafer mecanum drive. The ONE implementation of the mecanum math: every OpMode that
 * drives calls this class.
 *
 * Conventions: axial = forward positive, lateral = strafe RIGHT positive, yaw = turn RIGHT
 * (clockwise) positive. Headings are radians, counter-clockwise positive, as the IMU reports.
 */
public class Drivetrain {

    public final DcMotorEx frontLeft, frontRight, backLeft, backRight;
    /** All four drive motors were found. */
    public final boolean present;
    private final DcMotorEx[] motors;

    public Drivetrain(HardwareMap hw) {
        frontLeft = init(hw, Constants.Config.FRONT_LEFT, Constants.Drive.FRONT_LEFT_DIR);
        frontRight = init(hw, Constants.Config.FRONT_RIGHT, Constants.Drive.FRONT_RIGHT_DIR);
        backLeft = init(hw, Constants.Config.BACK_LEFT, Constants.Drive.BACK_LEFT_DIR);
        backRight = init(hw, Constants.Config.BACK_RIGHT, Constants.Drive.BACK_RIGHT_DIR);
        motors = new DcMotorEx[] { frontLeft, frontRight, backLeft, backRight };
        present = frontLeft != null && frontRight != null && backLeft != null && backRight != null;
    }

    private static DcMotorEx init(HardwareMap hw, String name, DcMotorSimple.Direction dir) {
        DcMotorEx m = hw.tryGet(DcMotorEx.class, name);
        if (m == null) return null;
        m.setDirection(dir);
        m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        m.setPower(0);
        return m;
    }

    /**
     * Standard mecanum mixing, scaled so no wheel exceeds 100%.
     * @return powers in the order front left, front right, back left, back right
     */
    public static double[] mix(double axial, double lateral, double yaw) {
        double fl = axial + lateral + yaw;
        double fr = axial - lateral - yaw;
        double bl = axial - lateral + yaw;
        double br = axial + lateral - yaw;
        double max = Math.max(1.0, Math.max(Math.max(Math.abs(fl), Math.abs(fr)), Math.max(Math.abs(bl), Math.abs(br))));
        return new double[] { fl / max, fr / max, bl / max, br / max };
    }

    /** Drive relative to the robot. scale (0-1) caps the output, e.g. for precision mode. */
    public void driveRobotCentric(double axial, double lateral, double yaw, double scale) {
        double[] p = mix(axial, lateral, yaw);
        setPowers(p[0] * scale, p[1] * scale, p[2] * scale, p[3] * scale);
    }

    /**
     * Drive relative to the field: stick up always moves away from the driver, whatever way the
     * robot faces. headingRad is the robot's heading from the driver's forward, CCW positive.
     */
    public void driveFieldCentric(double forward, double right, double yaw, double headingRad, double scale) {
        // Field vector (x forward, y left), rotated into the robot frame by -heading.
        double x = forward, y = -right;
        double cos = Math.cos(headingRad), sin = Math.sin(headingRad);
        double robotX = x * cos + y * sin;
        double robotY = -x * sin + y * cos;
        driveRobotCentric(robotX, -robotY, yaw, scale);
    }

    public void setPowers(double fl, double fr, double bl, double br) {
        if (frontLeft != null) frontLeft.setPower(fl);
        if (frontRight != null) frontRight.setPower(fr);
        if (backLeft != null) backLeft.setPower(bl);
        if (backRight != null) backRight.setPower(br);
    }

    public void stop() { setPowers(0, 0, 0, 0); }

    /** Encoder counts in the order front left, front right, back left, back right (0 if missing). */
    public int[] getPositions() {
        int[] out = new int[4];
        for (int i = 0; i < 4; i++) out[i] = motors[i] == null ? 0 : motors[i].getCurrentPosition();
        return out;
    }

    /** Average forward travel of all four wheels, inches. */
    public double averageForwardInches() {
        int[] p = getPositions();
        return (p[0] + p[1] + p[2] + p[3]) / 4.0 / Constants.Drive.TICKS_PER_INCH;
    }
}
