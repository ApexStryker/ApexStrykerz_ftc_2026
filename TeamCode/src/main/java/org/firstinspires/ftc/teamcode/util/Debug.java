package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.util.RobotLog;

/**
 * Debugging for the whole stack, in two parts.
 *
 * 1. EVENT LOG. Parts write one line to the robot log when something happens: BallPath mode
 *    changes and shots, launcher at speed, windmill step timeouts, Pinpoint status, localizer
 *    trust, missing devices, low battery, slow loops. All lines use the tag "Apex". Read them:
 *      adb logcat -s Apex                       (live, laptop on the robot's Wi-Fi)
 *      bash tools/robot-sync.sh --logs-only     (saved into robot-sync/logs/)
 *    The Control Hub also keeps a copy per OpMode run in FIRST/matchlogs on the hub.
 *
 * 2. DEBUG PAGE. In Main TeleOp, Ball Path Test and Pinpoint Test, press SHARE on gamepad 1 to
 *    switch the Driver Station screen to Robot.addDebugTelemetry(): every motor, servo and
 *    sensor value at once. Press SHARE again to go back.
 *
 * Use log() for normal events and warn() for anything that needs a look. Never log every loop:
 * log when something CHANGES, or the log fills up and the interesting line scrolls away.
 */
public final class Debug {

    public static final String TAG = "Apex";

    private Debug() {}

    public static void log(String format, Object... args) {
        RobotLog.ii(TAG, format, args);
    }

    public static void warn(String format, Object... args) {
        RobotLog.ww(TAG, format, args);
    }
}
