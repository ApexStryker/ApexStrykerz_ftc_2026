package org.firstinspires.ftc.teamcode.test;

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
 * Hardware Port Test: every joystick axis and button drives its own hub port, so each
 * motor and servo can be checked one at a time. Ports are driven by number, so this
 * works whatever the devices are named in the configuration.
 *
 * PUT THE ROBOT ON BLOCKS. Drive wheels will spin.
 *
 * GAMEPAD 1 - Control Hub
 *   Left stick  up/down     -> motor 0      Right stick up/down     -> motor 2
 *   Left stick  left/right  -> motor 1      Right stick left/right  -> motor 3
 *   Hold X, Y, B, A, LB, RB -> servo 0, 1, 2, 3, 4, 5
 *       (add left trigger to go the other way)
 *
 * GAMEPAD 2 - Expansion Hub
 *   Left stick  up/down     -> motor 0      Right stick up/down     -> motor 2
 *   Left stick  left/right  -> motor 1      Right stick left/right  -> motor 3
 *   Hold X, Y, B, A, LB, RB -> servo 0, 1, 2, 3, 4, 5
 *       (add left trigger to go the other way)
 *
 * Motors run at up to 50% power. Hold the RIGHT TRIGGER on that gamepad for 100%.
 * A servo button moves a positional servo to one end, or spins a continuous servo;
 * releasing it cuts the signal, so a continuous servo stops.
 *
 * Readings are written to the robot log (tag "PortTest") whenever a port is moving:
 *   adb logcat -s PortTest
 */
@TeleOp(name = "Hardware Port Test", group = "Test")
public class HardwarePortTest extends LinearOpMode {

    private static final double NORMAL_MOTOR_LIMIT = 0.5;
    private static final double DEADZONE = 0.10;
    private static final String TAG = "PortTest";
    private static final double LOG_PERIOD_S = 0.25;

    private DcMotorController chMotors, ehMotors;
    private ServoController chServos, ehServos;
    private final boolean[][] servoOn = new boolean[2][6];

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

        telemetry.addLine("Hardware Port Test. Robot on blocks!");
        telemetry.addLine("Gamepad 1 = Control Hub, Gamepad 2 = Expansion Hub.");
        telemetry.addLine("Sticks drive motors 0-3. Hold X,Y,B,A,LB,RB for servos 0-5.");
        if (ehMotors == null) telemetry.addLine("WARNING: no Expansion Hub in the active configuration.");
        telemetry.update();

        waitForStart();
        ElapsedTime logTimer = new ElapsedTime();

