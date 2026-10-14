package org.firstinspires.ftc.teamcode.localization;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/**
 * goBILDA Pinpoint odometry: EMPTY STUB. We don't have the Pinpoint yet, so this does nothing,
 * is never trusted, and the robot reports "MISSING: pinpoint" on the Driver Station. Position
 * comes from DriveEncoderLocalizer instead (Constants.USE_PINPOINT = false).
 *
 * When the Pinpoint arrives, fill this in. The driver is built into SDK v12 as
 * com.qualcomm.hardware.gobilda.GoBildaPinpointDriver, so nothing needs copying in:
 *   1. Mount the two pods. Add the device to the configuration as
 *      "goBILDA Pinpoint Odometry Computer" named Constants.Config.PINPOINT ("pinpoint").
 *   2. Constructor: hw.tryGet(GoBildaPinpointDriver.class, Constants.Config.PINPOINT), then
 *      setOffsets(xMm, yMm, DistanceUnit.MM), setEncoderResolution(pod type),
 *      setEncoderDirections(x, y), resetPosAndIMU(). Put the numbers in Constants.Odometry.
 *   3. update() -> pinpoint.update(); getPose() -> getPosition(); setPose() -> setPosition();
 *      getHeading() -> getHeading(AngleUnit.RADIANS);
 *      isTrusted() -> getDeviceStatus() == GoBildaPinpointDriver.DeviceStatus.READY.
 *   4. Set Constants.USE_PINPOINT = true. Push the robot forward: x must go up. Push it
 *      left: y must go up. Auto Pathed then appears on the Driver Station.
 */
public class PinpointLocalizer implements Localizer {

    public PinpointLocalizer(HardwareMap hw) {
        // Stub: nothing to set up until the hardware arrives.
    }

    @Override
    public void update() {}

    @Override
    public Pose2D getPose() {
        return new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public void setPose(Pose2D pose) {}

    @Override
    public double getHeading() { return 0; }

    @Override
    public boolean isTrusted() { return false; }

    @Override
    public String name() { return "Pinpoint (stub, not installed)"; }
}
