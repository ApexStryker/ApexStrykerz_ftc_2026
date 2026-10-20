package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * One place for every Driver Station configuration name and tuning number.
 * No other file may contain a configuration-name string literal or a magic number.
 *
 * Search for "TODO(Wed)" to find every value that must be checked on the robot.
 */
public final class Constants {
    private Constants() {}

    /** Which alliance we are on; picked with the D-pad during INIT, this is the default. */
    public enum Alliance { RED, BLUE }

    public static final Alliance DEFAULT_ALLIANCE = Alliance.RED;

    /**
     * false: position comes from the drive encoders. Set true once the goBILDA Pinpoint is
     * mounted, its settings in Constants.Odometry are measured, and Pinpoint Test passes all
     * four checks. true also makes "Auto Pathed" appear on the Driver Station.
     */
    public static final boolean USE_PINPOINT = false;

    // =========================================================================================
    /**
     * Driver Station configuration "Apex-1", read from the robot on 2026-10-12 and checked with
     * Config Name Test. Every motor and servo moved on that date.
     */
    public static final class Config {
        private Config() {}

        // Control Hub motor ports 0-3: goBILDA 5203 312 RPM drive
        public static final String FRONT_LEFT = "front_left";     // CH motor 0
        public static final String FRONT_RIGHT = "front_right";   // CH motor 1
        public static final String BACK_LEFT = "back_left";       // CH motor 2
        public static final String BACK_RIGHT = "back_right";     // CH motor 3

        // Expansion Hub motors.
        // TODO(Wed): confirm. On 2026-10-12 port 0 spun freely (launcher-like) and port 1 drew
        // 3-5 A (intake-like), the reverse of these names.
        public static final String INTAKE = "intake_mtr";         // EH motor 0
        public static final String LAUNCHER = "launcher_mtr";     // EH motor 1

        // Control Hub servo ports 0-2: goBILDA 2000-0025-0003 dual-mode servos.
        // TODO(Wed): program all three to continuous mode and change their configuration type to
        // "Continuous Rotation Servo". The code works with either type until then.
        // TODO(Wed): confirm left/right; port 1 moved the LEFT wheel before the plugs were fixed.
        public static final String SERVO_LEFT = "servo_left";     // CH servo 0, front left flower wheel
        public static final String SERVO_RIGHT = "servo_right";   // CH servo 1, front right flower wheel
        public static final String WINDMILL = "servo_windmill";   // CH servo 2, 4-blade indexer

        // Sensors
        public static final String IMU = "imu";                   // Control Hub built-in BHI260AP
        // TODO(Wed): add to the configuration when mounted (all optional; code runs without them).
        public static final String INTAKE_COLOR = "intake_color"; // REV Color Sensor V3, CH I2C
        public static final String WINDMILL_INDEX = "windmill_index"; // REV magnetic limit switch, digital
        public static final String PINPOINT = "pinpoint";         // goBILDA Pinpoint, I2C (on order)
        public static final String WEBCAM = "webcam";             // Logitech C615, Control Hub USB
    }

    // =========================================================================================
    /** How the Control Hub is mounted. Wrong values make field-centric driving go the wrong way. */
    public static final class Hub {
        private Hub() {}

        // TODO(Wed): set from the robot. Logo = the side with the REV logo; USB = the side the
        // USB ports face. Check with Motor Direction Test: turning the robot left by hand must
        // INCREASE the yaw reading.
        public static final RevHubOrientationOnRobot.LogoFacingDirection LOGO =
                RevHubOrientationOnRobot.LogoFacingDirection.UP;
        public static final RevHubOrientationOnRobot.UsbFacingDirection USB =
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
    }

    // =========================================================================================
    public static final class Drive {
        private Drive() {}