        while (opModeIsActive()) {
            boolean logNow = logTimer.seconds() >= LOG_PERIOD_S;
            if (logNow) logTimer.reset();

            // ---- Control Hub from gamepad 1 ----
            double limit1 = gamepad1.right_trigger > 0.5 ? 1.0 : NORMAL_MOTOR_LIMIT;
            double[] ch = {
                    -gamepad1.left_stick_y, gamepad1.left_stick_x,
                    -gamepad1.right_stick_y, gamepad1.right_stick_x };
            boolean[] chBtn = { gamepad1.x, gamepad1.y, gamepad1.b, gamepad1.a,
                    gamepad1.left_bumper, gamepad1.right_bumper };
            driveMotors("Control Hub", chMotors, ch, limit1, logNow);
            driveServos(0, "Control Hub", chServos, chBtn, gamepad1.left_trigger > 0.5, logNow);

            // ---- Expansion Hub from gamepad 2 ----
            double limit2 = gamepad2.right_trigger > 0.5 ? 1.0 : NORMAL_MOTOR_LIMIT;
            double[] eh = {
                    -gamepad2.left_stick_y, gamepad2.left_stick_x,
                    -gamepad2.right_stick_y, gamepad2.right_stick_x };
            boolean[] ehBtn = { gamepad2.x, gamepad2.y, gamepad2.b, gamepad2.a,
                    gamepad2.left_bumper, gamepad2.right_bumper };
            driveMotors("Expansion Hub", ehMotors, eh, limit2, logNow);
            driveServos(1, "Expansion Hub", ehServos, ehBtn, gamepad2.left_trigger > 0.5, logNow);

            // ---- Screen ----
            telemetry.addLine("GP1 = Control Hub    GP2 = Expansion Hub");
            telemetry.addLine("L stick up/down m0, L left/right m1, R up/down m2, R left/right m3");
            telemetry.addLine("Hold X Y B A LB RB = servo 0-5 (+LT reverse). RT = 100% power");
            telemetry.addLine();
            showMotors("Control Hub", chMotors);
            showMotors("Expansion Hub", ehMotors);
            showServos(0, "Control Hub");
            showServos(1, "Expansion Hub");
            telemetry.update();
        }
        stopAll();
    }

    // ---------------------------------------------------------------------------------

    private void driveMotors(String hub, DcMotorController c, double[] sticks, double limit, boolean logNow) {
        if (c == null) return;
        for (int port = 0; port < 4; port++) {
            double s = Math.abs(sticks[port]) < DEADZONE ? 0 : sticks[port];
            c.setMotorPower(port, s * limit);
            if (s != 0 && logNow) {
                String extra = "";
                if (c instanceof DcMotorControllerEx) {
                    DcMotorControllerEx ex = (DcMotorControllerEx) c;
                    extra = String.format(Locale.US, " vel=%.0f tps current=%.2f A",
                            ex.getMotorVelocity(port), ex.getMotorCurrent(port, CurrentUnit.AMPS));
                }
                RobotLog.ii(TAG, "MOTOR %s %d (%s) power=%+.2f ticks=%d%s", hub, port,
                        motorName(c, port), s * limit, c.getMotorCurrentPosition(port), extra);
            }
        }
    }

    private void driveServos(int hubIdx, String hub, ServoController c, boolean[] buttons,
                             boolean reverse, boolean logNow) {
        if (c == null) return;
        for (int port = 0; port < 6; port++) {
            if (buttons[port]) {
                double pos = reverse ? 0.0 : 1.0;
                c.setServoPosition(port, pos);
                if (!servoOn[hubIdx][port] && c instanceof ServoControllerEx) {
                    ((ServoControllerEx) c).setServoPwmEnable(port);
                }
                if (!servoOn[hubIdx][port] || logNow) {
                    RobotLog.ii(TAG, "SERVO %s %d (%s) command=%.1f", hub, port, servoName(c, port), pos);
                }
                servoOn[hubIdx][port] = true;
            } else if (servoOn[hubIdx][port]) {
                releaseServo(c, port);
                servoOn[hubIdx][port] = false;
                RobotLog.ii(TAG, "SERVO %s %d released", hub, port);
            }
        }
    }

    private void showMotors(String hub, DcMotorController c) {
        if (c == null) { telemetry.addLine(hub + ": not in configuration"); return; }
        for (int port = 0; port < 4; port++) {
            double vel = c instanceof DcMotorControllerEx ? ((DcMotorControllerEx) c).getMotorVelocity(port) : 0;
            telemetry.addLine(String.format(Locale.US, "%s m%d %-14s pwr %+.2f  ticks %6d  vel %5.0f",
                    hub.startsWith("Control") ? "CH" : "EH", port, motorName(c, port),
                    c.getMotorPower(port), c.getMotorCurrentPosition(port), vel));
        }
    }

    private void showServos(int hubIdx, String hub) {
        StringBuilder sb = new StringBuilder(hub.startsWith("Control") ? "CH servos on:" : "EH servos on:");
        boolean any = false;
        for (int port = 0; port < 6; port++) if (servoOn[hubIdx][port]) { sb.append(' ').append(port); any = true; }
        if (!any) sb.append(" none");
        telemetry.addLine(sb.toString());
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
