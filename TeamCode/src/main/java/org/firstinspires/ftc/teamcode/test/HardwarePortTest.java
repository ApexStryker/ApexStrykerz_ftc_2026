package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorControllerEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.Locale;

/**
 * Hardware Port Test, laid out for the goBILDA 2026-27 StarterBot wiring:
 *
 *   Control Hub   motor 0-3 : front left, front right, back left, back right drive
 *   Expansion Hub motor 0   : launcher        motor 1 : intake
 *   Control Hub   servo 0   : Servo 1 (front lateral wheel)
 *   Control Hub   servo 2   : Servo 2 (front lateral wheel)
 *   Control Hub   servo 4   : windmill
 *
 * Ports are driven by number, so this works whatever the devices are named.
 * PUT THE ROBOT ON BLOCKS. Drive wheels will spin.
 *
 * GAMEPAD 1 - drive wheels, one per stick direction
 *   Left stick  up/down = front left (CH m0)     Left stick  left/right = front right (CH m1)
 *   Right stick up/down = back left  (CH m2)     Right stick left/right = back right  (CH m3)
 *
 * GAMEPAD 2 - mechanisms
 *   Left stick  up/down = launcher (EH m0)       Right stick up/down = intake (EH m1)
 *   Hold X = Servo 1 (CH s0)   Hold B = Servo 2 (CH s2)   Hold Y = windmill (CH s4)
 *
 * Either gamepad: hold LEFT TRIGGER to reverse a servo, RIGHT TRIGGER for 100% motor power
 * (motors are capped at 50% otherwise).
 *
 * SPARE PORTS, for finding anything plugged in somewhere unexpected:
 *   Gamepad 1 hold X, Y, B, A, LB, RB          = Control Hub servo 0, 1, 2, 3, 4, 5
 *   Gamepad 2 D-pad up, right, down, left       = Expansion Hub servo 0, 1, 2, 3
 *
 * Readings are written to the robot log (tag "PortTest") whenever something moves.
 */
// Hidden from the Driver Station list now that the configuration names are verified with
// Config Name Test. Remove @Disabled to probe raw ports again after rewiring.
@Disabled
@TeleOp(name = "Hardware Port Test", group = "Test")
public class HardwarePortTest extends LinearOpMode {

    private static final double NORMAL_MOTOR_LIMIT = 0.5;
    private static final double DEADZONE = 0.10;
    private static final String TAG = "PortTest";
    private static final double LOG_PERIOD_S = 0.25;

    private static final String[] CH_MOTOR_LABEL = { "front left", "front right", "back left", "back right" };
    private static final String[] EH_MOTOR_LABEL = { "launcher", "intake", "spare", "spare" };
    private static final String[] CH_SERVO_LABEL = { "Servo 1 lateral", "spare", "Servo 2 lateral", "spare", "windmill", "spare" };
    private static final String[] EH_SERVO_LABEL = { "spare", "spare", "spare", "spare", "spare", "spare" };

    private DcMotorController chMotors, ehMotors;
    private ServoController chServos, ehServos;
    private final boolean[] chServoOn = new boolean[6];
    private final boolean[] ehServoOn = new boolean[6];
    private boolean logNow;

