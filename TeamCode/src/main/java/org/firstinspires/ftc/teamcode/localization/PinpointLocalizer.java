package org.firstinspires.ftc.teamcode.localization;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;

/**
 * goBILDA Pinpoint odometry computer. UNTESTED: the hardware is on order.
 *
 * The driver (com.qualcomm.hardware.gobilda.GoBildaPinpointDriver) is built into SDK v12, so no
 * driver file needs copying in. Method names below were checked against the SDK 12.0.0 jar.
 *
 * When the kit arrives:
 *   TODO: mount the pods; add the device to the configuration as "goBILDA Pinpoint Odometry
 *         Computer" named "pinpoint"; set the pod offsets, pod type and encoder directions in
 *         Constants.Odometry; set Constants.USE_PINPOINT = true; check that pushing the robot
 *         forward increases x and pushing it left increases y.
 */
public class PinpointLocalizer implements Localizer {

    private final GoBildaPinpointDriver pinpoint;

    public PinpointLocalizer(HardwareMap hw) {
        pinpoint = hw.tryGet(GoBildaPinpointDriver.class, Constants.Config.PINPOINT);
        if (pinpoint == null) return;
        pinpoint.setOffsets(Constants.Odometry.PINPOINT_X_POD_OFFSET_MM,
                Constants.Odometry.PINPOINT_Y_POD_OFFSET_MM, DistanceUnit.MM);
        pinpoint.setEncoderResolution(Constants.Odometry.PINPOINT_POD);
        pinpoint.setEncoderDirections(Constants.Odometry.PINPOINT_X_DIR, Constants.Odometry.PINPOINT_Y_DIR);
        pinpoint.resetPosAndIMU();   // robot must be still for ~0.25 s while the IMU calibrates
    }

    @Override
    public void update() {
        if (pinpoint != null) pinpoint.update();
    }

    @Override
    public Pose2D getPose() {
        return pinpoint == null ? new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.RADIANS, 0) : pinpoint.getPosition();
    }

    @Override
    public void setPose(Pose2D pose) {
        if (pinpoint != null) pinpoint.setPosition(pose);
    }

    @Override
    public double getHeading() {
        return pinpoint == null ? 0 : AngleUnit.normalizeRadians(pinpoint.getHeading(AngleUnit.RADIANS));
    }

    @Override
    public boolean isTrusted() {
        return pinpoint != null && pinpoint.getDeviceStatus() == GoBildaPinpointDriver.DeviceStatus.READY;
    }

    @Override
    public String name() {
        return pinpoint == null ? "Pinpoint (NOT FOUND)" : "Pinpoint " + pinpoint.getDeviceStatus();
    }
}
