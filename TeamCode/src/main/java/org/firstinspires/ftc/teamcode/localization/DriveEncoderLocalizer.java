package org.firstinspires.ftc.teamcode.localization;

import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Drivetrain;

/**
 * Position from the four drive encoders, heading from the Control Hub IMU. Works today with no
 * extra hardware.
 *
 * - Heading always comes from the IMU. Heading from mecanum wheel slip is unusable.
 * - Each loop's motion is integrated with the pose exponential, not straight-line (Euler)
 *   steps, which drift badly while turning.
 * - Mecanum wheels slip, so the error grows without limit. isTrusted() turns false after
 *   MAX_TRUSTED_PATH_IN of travel since the last setPose().
 */
public class DriveEncoderLocalizer implements Localizer {

    private final Drivetrain drive;
    private final IMU imu;

    private double x, y;               // inches, field frame
    private double headingOffset;      // field heading = IMU yaw + offset
    private double heading;
    private double lastImuYaw;
    private int[] lastTicks;
    private double pathSinceReset;

    public DriveEncoderLocalizer(Drivetrain drive, IMU imu) {
        this.drive = drive;
        this.imu = imu;
        lastTicks = drive.getPositions();
        lastImuYaw = imuYaw();
        heading = 0;
        headingOffset = -lastImuYaw;
    }

    @Override
    public void update() {
        int[] t = drive.getPositions();
        double dFl = t[0] - lastTicks[0], dFr = t[1] - lastTicks[1];
        double dBl = t[2] - lastTicks[2], dBr = t[3] - lastTicks[3];
        lastTicks = t;

        // Mecanum forward kinematics, robot frame (x forward, y left), inches.
        double perTick = 1.0 / Constants.Drive.TICKS_PER_INCH;
        double dx = (dFl + dFr + dBl + dBr) / 4.0 * perTick;
        double dRight = (dFl - dFr - dBl + dBr) / 4.0 * perTick * Constants.Drive.LATERAL_MULTIPLIER;
        double dy = -dRight;

        double yaw = imuYaw();
        double dTheta = AngleUnit.normalizeRadians(yaw - lastImuYaw);
        lastImuYaw = yaw;

        // Pose exponential: exact for constant-curvature motion over the loop.
        double a, b;
        if (Math.abs(dTheta) < 1e-6) {
            a = 1.0 - dTheta * dTheta / 6.0;
            b = dTheta / 2.0;
        } else {
            a = Math.sin(dTheta) / dTheta;
            b = (1.0 - Math.cos(dTheta)) / dTheta;
        }
        double localX = a * dx - b * dy;
        double localY = b * dx + a * dy;

        double cos = Math.cos(heading), sin = Math.sin(heading);
        x += localX * cos - localY * sin;
        y += localX * sin + localY * cos;
        heading = AngleUnit.normalizeRadians(yaw + headingOffset);
        pathSinceReset += Math.hypot(dx, dy);
    }

    private double imuYaw() {
        return imu == null ? 0 : imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
    }

    @Override
    public Pose2D getPose() {
        return new Pose2D(DistanceUnit.INCH, x, y, AngleUnit.RADIANS, heading);
    }

    @Override
    public void setPose(Pose2D pose) {
        x = pose.getX(DistanceUnit.INCH);
        y = pose.getY(DistanceUnit.INCH);
        double h = pose.getHeading(AngleUnit.RADIANS);
        lastImuYaw = imuYaw();
        headingOffset = h - lastImuYaw;
        heading = AngleUnit.normalizeRadians(h);
        lastTicks = drive.getPositions();
        pathSinceReset = 0;
    }

    @Override
    public double getHeading() { return heading; }

    @Override
    public boolean isTrusted() {
        return imu != null && drive.present && pathSinceReset < Constants.Odometry.MAX_TRUSTED_PATH_IN;
    }

    @Override
    public String name() { return "drive encoders + IMU"; }

    @Override
    public String debugInfo() {
        return String.format("%.0f in driven since last reset (trusted up to %.0f)%s", pathSinceReset,
                Constants.Odometry.MAX_TRUSTED_PATH_IN, imu == null ? ", NO IMU" : "");
    }
}