    @Override
    public void runOpMode() {
        for (DcMotorController c : hardwareMap.getAll(DcMotorController.class)) {
            if (isControlHub(c)) chMotors = c; else ehMotors = c;
        }
        for (ServoController c : hardwareMap.getAll(ServoController.class)) {
            if (isControlHub(c)) chServos = c; else ehServos = c;
        }
        prepareMotors(chMotors);
        prepareMotors(ehMotors);
        stopAll();

        RobotLog.ii(TAG, "found: CH motors=%b CH servos=%b EH motors=%b EH servos=%b",
                chMotors != null, chServos != null, ehMotors != null, ehServos != null);

        telemetry.addLine("Hardware Port Test (StarterBot wiring). Robot on blocks!");
        telemetry.addLine("GP1 sticks = 4 drive wheels. GP2 sticks = launcher, intake.");
        telemetry.addLine("GP2 X = Servo 1, B = Servo 2, Y = windmill.");
        if (ehMotors == null) telemetry.addLine("WARNING: no Expansion Hub in the active configuration.");
        telemetry.update();

        waitForStart();
        ElapsedTime logTimer = new ElapsedTime();

        while (opModeIsActive()) {
            logNow = logTimer.seconds() >= LOG_PERIOD_S;
            if (logNow) logTimer.reset();

            boolean full = gamepad1.right_trigger > 0.5 || gamepad2.right_trigger > 0.5;
            boolean reverse = gamepad1.left_trigger > 0.5 || gamepad2.left_trigger > 0.5;
            double limit = full ? 1.0 : NORMAL_MOTOR_LIMIT;

            // ---- Motors ----
            motor("Control Hub", chMotors, 0, CH_MOTOR_LABEL[0], -gamepad1.left_stick_y, limit);
            motor("Control Hub", chMotors, 1, CH_MOTOR_LABEL[1], gamepad1.left_stick_x, limit);
            motor("Control Hub", chMotors, 2, CH_MOTOR_LABEL[2], -gamepad1.right_stick_y, limit);
            motor("Control Hub", chMotors, 3, CH_MOTOR_LABEL[3], gamepad1.right_stick_x, limit);
            motor("Expansion Hub", ehMotors, 0, EH_MOTOR_LABEL[0], -gamepad2.left_stick_y, limit);
            motor("Expansion Hub", ehMotors, 1, EH_MOTOR_LABEL[1], -gamepad2.right_stick_y, limit);

            // ---- Servos: named buttons on gamepad 2, spare-port buttons on both ----
            boolean[] ch = {
                    gamepad2.x || gamepad1.x,             // s0 Servo 1
                    gamepad1.y,                           // s1 spare
                    gamepad2.b || gamepad1.b,             // s2 Servo 2
                    gamepad1.a,                           // s3 spare
                    gamepad2.y || gamepad1.left_bumper,   // s4 windmill
                    gamepad1.right_bumper };              // s5 spare
            boolean[] eh = {
                    gamepad2.dpad_up, gamepad2.dpad_right, gamepad2.dpad_down, gamepad2.dpad_left,
                    false, false };
            servos("Control Hub", chServos, chServoOn, CH_SERVO_LABEL, ch, reverse);
            servos("Expansion Hub", ehServos, ehServoOn, EH_SERVO_LABEL, eh, reverse);

            // ---- Screen ----
            telemetry.addLine("GP1: L↕ FL  L↔ FR  R↕ BL  R↔ BR      GP2: L↕ launcher  R↕ intake");
            telemetry.addLine("GP2: X Servo 1   B Servo 2   Y windmill    LT reverse   RT 100%");
            telemetry.addLine();
            showMotor("CH m0 front left ", chMotors, 0);
            showMotor("CH m1 front right", chMotors, 1);
            showMotor("CH m2 back left  ", chMotors, 2);
            showMotor("CH m3 back right ", chMotors, 3);
            showMotor("EH m0 launcher   ", ehMotors, 0);
            showMotor("EH m1 intake     ", ehMotors, 1);
            telemetry.addLine();
            telemetry.addLine(servoLine("CH", chServoOn, CH_SERVO_LABEL));
            telemetry.addLine(servoLine("EH", ehServoOn, EH_SERVO_LABEL));
            telemetry.update();
        }
        stopAll();
    }

    // ---------------------------------------------------------------------------------

    private void motor(String hub, DcMotorController c, int port, String label, double stick, double limit) {
        if (c == null) return;
        double s = Math.abs(stick) < DEADZONE ? 0 : stick;
        c.setMotorPower(port, s * limit);
        if (s != 0 && logNow) {
            String extra = "";
            if (c instanceof DcMotorControllerEx) {
                DcMotorControllerEx ex = (DcMotorControllerEx) c;
                extra = String.format(Locale.US, " vel=%.0f tps current=%.2f A",
                        ex.getMotorVelocity(port), ex.getMotorCurrent(port, CurrentUnit.AMPS));
            }
            RobotLog.ii(TAG, "MOTOR %s %d (%s, config=%s) power=%+.2f ticks=%d%s", hub, port, label,
                    motorName(c, port), s * limit, c.getMotorCurrentPosition(port), extra);
        }
    }