        // Every wheel must roll the robot FORWARD on positive power. FRONT_LEFT_DIR checked on
        // the robot 2026-10-19 (was REVERSE, ran backward). TODO: confirm the other three with
        // Motor Direction Test. (2026-10-19: back_right's motor turned but its gears were not
        // meshing, so the wheel stood still; a mechanical fix, not a code one.)
        public static final DcMotorSimple.Direction FRONT_LEFT_DIR = DcMotorSimple.Direction.FORWARD;
        public static final DcMotorSimple.Direction FRONT_RIGHT_DIR = DcMotorSimple.Direction.FORWARD;
        public static final DcMotorSimple.Direction BACK_LEFT_DIR = DcMotorSimple.Direction.REVERSE;
        public static final DcMotorSimple.Direction BACK_RIGHT_DIR = DcMotorSimple.Direction.FORWARD;

        /** goBILDA 5203 19.2:1 encoder counts per output revolution. */
        public static final double TICKS_PER_REV = 537.7;
        /** GripForce mecanum wheel, 104 mm. */
        public static final double WHEEL_DIAMETER_IN = 104.0 / 25.4;
        public static final double TICKS_PER_INCH = TICKS_PER_REV / (Math.PI * WHEEL_DIAMETER_IN);
        /**
         * Sideways travel per tick differs from forward travel on mecanum. Calibrate: strafe a
         * measured distance, then set this to (measured / reported).
         */
        public static final double LATERAL_MULTIPLIER = 1.0;

        /**
         * false = robot-centric (stick up = the way the robot faces), exactly like the old Strafer
         * TeleOp. Field-centric needs a correct Constants.Hub, which is not checked yet: on
         * 2026-10-19 field-centric drove wrongly while Strafer TeleOp drove right.
         * TODO: set true once Motor Direction Test shows yaw going UP when turning left.
         */
        public static final boolean FIELD_CENTRIC_DEFAULT = false;
        /** Output while the driver holds the precision trigger. */
        public static final double PRECISION_SCALE = 0.4;
        /** Output cap while Flower mode is active, for gentle lining up. */
        public static final double FLOWER_SCALE = 0.35;
        public static final double TEST_POWER = 0.3;
    }

    // =========================================================================================
    /** Debugging (util/Debug.java): what counts as worth a line in the robot log. */
    public static final class Debug {
        private Debug() {}

        /** Loops slower than this are logged; normal is well under 20 ms. */
        public static final double SLOW_LOOP_MS = 60;
        /** Battery below this is logged as a warning: a sagging battery can reset the hubs. */
        public static final double LOW_BATTERY_V = 11.5;
        /** How often to read the battery; each read takes a moment, so not every loop. */
        public static final double BATTERY_CHECK_MS = 1000;
    }

    // =========================================================================================
    /** The webcam, mounted looking up at the AprilTags under the HIVE cells. */
    public static final class Camera {
        private Camera() {}

        public static final int WIDTH = 640, HEIGHT = 480;
        /**
         * Lens calibration at 640x480, in pixels. The SDK has none for our Logitech C615, so
         * these are its built-in C920 values (a very similar lens). Tag IDs are exact; distances
         * and angles are approximate until we calibrate the C615. TODO: calibrate and replace.
         */
        public static final double FX = 622.001, FY = 622.001, CX = 319.803, CY = 241.251;

        /** HIVE cell AprilTags (manual section 9): family 36h11, 3.25 in, facing the tiles. */
        public static final double TAG_SIZE_IN = 3.25;
        public static final int RED_FIRST_ID = 30, RED_LAST_ID = 37;
        public static final int BLUE_FIRST_ID = 38, BLUE_LAST_ID = 45;
    }

    // =========================================================================================
    public static final class Odometry {
        private Odometry() {}

        /** Drive-encoder pose is not trusted after this much travel since the last setPose. */
        public static final double MAX_TRUSTED_PATH_IN = 144.0;

