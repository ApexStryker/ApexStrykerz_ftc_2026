package org.firstinspires.ftc.teamcode;

/**
 * One place for every Driver Station configuration name and tuning number.
 * No other file should contain a configuration-name string literal.
 */
public final class Constants {
    private Constants() {}

    /**
     * Driver Station configuration "Apex-1", read from the robot on 2026-10-12 and checked with
     * the Config Name Test OpMode. Every motor and servo moved on that date.
     */
    public static final class Config {
        private Config() {}

        // ---- Control Hub, motor ports 0-3: goBILDA 5203 312 RPM drive ----
        public static final String FRONT_LEFT = "front_left";     // CH motor 0
        public static final String FRONT_RIGHT = "front_right";   // CH motor 1
        public static final String BACK_LEFT = "back_left";       // CH motor 2
        public static final String BACK_RIGHT = "back_right";     // CH motor 3

        // ---- Expansion Hub motors ----
        // TODO: confirm on the robot. Current draw on 2026-10-12 suggested port 0 spins freely
        // (launcher-like) and port 1 works hard (intake-like), the reverse of these names.
        public static final String INTAKE = "intake_mtr";         // EH motor 0
        public static final String LAUNCHER = "launcher_mtr";     // EH motor 1

        // ---- Control Hub servo ports 0-2: goBILDA 2000-0025-0003 dual-mode servos ----
        // TODO: confirm left/right. On 2026-10-12 the servo on port 1 ("servo_right") moved the
        // LEFT front lateral wheel before the cables were re-seated; swap names or plugs if still so.
        public static final String SERVO_LEFT = "servo_left";     // CH servo 0
        public static final String SERVO_RIGHT = "servo_right";   // CH servo 1
        public static final String WINDMILL = "servo_windmill";   // CH servo 2

        // ---- Sensors ----
        public static final String IMU = "imu";                   // Control Hub built-in BHI260AP
    }

    // Motor directions are deliberately not here yet. They get set from a MotorDirectionTest run
    // on the rebuilt robot (spec step 4); StraferTeleOp still holds the original master values.
}