    private void servos(String hub, ServoController c, boolean[] on, String[] labels,
                        boolean[] held, boolean reverse) {
        if (c == null) return;
        for (int port = 0; port < 6; port++) {
            if (held[port]) {
                double pos = reverse ? 0.0 : 1.0;
                c.setServoPosition(port, pos);
                if (!on[port] && c instanceof ServoControllerEx) ((ServoControllerEx) c).setServoPwmEnable(port);
                if (!on[port] || logNow) {
                    RobotLog.ii(TAG, "SERVO %s %d (%s, config=%s) command=%.1f", hub, port, labels[port],
                            servoName(c, port), pos);
                }
                on[port] = true;
            } else if (on[port]) {
                releaseServo(c, port);
                on[port] = false;
                RobotLog.ii(TAG, "SERVO %s %d (%s) released", hub, port, labels[port]);
            }
        }
    }

    private void showMotor(String label, DcMotorController c, int port) {
        if (c == null) { telemetry.addLine(label + "  hub not in configuration"); return; }
        double vel = c instanceof DcMotorControllerEx ? ((DcMotorControllerEx) c).getMotorVelocity(port) : 0;
        double amps = c instanceof DcMotorControllerEx
                ? ((DcMotorControllerEx) c).getMotorCurrent(port, CurrentUnit.AMPS) : 0;
        telemetry.addLine(String.format(Locale.US, "%s pwr %+.2f  vel %5.0f  %.1f A  ticks %d",
                label, c.getMotorPower(port), vel, amps, c.getMotorCurrentPosition(port)));
    }

    private String servoLine(String hub, boolean[] on, String[] labels) {
        StringBuilder sb = new StringBuilder(hub + " servos moving:");
        boolean any = false;
        for (int port = 0; port < 6; port++) {
            if (on[port]) { sb.append(" s").append(port).append(" (").append(labels[port]).append(")"); any = true; }
        }
        if (!any) sb.append(" none");
        return sb.toString();
    }

    private void prepareMotors(DcMotorController c) {
        if (c == null) return;
        for (int port = 0; port < 4; port++) {
            c.setMotorMode(port, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            c.setMotorZeroPowerBehavior(port, DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    private void stopAll() {
        for (DcMotorController c : new DcMotorController[] { chMotors, ehMotors }) {
            if (c != null) for (int port = 0; port < 4; port++) c.setMotorPower(port, 0);
        }
        for (ServoController c : new ServoController[] { chServos, ehServos }) {
            if (c != null) for (int port = 0; port < 6; port++) releaseServo(c, port);
        }
    }

    private void releaseServo(ServoController c, int port) {
        if (c instanceof ServoControllerEx) ((ServoControllerEx) c).setServoPwmDisable(port);
        else c.setServoPosition(port, 0.5);
    }

    private boolean isControlHub(HardwareDevice d) {
        for (String n : hardwareMap.getNamesOf(d)) if (n.toLowerCase().contains("control")) return true;
        return false;
    }

    private String motorName(DcMotorController c, int port) {
        for (DcMotor m : hardwareMap.getAll(DcMotor.class)) {
            if (m.getController() == c && m.getPortNumber() == port) {
                for (String n : hardwareMap.getNamesOf(m)) return n;
            }
        }
        return "-";
    }

    private String servoName(ServoController c, int port) {
        for (com.qualcomm.robotcore.hardware.Servo s : hardwareMap.getAll(com.qualcomm.robotcore.hardware.Servo.class)) {
            if (s.getController() == c && s.getPortNumber() == port) {
                for (String n : hardwareMap.getNamesOf(s)) return n;
            }
        }
        return "-";
    }
}
