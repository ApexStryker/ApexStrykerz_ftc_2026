package org.firstinspires.ftc.teamcode.localization;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.util.Debug;

/**
 * Position from the goBILDA Pinpoint: two unpowered tracking wheels ("pods") plus the
 * Pinpoint's own IMU, which do not slip like the mecanum wheels.
 *
 * Setup (the Pinpoint slides in the deck):
 *   1. Mount the pods and the Pinpoint (sticker side up). X pod rolls forward, Y pod sideways.
 *   2. Driver Hub: Control Hub, I2C bus 1, "goBILDA Pinpoint Odometry Computer", name "pinpoint".
 *   3. Put the pod type, offsets A and B, and directions in Constants.Odometry.
 *   4. Run Pinpoint Test and pass its four checks. Then set Constants.USE_PINPOINT = true.
 *
 * The Pinpoint forgets its settings at every power cycle, so they are sent each time this is
 * built (every OpMode start). It also calibrates its IMU then: the robot must be still.
 * Missing from the configuration: present is false, the pose stays at zero, never trusted.
 */
public class PinpointLocalizer implements Localizer {

    public final boolean present;
    private final GoBildaPinpointDriver pinpoint;
    private GoBildaPinpointDriver.DeviceStatus lastStatus;

    public PinpointLocalizer(HardwareMap hw) {
        pinpoint = hw.tryGet(GoBildaPinpointDriver.class, Constants.Config.PINPOINT);
        present = pinpoint != null;
        if (!present) {
            Debug.warn("Pinpoint: '%s' not in the configuration", Constants.Config.PINPOINT);
            return;
        }
        pinpoint.setOffsets(Constants.Odometry.PINPOINT_X_POD_OFFSET_MM,
                Constants.Odometry.PINPOINT_Y_POD_OFFSET_MM, DistanceUnit.MM);
        pinpoint.setEncoderResolution(Constants.Odometry.PINPOINT_POD);
        pinpoint.setEncoderDirections(Constants.Odometry.PINPOINT_X_DIR, Constants.Odometry.PINPOINT_Y_DIR);
        pinpoint.resetPosAndIMU();   // the robot must be still for about 0.25 s
        Debug.log("Pinpoint: pod %s, offsets A %.1f mm, B %.1f mm, directions %s/%s",
                Constants.Odometry.PINPOINT_POD, Constants.Odometry.PINPOINT_X_POD_OFFSET_MM,
                Constants.Odometry.PINPOINT_Y_POD_OFFSET_MM, Constants.Odometry.PINPOINT_X_DIR,
                Constants.Odometry.PINPOINT_Y_DIR);
    }

    @Override
    public void update() {
        if (!present) return;
        pinpoint.update();
        GoBildaPinpointDriver.DeviceStatus status = pinpoint.getDeviceStatus();
        if (status != lastStatus) {
            if (status == GoBildaPinpointDriver.DeviceStatus.READY) Debug.log("Pinpoint: READY");
            else Debug.warn("Pinpoint: %s (%s)", status, explain(status));
            lastStatus = status;
        }
    }

    @Override
    public Pose2D getPose() {
        return present ? pinpoint.getPosition() : new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public void setPose(Pose2D pose) {
        if (present) pinpoint.setPosition(pose);
    }

    @Override
    public double getHeading() {
        return present ? AngleUnit.normalizeRadians(pinpoint.getHeading(AngleUnit.RADIANS)) : 0;
    }

    @Override
    public boolean isTrusted() {
        return present && pinpoint.getDeviceStatus() == GoBildaPinpointDriver.DeviceStatus.READY;
    }

    @Override
    public String name() {
        return present ? "Pinpoint " + pinpoint.getDeviceStatus() : "Pinpoint (not in configuration)";
    }

    @Override
    public String debugInfo() {
        if (!present) return "Pinpoint not in the configuration";
        return String.format("status %s, raw counts X %d Y %d, %.0f Hz",
                pinpoint.getDeviceStatus(), pinpoint.getEncoderX(), pinpoint.getEncoderY(), pinpoint.getFrequency());
    }

    /** Status in words, matching the light on the Pinpoint. */
    public String statusText() {
        return present ? pinpoint.getDeviceStatus() + ": " + explain(pinpoint.getDeviceStatus()) : "not in the configuration";
    }

    public int rawX() { return present ? pinpoint.getEncoderX() : 0; }
    public int rawY() { return present ? pinpoint.getEncoderY() : 0; }

    /** Zero the position and recalibrate the IMU. Only with the robot standing still. */
    public void resetPosAndIMU() {
        if (!present) return;
        pinpoint.resetPosAndIMU();
        Debug.log("Pinpoint: position and IMU reset");
    }

    private static String explain(GoBildaPinpointDriver.DeviceStatus s) {
        switch (s) {
            case READY: return "green light, working";
            case CALIBRATING: return "red light, keep the robot still";
            case NOT_READY: return "red light, starting up";
            case FAULT_NO_PODS_DETECTED: return "purple light, no pods plugged in";
            case FAULT_X_POD_NOT_DETECTED: return "blue light, X pod not plugged in";
            case FAULT_Y_POD_NOT_DETECTED: return "orange light, Y pod not plugged in";
            case FAULT_IMU_RUNAWAY: return "IMU fault, power-cycle the robot";
            case FAULT_BAD_READ: return "bad read over I2C, check the cable";
            default: return "unknown";
        }
    }
}
