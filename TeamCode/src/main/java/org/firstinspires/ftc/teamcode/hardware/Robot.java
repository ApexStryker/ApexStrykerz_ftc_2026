package org.firstinspires.ftc.teamcode.hardware;

import android.graphics.Color;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.localization.DriveEncoderLocalizer;
import org.firstinspires.ftc.teamcode.localization.Localizer;
import org.firstinspires.ftc.teamcode.localization.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.util.Debug;

import java.util.List;

/**
 * The whole robot, built once per OpMode: new Robot(hardwareMap).
 *
 * - Every device is optional. A missing one is reported by reportMissing() and its subsystem
 *   does nothing, so the same code runs on a half-built robot.
 * - Bulk caching is on for both hubs, which cuts a loop from ~40 ms to under 10 ms.
 * - Call update() once at the top of every loop.
 * - Debugging: monitor() (part of update) logs low battery, slow loops, launcher ready and
 *   localizer trust to the robot log (tag "Apex"); addDebugTelemetry() is the debug page.
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
    /** The Pinpoint is in the configuration (whether or not USE_PINPOINT uses it yet). */
    public final boolean pinpointConfigured;
    private final List<VoltageSensor> batteries;

    // ---- Debug monitoring state ----
    private final ElapsedTime loopTimer = new ElapsedTime(), batteryTimer = new ElapsedTime(), slowLogTimer = new ElapsedTime();
    private double loopMs, worstLoopMs, avgLoopMs, batteryVolts;
    private int loops;
    private boolean batteryLow, lastLauncherReady, lastTrusted = true;

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

        imu = initImu(hw);

        // Position from the drive encoders and IMU until the Pinpoint is set up and switched on.
        pinpointConfigured = hw.tryGet(GoBildaPinpointDriver.class, Constants.Config.PINPOINT) != null;
        localizer = Constants.USE_PINPOINT
                ? new PinpointLocalizer(hw)
                : new DriveEncoderLocalizer(drive, imu);

        batteries = hw.getAll(VoltageSensor.class);
        batteryVolts = readBattery();
        logStartup();
    }

    /** The Control Hub IMU, set up for how the hub is mounted (Constants.Hub); null if not configured. */
    public static IMU initImu(HardwareMap hw) {
        IMU found = hw.tryGet(IMU.class, Constants.Config.IMU);
        if (found != null) {
            found.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(Constants.Hub.LOGO, Constants.Hub.USB)));
            found.resetYaw();
        }
        return found;
    }

    /** Once per loop, before anything reads the pose or a sensor. */
    public void update() {
        localizer.update();
        ballPath.update();
        monitor();
    }

    /**
     * Once per loop in OpModes that run the ball path by hand (Main TeleOp, Ball Path Test):
     * like update() but without BallPath, which would fight the operator's controls.
     */
    public void updateManual() {
        localizer.update();
        ballSensor.update();
        launcher.update();
        monitor();
    }

    /**
     * Debug bookkeeping, once per loop (update() calls it; OpModes that skip update() call it
     * themselves). Logs only when something changes, never every loop.
     */
    public void monitor() {
        loopMs = loopTimer.milliseconds();
        loopTimer.reset();
        if (loops > 0) {   // the first loop includes start-up time
            worstLoopMs = Math.max(worstLoopMs, loopMs);
            avgLoopMs = loops == 1 ? loopMs : avgLoopMs * 0.95 + loopMs * 0.05;
            if (loopMs > Constants.Debug.SLOW_LOOP_MS && slowLogTimer.seconds() > 1) {
                Debug.warn("slow loop: %.0f ms (normal under %.0f)", loopMs, Constants.Debug.SLOW_LOOP_MS);
                slowLogTimer.reset();
            }
        }
        loops++;

        if (batteryTimer.milliseconds() >= Constants.Debug.BATTERY_CHECK_MS) {
            batteryTimer.reset();
            batteryVolts = readBattery();
            if (!batteryLow && batteryVolts > 0 && batteryVolts < Constants.Debug.LOW_BATTERY_V) {
                batteryLow = true;
                Debug.warn("battery low: %.2f V", batteryVolts);
            } else if (batteryLow && batteryVolts > Constants.Debug.LOW_BATTERY_V + 0.3) {
                batteryLow = false;
                Debug.log("battery recovered: %.2f V", batteryVolts);
            }
        }

        boolean ready = launcher.atSpeed();
        if (ready != lastLauncherReady) {
            Debug.log(ready ? "launcher at speed: %.0f tps" : "launcher off speed: %.0f tps", launcher.getVelocityTps());
            lastLauncherReady = ready;
        }

        boolean trusted = localizer.isTrusted();
        if (trusted != lastTrusted) {
            if (trusted) Debug.log("localizer trusted again: %s", localizer.name());
            else Debug.warn("localizer not trusted: %s", localizer.name());
            lastTrusted = trusted;
        }
    }

    /** Lowest reading across both hubs, volts; 0 if none can be read. */
    private double readBattery() {
        double min = 0;
        for (VoltageSensor v : batteries) {
            double volts = v.getVoltage();
            if (volts > 0 && (min == 0 || volts < min)) min = volts;
        }
        return min;
    }

    private void logStartup() {
        StringBuilder missing = new StringBuilder();
        if (!drive.present) missing.append(" drive-motor");
        if (imu == null) missing.append(" ").append(Constants.Config.IMU);
        if (!intake.present) missing.append(" ").append(Constants.Config.INTAKE);
        if (!launcher.present) missing.append(" ").append(Constants.Config.LAUNCHER);
        if (!windmill.present) missing.append(" ").append(Constants.Config.WINDMILL);
        if (!flower.leftPresent()) missing.append(" ").append(Constants.Config.SERVO_LEFT);
        if (!flower.rightPresent()) missing.append(" ").append(Constants.Config.SERVO_RIGHT);
        Debug.log("robot built: %d hubs, battery %.2f V, localizer %s, missing:%s",
                hubs.size(), batteryVolts, localizer.name(), missing.length() == 0 ? " none" : missing.toString());
    }

    public double getLoopMs() { return loopMs; }
    public double getBatteryVolts() { return batteryVolts; }

    /**
     * The debug page: every part's state on one Driver Station screen. Reads motor currents,
     * which costs a few milliseconds per loop, so show it only when asked for (SHARE).
     */
    public void addDebugTelemetry(Telemetry t) {
        t.addLine("DEBUG PAGE (gamepad 1 SHARE to go back)");
        t.addData("Loop", "%.0f ms  (average %.0f, worst %.0f)", loopMs, avgLoopMs, worstLoopMs);
        t.addData("Battery", "%.2f V%s", batteryVolts, batteryLow ? "  LOW" : "");
        double[] p = drive.getPowers();
        int[] c = drive.getPositions();
        t.addData("Drive power", "FL %+.2f  FR %+.2f  BL %+.2f  BR %+.2f", p[0], p[1], p[2], p[3]);
        t.addData("Drive counts", "FL %d  FR %d  BL %d  BR %d", c[0], c[1], c[2], c[3]);
        t.addData("Intake", "power %+.2f  current %.2f A", intake.getPower(), intake.getCurrentAmps());
        t.addData("Launcher", "%s  target %.0f  actual %.0f tps  %s",
                launcher.isEnabled() ? "on" : "off", launcher.getTargetTps(), launcher.getVelocityTps(),
                launcher.atSpeed() ? "AT SPEED" : "");
        t.addData("Windmill", "%s  steps %d%s%s", windmill.isStepping() ? "stepping" : windmill.isReversing() ? "reversing" : "-",
                windmill.getStepsDone(), windmill.lastStepTimedOut() ? "  last step TIMED OUT" : "",
                windmill.hasIndexSwitch ? "  (index switch)" : "  (timed)");
        t.addData("Side wheels", flower.isRunning() ? "running" : "stopped");
        t.addData("BallPath", "%s %s  balls %s  shots %d  empty %d", ballPath.getMode(), ballPath.getFireStep(),
                ballPath.countKnown() ? String.valueOf(ballPath.getBallCount()) : "?", ballPath.getShotsFired(), ballPath.getEmptyFeeds());
        if (ballSensor.present) {
            t.addData("Ball sensor", "%s  hue %.0f  %.1f cm", ballSensor.getBall(), ballSensor.getHue(), ballSensor.getDistanceCm());
        }
        Pose2D pose = localizer.getPose();
        t.addData("Position", "x %.1f  y %.1f in  heading %.1f°  %s  %s", pose.getX(DistanceUnit.INCH), pose.getY(DistanceUnit.INCH),
                pose.getHeading(AngleUnit.DEGREES), localizer.name(), localizer.isTrusted() ? "" : "(not trusted)");
        String detail = localizer.debugInfo();
        if (!detail.isEmpty()) t.addData("Localizer", detail);
        reportMissing(t);
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
        if (!Constants.USE_PINPOINT) {
            t.addLine(pinpointConfigured
                    ? "pinpoint configured, not in use yet (USE_PINPOINT = false)"
                    : "MISSING: " + Constants.Config.PINPOINT + " (position from drive encoders)");
        }
        if (!localizer.isTrusted()) t.addLine("localizer not trusted: " + localizer.name());
    }
}
