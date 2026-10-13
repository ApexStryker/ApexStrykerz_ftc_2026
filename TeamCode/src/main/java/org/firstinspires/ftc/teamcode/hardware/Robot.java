package org.firstinspires.ftc.teamcode.hardware;

import android.graphics.Color;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.localization.DriveEncoderLocalizer;
import org.firstinspires.ftc.teamcode.localization.Localizer;
import org.firstinspires.ftc.teamcode.localization.PinpointLocalizer;

import java.util.List;

/**
 * The whole robot, built once per OpMode: new Robot(hardwareMap).
 *
 * - Every device is optional. A missing one is reported by reportMissing() and its subsystem
 *   does nothing, so the same code runs on a half-built robot.
 * - Bulk caching is on for both hubs, which cuts a loop from ~40 ms to under 10 ms.
 * - Call update() once at the top of every loop.
 */
public class Robot {

    public final List<LynxModule> hubs;
    public final Drivetrain drive;
    public final Intake intake;
    public final Launcher launcher;
    public final Windmill windmill;
    public final FlowerWheels flower;
    public final BallSensor ballSensor;
    public final BallPath ballPath;
    /** null if the IMU is missing; field-centric driving then falls back to robot-centric. */
    public final IMU imu;
    public final Localizer localizer;

    public Robot(HardwareMap hw) {
        hubs = hw.getAll(LynxModule.class);
        for (LynxModule hub : hubs) hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);

        drive = new Drivetrain(hw);
        intake = new Intake(hw);
        launcher = new Launcher(hw);
        windmill = new Windmill(hw);
        flower = new FlowerWheels(hw);
        ballSensor = new BallSensor(hw);
        ballPath = new BallPath(intake, windmill, launcher, flower, ballSensor);

        IMU found = hw.tryGet(IMU.class, Constants.Config.IMU);
        if (found != null) {
            found.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(Constants.Hub.LOGO, Constants.Hub.USB)));
            found.resetYaw();
        }
        imu = found;

        localizer = Constants.USE_PINPOINT
                ? new PinpointLocalizer(hw)
                : new DriveEncoderLocalizer(drive, imu);
    }

    /** Once per loop, before anything reads the pose or a sensor. */
    public void update() {
        localizer.update();
        ballPath.update();
    }

    /** Everything off. Called at the end of every OpMode. */
    public void stop() {
        drive.stop();
        ballPath.stopAll();
    }

    public void setPose(double xIn, double yIn, double headingDeg) {
        localizer.setPose(new Pose2D(DistanceUnit.INCH, xIn, yIn, AngleUnit.DEGREES, headingDeg));
    }

    /** Both hub status LEDs, e.g. green when the launcher is ready. */
    public void setHubLeds(int color) {
        for (LynxModule hub : hubs) hub.setConstant(color);
    }

    public static final int LED_READY = Color.GREEN, LED_BUSY = Color.YELLOW, LED_IDLE = Color.BLUE;

    /** One telemetry line per missing device, so the team sees at a glance what is not wired. */
    public void reportMissing(Telemetry t) {
        if (!drive.present) t.addLine("MISSING: one or more drive motors");
        if (imu == null) t.addLine("MISSING: imu (field-centric disabled)");
        if (!intake.present) t.addLine("MISSING: " + Constants.Config.INTAKE);
        if (!launcher.present) t.addLine("MISSING: " + Constants.Config.LAUNCHER);
        if (!windmill.present) t.addLine("MISSING: " + Constants.Config.WINDMILL);
        if (!flower.leftPresent()) t.addLine("MISSING: " + Constants.Config.SERVO_LEFT);
        if (!flower.rightPresent()) t.addLine("MISSING: " + Constants.Config.SERVO_RIGHT);
        if (!ballSensor.present) t.addLine("not fitted: " + Constants.Config.INTAKE_COLOR + " (no ball counting)");
        if (windmill.present && !windmill.hasIndexSwitch)
            t.addLine("not fitted: " + Constants.Config.WINDMILL_INDEX + " (timed windmill steps)");
        if (windmill.configuredAsPositional() || flower.configuredAsPositional())
            t.addLine("NOTE: change servo config type to Continuous Rotation Servo");
        if (!localizer.isTrusted()) t.addLine("localizer not trusted: " + localizer.name());
    }
}