        // ---- goBILDA Pinpoint (arrived 2026-10-19; see the Pinpoint slides in the deck) ----
        /**
         * Which goBILDA pods are in the kit. TODO: check the box: 4-Bar pod or Swingarm pod.
         * Wrong type = every distance off by the same factor (tape-measure check finds it).
         */
        public static final GoBildaPinpointDriver.GoBildaOdometryPods PINPOINT_POD =
                GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD;
        /**
         * "A": how far SIDEWAYS the X (forward-rolling) pod is from the robot's centre, mm.
         * Left of centre +, right of centre -. TODO: measure once the pods are mounted.
         */
        public static final double PINPOINT_X_POD_OFFSET_MM = 0.0;
        /**
         * "B": how far FORWARD the Y (sideways-rolling) pod is from the robot's centre, mm.
         * In front of centre +, behind -. TODO: measure once the pods are mounted.
         */
        public static final double PINPOINT_Y_POD_OFFSET_MM = 0.0;
        /** Pushing the robot forward must make x go UP, pushing it left must make y go UP. TODO: check with Pinpoint Test. */
        public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_X_DIR =
                GoBildaPinpointDriver.EncoderDirection.FORWARD;
        public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_Y_DIR =
                GoBildaPinpointDriver.EncoderDirection.FORWARD;
    }

    // =========================================================================================
    public static final class Launcher {
        private Launcher() {}

        // TODO(Wed): if the wheel spins backward, flip this.
        public static final DcMotorSimple.Direction DIRECTION = DcMotorSimple.Direction.FORWARD;
        /** Encoder counts per wheel revolution, only used to show RPM. TODO(Wed): read motor label. */
        public static final double TICKS_PER_REV = 28.0;

        /**
         * Speed presets in encoder ticks per second, picked with the operator's D-pad.
         * 2026-10-12: about 1320 tps at 50% power, so full power is roughly 2600 tps.
         * TODO(Wed): tune with Launcher Tuner at the near and far shooting spots.
         */
        public static final double[] PRESETS_TPS = { 1800, 2300 };
        public static final String[] PRESET_NAMES = { "NEAR", "FAR" };

        // ---- Ball Path Test: adjusting the speed live with the D-pad ----
        public static final double COARSE_STEP_TPS = 100;
        public static final double FINE_STEP_TPS = 25;
        /** Top of the adjustable range: about full speed of a 6000 RPM motor (6000 x 28 / 60). */
        public static final double MAX_TPS = 2800;

        /** Ready when within this many tps of target... */
        public static final double READY_TOLERANCE_TPS = 60;
        /** ...continuously for this long. */
        public static final double READY_HOLD_MS = 120;
        /** A shot is detected when speed dips this far below target after a feed. */
        public static final double SHOT_DIP_TPS = 150;
        /** Give up waiting for a shot this long after a feed (empty windmill pocket). */
        public static final double SHOT_TIMEOUT_MS = 700;

        /** Custom velocity PIDF. false = SDK defaults. Tune with Launcher Tuner. */
        public static final boolean USE_CUSTOM_PIDF = false;
        public static final double P = 10, I = 3, D = 0, F = 12;

        /**
         * Distance (inches, e.g. from an AprilTag) to speed (tps), linear interpolation between
         * points. EMPTY on purpose: fill on the practice field, sorted by distance.
         * Until then the presets are used.
         */
        public static final double[] TABLE_DISTANCE_IN = {};
        public static final double[] TABLE_SPEED_TPS = {};
    }

    // =========================================================================================
    public static final class Intake {
        private Intake() {}

        // TODO(Wed): if positive power spits balls out, flip this.
        public static final DcMotorSimple.Direction DIRECTION = DcMotorSimple.Direction.FORWARD;
        public static final double IN_POWER = 1.0;
        public static final double OUT_POWER = -0.8;
        /** Gentle push while firing, to keep the queue pressed against the windmill. */
        public static final double FEED_POWER = 0.35;
        /** Ball Path Test: the right stick does nothing until pushed this far from the middle. */
        public static final double STICK_DEADZONE = 0.15;
    }

    // =========================================================================================
    public static final class Windmill {
        private Windmill() {}

