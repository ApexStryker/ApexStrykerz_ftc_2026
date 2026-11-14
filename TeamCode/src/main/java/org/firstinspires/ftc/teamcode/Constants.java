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
    public static final boolean USE_PINPOINT = true;

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

        // Set from the robot 2026-10-31: hub on its edge, logo to the right, USB ports to the front.
        // Check with Motor Direction Test: turning the robot left by hand must INCREASE the yaw.
        public static final RevHubOrientationOnRobot.LogoFacingDirection LOGO =
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
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
        /**
         * The Pinpoint zeroes its position when it finishes the reset it does at every OpMode
         * start (about 0.25 s). A start pose set before then is wiped: 2026-11-07, START was
         * pressed 0.5 s after INIT and Auto Compete thought it was in the corner (0, 0) and did
         * not drive. setPose waits until the reset is this old and the Pinpoint is READY...
         */
        public static final double PINPOINT_RESET_SETTLE_MS = 400;
        /** ...but never longer than this, and setPose checks the position took. */
        public static final double PINPOINT_READY_WAIT_MS = 1500;

        // ---- goBILDA Pinpoint (arrived 2026-10-19; see the Pinpoint slides in the deck) ----
        /**
         * Which goBILDA pods we have: the 4-Bar Odometry Pack (3203-3110-0002), confirmed
         * 2026-10-21. Wrong type = every distance off by the same factor (tape-measure check).
         * Installing: docs/pinpoint-install.md.
         */
        public static final GoBildaPinpointDriver.GoBildaOdometryPods PINPOINT_POD =
                GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD;
        /**
         * "A": how far SIDEWAYS the X (forward-rolling) pod is from the robot's centre, mm.
         * Left of centre +, right of centre -. Measured by the Odometry Check spin 2026-10-31.
         */
        public static final double PINPOINT_X_POD_OFFSET_MM = 300.0;
        /**
         * "B": how far FORWARD the Y (sideways-rolling) pod is from the robot's centre, mm.
         * In front of centre +, behind -. Measured by the Odometry Check spin 2026-10-31.
         */
        public static final double PINPOINT_Y_POD_OFFSET_MM = -152.0;
        /** Pushing the robot forward must make x go UP, pushing it left must make y go UP. TODO: check with Pinpoint Test. */
        public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_X_DIR =
                GoBildaPinpointDriver.EncoderDirection.REVERSED;   // Odometry Check 2026-10-31: forward push read -10.5 in
        public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_Y_DIR =
                GoBildaPinpointDriver.EncoderDirection.REVERSED;   // Odometry Check 2026-10-31: left push read -12.7 in

        // ---- Odometry Check (pass limits; goBILDA's guide gives the spin and return numbers) ----
        /** Stays-put test: how long to watch, and the most drift allowed. */
        public static final double STILL_TEST_S = 10.0;
        public static final double STILL_MAX_DRIFT_IN = 0.2;
        public static final double STILL_MAX_DRIFT_DEG = 0.5;
        /** Push-by-hand direction tests: push at least this far. */
        public static final double PUSH_MIN_IN = 6.0;
        /** Sideways movement allowed during a push, as a share of the push. */
        public static final double PUSH_MAX_CROSS = 0.35;
        /** Spin test: whole turns left, power, and the time limit. */
        public static final double SPIN_TURNS = 2.0;
        public static final double SPIN_POWER = 0.3;
        public static final double SPIN_TIMEOUT_S = 20.0;
        /** Spin test: x and y may wander this far from the start (goBILDA: about 4 in). */
        public static final double SPIN_MAX_EXCURSION_IN = 4.0;
        /** Spin test: Pinpoint heading and Control Hub IMU heading must agree this closely. */
        public static final double SPIN_MAX_IMU_DIFF_DEG = 2.0;

        // ---- Odometry Path Test ----
        /** Side of the square and diamond paths: 2 ft, so they fit beside the hive in the middle. */
        public static final double TEST_SQUARE_IN = 24.0;
        /** Side of the field loop (fits the 12 ft field from a start near a corner). */
        public static final double TEST_FIELD_LOOP_IN = 96.0;
        /** Back at the start, the robot should be this close to the tape mark (goBILDA: about 1/2 in). */
        public static final double RETURN_MAX_ERROR_IN = 0.5;
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
         * 2026-11-02: NEAR set to 1250, the speed the team picked with the D-pad and shot at.
         * 2026-11-04: NEAR raised to 1325 (+75) after the first Auto Compete runs, then 1310 (-15).
         * 2026-11-07: NEAR 1295, picked in Launcher Tuner with our own speed loop (the wheel now
         * really holds the set speed; the old hub control swung about 1220-1380 around 1310).
         * 2026-11-09: NEAR 1290 (-5): shots sometimes went slightly long.
         * TODO: tune FAR with Launcher Tuner at the far shooting spot.
         */
        public static final double[] PRESETS_TPS = { 1290, 2300 };
        public static final String[] PRESET_NAMES = { "NEAR", "FAR" };
        /**
         * TeleOp: can the operator nudge the launcher speed with D-pad up / down (STEP_TPS a press)?
         * 2026-11-07: ON. TeleOp starts at the NEAR preset, the same speed as Auto Compete.
         */
        public static final boolean OPERATOR_DPAD_SPEED = true;
        /**
         * TeleOp: circle = other preset, D-pad right/left = windmill speed. 2026-11-04: OFF, the
         * windmill stays at FEED_SPEED. Set true to tune again.
         */
        public static final boolean OPERATOR_SPEED_CHANGES = false;

        /** Operator D-pad up / down: launcher speed step (100 until 2026-11-02, 80 until 2026-11-07, then 25). */
        public static final double STEP_TPS = 10;
        /** Top of the adjustable range: about full speed of a 6000 RPM motor (6000 x 28 / 60). */
        public static final double MAX_TPS = 2800;

        /**
         * Ready when within this many tps of target... 60 until 2026-11-07, then 30 once our loop
         * held the speed, so the next ball waits out the short overshoot after each shot.
         * 2026-11-09: 25. Readings come in steps of 20; at target 1290 a reading of 1320 (the tail
         * of the overshoot) was exactly 30 and still counted as ready. 25 always accepts the two
         * readings either side of the target and not the next one up.
         */
        public static final double READY_TOLERANCE_TPS = 25;
        /** ...continuously for this long. */
        public static final double READY_HOLD_MS = 120;
        /** A shot is detected when speed dips this far below target after a feed. */
        public static final double SHOT_DIP_TPS = 150;
        /** Give up waiting for a shot this long after a feed (empty windmill pocket). */
        public static final double SHOT_TIMEOUT_MS = 700;

        /**
         * true = our own speed loop in hardware/Launcher (battery-corrected base power, correction
         * toward the target, full-power boost after each ball). false = the hub's built-in velocity
         * PIDF below, as before 2026-11-04 (that one swung about 1220-1380 around 1310).
         */
        public static final boolean OWN_SPEED_LOOP = true;
        /**
         * Our loop's gains. Power = (KS + KV x target) x NOMINAL_VOLTS / battery + KP x error + KI x
         * (sum of error x seconds), with error = target - actual in tps.
         * KV, KS: base power per tps and to overcome friction, at NOMINAL_VOLTS. 2026-11-07: from
         * Launcher Tuner "Calibrate" (874 tps at 0.35 power, 1666 tps at 0.65, battery 12.7 V).
         * KP, KI: 2026-11-07, checked in Launcher Tuner: holds within one encoder reading (+/-10
         * tps) of target, back to speed about 0.1-0.2 s after each ball, about +50 overshoot
         * for 0.3 s after.
         */
        public static final double KV = 0.000398, KS = 0.0232;
        public static final double KP = 0.002, KI = 0.002;
        /** The most the KI term may add or remove, as motor power (stops wind-up). */
        public static final double I_LIMIT = 0.1;
        /** Full power while the wheel is more than this slow (right after a ball), to recover fast. */
        public static final double BOOST_BELOW_TPS = 80;
        /** The battery voltage KV and KS are measured at; base power scales by NOMINAL / battery. */
        public static final double NOMINAL_VOLTS = 12.0;
        /** Read the battery this often (a reading costs a few ms). */
        public static final double VOLTS_READ_MS = 100;
        /** Launcher Tuner calibration: the two fixed powers, and how long to run each. */
        public static final double CAL_POWER_LOW = 0.35, CAL_POWER_HIGH = 0.65, CAL_STEP_S = 2.5;

        /** The hub's velocity PIDF, only when OWN_SPEED_LOOP is false. false = SDK defaults. */
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
         * 2026-11-02: -0.6 -> -0.75 (25% faster, the team's request).
         */
        public static final double FEED_SPEED = -0.75;
        public static final double REVERSE_SPEED = 0.4;
        /**
         * Without the index switch: run this long for one quarter turn. Was 350 at -0.6; cut by
         * the same 25% when FEED_SPEED went to -0.75. TODO: tune.
         */
        public static final double STEP_MS = 280;
        /** Operator D-pad right / left: feed speed step, and the slowest / fastest it can go. */
        public static final double SPEED_STEP = 0.05;
        public static final double MIN_SPEED = 0.1, MAX_SPEED = 1.0;
        /** With the index switch: stop the step if no blade arrives in this time. */
        public static final double STEP_TIMEOUT_MS = 900;
        /** REV magnetic limit switch reads false when a magnet is present. */
        public static final boolean INDEX_ACTIVE_LOW = true;
        /**
         * Ball Path Test: when shooting, the windmill waits for the launcher to reach speed, but
         * starts anyway after this long (e.g. if the speed preset is too high to reach).
         */
        public static final double SHOOT_MAX_WAIT_S = 2.0;
        /**
         * While shooting, pause the windmill when the wheel dips this far below target (a ball
         * just went through) until it is back within Launcher.READY_TOLERANCE_TPS, so the next
         * ball never goes in on a slowed wheel and falls short. Used by TeleOp and Auto Compete.
         */
        public static final double PAUSE_DIP_TPS = 100;
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

        // ---- Path following (hardware/PathFollower: Auto Pathed, Odometry Path Test) ----
        /**
         * ARENA COORDINATES for Auto Pathed, in inches and degrees. Stand behind OUR wall
         * (the Driver Station side) looking at the field:
         *   (0, 0)  = the field corner on your RIGHT, where our wall meets the right-hand wall
         *   x       = inches AWAY from our wall (0 to 144)
         *   y       = inches to the LEFT from the right-hand wall (0 to 144)
         *   heading = 0° faces away from our wall, 90° faces left, -90° faces right
         * Positions are the CENTRE of the robot, so nothing closer than ARENA_WALL_GAP_IN to a wall.
         */
        public static final double ARENA_SIZE_IN = 144.0;
        public static final double ARENA_WALL_GAP_IN = 9.0;
        /**
         * Where the robot stands at START {x, y, heading}, in arena coordinates. Mark it with tape.
         * TODO: measure the team's real start spot (robot centre) and put it here.
         */
        public static final double[] START_POSE = { 9, 24, 0 };
        /**
         * Auto Pathed waypoints {x, y, heading}, in ARENA coordinates (not from the start).
         * The team's loop (2026-10-31): 85 in ahead, 100 in left, back, and home to START_POSE.
         */
        public static final double[][] APEXPATH = { { 94, 24, 0 }, { 94, 124, 90 }, { 9, 124, 90 }, { 9, 24, 0 } };
        /** Drive power per inch still to go. */
        public static final double POSITION_KP = 0.06;
        public static final double POSITION_TOLERANCE_IN = 1.0;
        public static final double HEADING_TOLERANCE_DEG = 2.0;
        public static final double PATH_MAX_POWER = 0.5;
        /**
         * Near the target the robot stalls below this, so it never asks for less while moving.
         * 0.08 stalled 2.5 in short going forward, 0.2 stalled 3 in short strafing (2026-10-31).
         */
        public static final double PATH_MIN_POWER = 0.28;
        /** Same for turning while following a path: below this the heading never gets corrected. */
        public static final double PATH_MIN_TURN_POWER = 0.15;
        /** Each waypoint gets this long, plus PATH_TIMEOUT_PER_IN_S for every inch of distance. */
        public static final double WAYPOINT_TIMEOUT_S = 4.0;
        public static final double PATH_TIMEOUT_PER_IN_S = 0.1;
        /** Stop if the distance to the target grows this far above its best: driving the wrong way. */
        public static final double RUNAWAY_IN = 6.0;
        /** Stop if the robot leaves the path's box by more than this. */
        public static final double FENCE_MARGIN_IN = 12.0;
        /** Settle at each waypoint before measuring and moving on. */
        public static final double WAYPOINT_SETTLE_S = 0.3;
        /**
         * Stop if, after PROGRESS_CHECK_S of driving, the robot is not at least MIN_PROGRESS_IN
         * closer to the target: e.g. a pod that does not touch the floor, so the robot never
         * seems to move. Found on the robot 2026-10-31 (it drove 50 in "blind").
         */
        public static final double PROGRESS_CHECK_S = 1.5;
        public static final double MIN_PROGRESS_IN = 2.0;
        /** Odometry Path Test: stop if the drive encoders and the Pinpoint disagree by more than this. */
        public static final double TRACKING_MISMATCH_IN = 12.0;
    }

    // =========================================================================================
    /**
     * The fixed BIOBUZZ field (Competition Manual section 9) in our ARENA COORDINATES (see Auto
     * above). The field looks the same turned 180°, so these numbers are right for red and blue.
     * Boxes are {x1, y1, x2, y2} in inches, the real object; util/FieldMap grows them by
     * ROBOT_HALF_IN + CLEARANCE_IN to get where the robot's centre may not go.
     */
    public static final class Field {
        private Field() {}

        /** Half the robot's 18 in width. */
        public static final double ROBOT_HALF_IN = 9.0;
        /** Extra room kept from the hive frame and the flowers. */
        public static final double CLEARANCE_IN = 2.0;
        /**
         * In AUTO our side is x 0 to 72 (columns A-C for red, D-F for blue). Crossing it risks a
         * MAJOR FOUL (G402), so the whole robot stays on our side.
         */
        public static final double OUR_SIDE_MAX_X = 72.0;
        /**
         * Hive frame: the A-shaped side on our half stands on a floor bar at x ≈ 47.3 (49.46 in
         * frame, centred), y 52.5 to 91.5 (38.95 in deep). The hives hang 30.6 in up, out of reach.
         * 2026-11-04: the robot bumped a leg with its edge near x 43, so the legs' feet are wider
         * than the bar: keep out x 42 to 53.
         * Flowers: about 10 in across, on our wall at y ≈ 48 and on the left wall at x ≈ 48.
         */
        public static final double[][] OBSTACLES = {
                { 42, 52.5, 53, 91.5 },
                { 0, 43, 10, 53 },
                { 43, 134, 53, 144 },
        };
        public static final String[] OBSTACLE_NAMES = { "hive frame", "flower on our wall", "flower on the left wall" };
        /** Our LOADING ZONE (manual Figure 9-2): AUTO PARK = the robot at least partly inside it. */
        public static final double[] LOADING_ZONE = { 0, 96, 11, 119 };
        /** Route planning grid, inches. */
        public static final double GRID_IN = 2.0;
    }

    // =========================================================================================
    /**
     * Auto Compete: shoot the 4 preloads into our hive, then PARK in our Loading Zone.
     * Poses are {x, y, heading} in ARENA COORDINATES. Heading 180° = the intake (front) faces our
     * wall, so the launcher at the back faces the hive.
     */
    public static final class Compete {
        private Compete() {}

        /**
         * Start: against the RIGHT-HAND side wall (y = 0), intake to the wall (heading -90°), the
         * launcher facing along the wall's normal straight at our hive, in line with the hive frame
         * (x ≈ 47 from our wall). Legal per G304: touching a wall, our side, not in the Loading
         * Zone, clear of the flowers. TODO: measure how far the robot centre is from our wall.
         * (2026-11-04: first set to our wall at (9, 72, 180°); the team actually starts here.)
         */
        public static final double[] START_POSE = { 47, 9, -90 };
        /** Where to shoot from. The same as START_POSE = shoot without moving. */
        public static final double[] SHOOT_POSE = { 47, 9, -90 };
        /**
         * The way to the Loading Zone, {x, y} in order (each leg still checked against the map):
         *  1. along the right-hand wall to the corner by our wall;
         *  2. straight along our wall (the team's "96 in" run) with the robot's side 12 in off the
         *     wall, which clears the 10 in flower on it by 2 in;
         *  3. turn on the spot to PARK_POSE's heading (out here, clear of walls and flowers: a
         *     turning 18 in robot reaches 12.7 in from its centre);
         *  4. in against our wall at the Loading Zone.
         * 2026-11-09: run end 108 -> 120 (one foot further along our wall), then 114 (6 in further).
         */
        public static final double[][] PARK_VIA = { { 21, 12 }, { 21, 114 } };
        /** Heading while driving the PARK_VIA legs: the same as shooting, so no turn near walls. */
        public static final double PARK_TRAVEL_HEADING = -90;
        /**
         * Where to park ("2" on the team's sketch): 1 in off our wall.
         * 2026-11-09: further along the wall (y 108 -> 120 -> 114, the team's call after watching it)
         * and facing the arena: heading 0 = intake out toward the field, launcher (back) against our
         * wall (was -90, side on). Our map's Loading Zone is y 96-119; at y 114 the robot covers
         * y 105-123.
         */
        public static final double[] PARK_POSE = { 10, 114, 0 };

        public static final int PRELOADS = 4;
        /** Launcher preset for the shots (0 = NEAR). */
        public static final int SHOOT_PRESET = 0;
        /**
         * Shooting: the windmill turns while the wheel is at full speed and pauses the moment a ball
         * goes through (the wheel dips SHOT_DIP_TPS below target), until the wheel is back within
         * READY_TOLERANCE_TPS. So every ball meets a full-speed wheel and none is skipped.
         * 2026-11-04: replaced single 280 ms quarter turns, which fired only 2 of 4 balls.
         * Stops after PRELOADS shots or FEED_MAX_S, whichever comes first.
         */
        public static final double SHOT_DIP_TPS = Windmill.PAUSE_DIP_TPS;
        /** 8 until 2026-11-09: the last ball sometimes needed longer, and there is time to spare. */
        public static final double FEED_MAX_S = 12.0;
        /**
         * While the windmill turns, the intake pushes gently (Intake.FEED_POWER), as Launcher Tuner
         * does. 2026-11-09: without it the wheel ran 20-30 tps low for seconds at a time while
         * feeding (a ball rubbing on it without going through) and the 3rd / 4th ball came late
         * or never. false = windmill only, as before.
         */
        public static final boolean INTAKE_PUSH_WHILE_FEEDING = true;
        /**
         * The robot does not drive while shooting, so before parking its position should still be
         * the shooting spot. Further off than this (the Pinpoint was reset or slipped): log it and
         * put it back to the shooting spot.
         */
        public static final double POSE_CHECK_IN = 6.0;
        /**
         * At the end, push gently sideways against our wall for this long, so the robot finishes
         * flat against it (odometry alone left it a few inches off).
         */
        public static final double WALL_SQUEEZE_POWER = 0.35;
        public static final double WALL_SQUEEZE_S = 0.8;

        /** AUTO is 30 s. */
        public static final double AUTO_LENGTH_S = 30.0;
        /**
         * "Auto Compete - wait 8 s": stand still this long before shooting, for matches where our
         * alliance partner shoots first (2026-11-14, first competition). The robot still leaves for
         * the Loading Zone at the usual time, so about 8 s are left for the 4 balls.
         */
        public static final double PARTNER_FIRST_WAIT_S = 8.0;
        /** ...and start the launcher this long before the wait ends, so it is at speed in time. */
        public static final double WAIT_SPINUP_LEAD_S = 1.5;
        /** Leave for the Loading Zone no later than this many seconds before the end... */
        public static final double PARK_RESERVE_S = 8.0;
        /** ...or earlier if the trip needs it: route length at this speed, plus the margin. */
        public static final double PARK_SPEED_IN_PER_S = 12.0;
        public static final double PARK_MARGIN_S = 2.0;

        // ---- Bumps ----
        /** Stuck: less than STALL_MIN_IN closer to the target in STALL_WINDOW_S, still away from it. */
        public static final double STALL_WINDOW_S = 0.75;
        public static final double STALL_MIN_IN = 1.0;
        public static final double STALL_FAR_IN = 3.0;
        /** After a bump: back off this far the way we came, step this far sideways, plan again. */
        public static final double BACK_OFF_IN = 6.0;
        public static final double SIDESTEP_IN = 8.0;
        public static final int MAX_RETRIES = 2;
    }
}