        /**
         * Continuous-servo speed for one feed step. Its SIGN is the feed direction (the windmill
         * turns clockwise to feed). Negative since 2026-10-17: positive turned it the wrong way
         * on the robot. REVERSE_SPEED must always have the opposite sign.
         */
        public static final double FEED_SPEED = -0.6;
        public static final double REVERSE_SPEED = 0.4;
        /** Without the index switch: run this long for one quarter turn. TODO(Wed): tune. */
        public static final double STEP_MS = 350;
        /** With the index switch: stop the step if no blade arrives in this time. */
        public static final double STEP_TIMEOUT_MS = 900;
        /** REV magnetic limit switch reads false when a magnet is present. */
        public static final boolean INDEX_ACTIVE_LOW = true;
        /**
         * Ball Path Test: when shooting, the windmill waits for the launcher to reach speed, but
         * starts anyway after this long (e.g. if the speed preset is too high to reach).
         */
        public static final double SHOOT_MAX_WAIT_S = 2.0;
    }

    // =========================================================================================
    public static final class FlowerWheels {
        private FlowerWheels() {}

        /** Speeds that pull a ball inward. TODO(Wed): negate either if it pushes outward. */
        public static final double LEFT_IN = 1.0;
        public static final double RIGHT_IN = -1.0;
    }

    // =========================================================================================
    public static final class Servos {
        private Servos() {}

        /** goBILDA dual-mode servo in continuous mode expects 1000-2000 us. */
        public static final double CONTINUOUS_PWM_LOW_US = 1000;
        public static final double CONTINUOUS_PWM_HIGH_US = 2000;
    }

    // =========================================================================================
    public static final class BallSensor {
        private BallSensor() {}

        /** A ball is present when the color sensor reads closer than this. TODO(Wed): tune. */
        public static final double PRESENT_CM = 4.0;
        public static final float GAIN = 8f;
        /** Hue windows, degrees. TODO(Wed): check against real balls under event lighting. */
        public static final double POLLEN_HUE_MIN = 35, POLLEN_HUE_MAX = 90;
        public static final double BLUE_HUE_MIN = 180, BLUE_HUE_MAX = 260;
        public static final double RED_HUE_LOW_MAX = 20, RED_HUE_HIGH_MIN = 330;
        /** How long to run the intake backward to spit out opponent Nectar (rule G408). */
        public static final double REJECT_MS = 700;
        /** Rule G407: never control more than this many balls. */
        public static final int MAX_BALLS = 4;
    }

    // =========================================================================================
    public static final class Auto {
        private Auto() {}

        /** Shoot the 4 preloads before leaving. TODO(Wed): enable once a start spot is chosen. */
        public static final boolean FIRE_PRELOADS = false;
        public static final int PRELOADS = 4;
        public static final int PRELOAD_PRESET = 0;
        public static final double FIRE_TIMEOUT_S = 8.0;

        /** LEAVE: drive straight off the wall this far. */
        public static final double LEAVE_DISTANCE_IN = 24.0;
        public static final double DRIVE_POWER = 0.4;
        public static final double DRIVE_TIMEOUT_S = 4.0;
        /** Heading hold: power per degree of heading error. */
        public static final double HEADING_KP = 0.02;

        // ---- Turns (turnLeft / turnRight in AutoBase), using the IMU ----
        /** Turn power per degree still to go. */
        public static final double TURN_KP = 0.015;
        public static final double TURN_MAX_POWER = 0.5;
        /** Below this the robot stalls instead of turning. */
        public static final double TURN_MIN_POWER = 0.12;
        public static final double TURN_TOLERANCE_DEG = 2.0;
        public static final double TURN_TIMEOUT_S = 3.0;

        // ---- Auto Pathed (only with USE_PINPOINT) ----
        /** Waypoints {x in, y in, heading deg}, field frame relative to the start pose. */
        public static final double[][] PATH = { { 24, 0, 0 }, { 24, 24, 0 } };
        public static final double POSITION_KP = 0.06;
        public static final double POSITION_TOLERANCE_IN = 1.0;
        public static final double HEADING_TOLERANCE_DEG = 2.0;
        public static final double PATH_MAX_POWER = 0.5;
        public static final double WAYPOINT_TIMEOUT_S = 4.0;
    }
}
