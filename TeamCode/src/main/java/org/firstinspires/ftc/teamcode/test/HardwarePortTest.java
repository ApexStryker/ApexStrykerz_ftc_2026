package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorControllerEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.Locale;

/**
 * Hardware Port Test: drives every hub port BY NUMBER, ignoring configuration names, to find
 * out what is physically plugged in where. Use it after rewiring; use Config Name Test to check
 * the names afterwards.
 *
 * Hidden from the Driver Station list (@Disabled). Delete that line to use it.
 * PUT THE ROBOT ON BLOCKS. Drive wheels will spin.
 *
 *                 GAMEPAD 1 = Control Hub        GAMEPAD 2 = Expansion Hub
 *   Left  stick up/down     motor 0                  motor 0
 *   Left  stick left/right  motor 1                  motor 1
 *   Right stick up/down     motor 2                  motor 2
 *   Right stick left/right  motor 3                  motor 3
 *   Hold D-pad up/right/down/left, LB, RB  -> servo 0, 1, 2, 3, 4, 5 on that gamepad's hub
 *
 * Motors run at up to 50%; hold RIGHT TRIGGER for 100%. A held servo button sweeps the servo
 * end to end every 2 s; LEFT TRIGGER parks it at 0 instead. Releasing cuts the servo signal.
 * The screen shows each port's configured name (if any) and each hub's servo current, which
 * rises when a connected servo moves. Every change is logged under the tag "HwTest".
 */
@Disabled
@TeleOp(name = "Hardware Port Test", group = "Test")
public class HardwarePortTest extends LinearOpMode {

    private static final double NORMAL_MOTOR_LIMIT = 0.5;
    private static final double LOG_PERIOD_S = 0.25;

    /** One hub's motor and servo controllers plus servo state. */
    private class Hub {
        final String label;
        DcMotorController motors;
        ServoController servos;
        LynxModule module;
        final boolean[] servoOn = new boolean[6];
        final ElapsedTime[] servoHeld = new ElapsedTime[6];

        Hub(String label) {
            this.label = label;
            for (int i = 0; i < 6; i++) servoHeld[i] = new ElapsedTime();
        }
    }

    private final Hub ch = new Hub("CH"), eh = new Hub("EH");
    private boolean logNow;

    @Override
    public void runOpMode() {
        for (DcMotorController c : hardwareMap.getAll(DcMotorController.class)) {
            if (TestUtil.onControlHub(hardwareMap, c)) ch.motors = c; else eh.motors = c;
        }
        for (ServoController c : hardwareMap.getAll(ServoController.class)) {
            if (TestUtil.onControlHub(hardwareMap, c)) ch.servos = c; else eh.servos = c;
        }
        ch.module = TestUtil.hub(hardwareMap, true);
        eh.module = TestUtil.hub(hardwareMap, false);
        for (Hub h : new Hub[] { ch, eh }) prepare(h);
        stopAll();

        telemetry.addLine("Hardware Port Test. Robot on blocks!");
        telemetry.addLine("Gamepad 1 = Control Hub, gamepad 2 = Expansion Hub.");
        if (eh.motors == null) telemetry.addLine("WARNING: no Expansion Hub in the active configuration.");
        telemetry.update();

        waitForStart();
        ElapsedTime logTimer = new ElapsedTime();

        while (opModeIsActive()) {
            logNow = logTimer.seconds() >= LOG_PERIOD_S;
            if (logNow) logTimer.reset();

            drive(ch, gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_y,
                    gamepad1.right_stick_x, gamepad1.right_trigger > 0.5);
            drive(eh, gamepad2.left_stick_y, gamepad2.left_stick_x, gamepad2.right_stick_y,
                    gamepad2.right_stick_x, gamepad2.right_trigger > 0.5);
            servos(ch, new boolean[] { gamepad1.dpad_up, gamepad1.dpad_right, gamepad1.dpad_down,
                    gamepad1.dpad_left, gamepad1.left_bumper, gamepad1.right_bumper }, gamepad1.left_trigger > 0.5);
            servos(eh, new boolean[] { gamepad2.dpad_up, gamepad2.dpad_right, gamepad2.dpad_down,
                    gamepad2.dpad_left, gamepad2.left_bumper, gamepad2.right_bumper }, gamepad2.left_trigger > 0.5);

            telemetry.addLine("GP1 = Control Hub, GP2 = Expansion Hub");
            telemetry.addLine("Sticks: L↕ m0  L↔ m1  R↕ m2  R↔ m3   RT = 100%");
            telemetry.addLine("Servos: D-pad ↑ → ↓ ← = s0-s3, LB = s4, RB = s5   LT = park");
            show(ch);
            show(eh);
            telemetry.update();
        }
        stopAll();
    }

    // ---------------------------------------------------------------------------------

    private void drive(Hub h, double ly, double lx, double ry, double rx, boolean full) {
        if (h.motors == null) return;
        double limit = full ? 1.0 : NORMAL_MOTOR_LIMIT;
        double[] cmd = { -ly, lx, -ry, rx };
        for (int port = 0; port < 4; port++) {
            double p = TestUtil.deadzone(cmd[port]) * limit;
            h.motors.setMotorPower(port, p);
            if (p != 0 && logNow) {
                RobotLog.ii(TestUtil.TAG, "PORT %s motor %d (%s) power=%+.2f ticks=%d%s", h.label, port,
                        motorName(h, port), p, h.motors.getMotorCurrentPosition(port), motorReadings(h, port));
            }
        }
    }

    private void servos(Hub h, boolean[] held, boolean park) {
        if (h.servos == null) return;
        for (int port = 0; port < 6; port++) {
            if (held[port]) {
                if (!h.servoOn[port]) {
                    h.servoHeld[port].reset();
                    if (h.servos instanceof ServoControllerEx) ((ServoControllerEx) h.servos).setServoPwmEnable(port);
                    RobotLog.ii(TestUtil.TAG, "PORT %s servo %d (%s) on", h.label, port, servoName(h, port));
                }
                double pos = park ? 0.0 : TestUtil.sweep(h.servoHeld[port].seconds());
                h.servos.setServoPosition(port, pos);
                if (logNow) RobotLog.ii(TestUtil.TAG, "PORT %s servo %d cmd=%.2f servoCurrent=%d mA",
                        h.label, port, pos, TestUtil.servoCurrentMa(h.module));
                h.servoOn[port] = true;
            } else if (h.servoOn[port]) {
                release(h, port);
                RobotLog.ii(TestUtil.TAG, "PORT %s servo %d released", h.label, port);
            }
        }
    }

    private void show(Hub h) {
        telemetry.addLine();
        if (h.motors == null) { telemetry.addLine(h.label + ": not in the active configuration"); return; }
        for (int port = 0; port < 4; port++) {
            telemetry.addLine(String.format(Locale.US, "%s m%d %-14s pwr %+.2f%s", h.label, port,
                    motorName(h, port), h.motors.getMotorPower(port), motorReadings(h, port)));
        }
        StringBuilder on = new StringBuilder();
        for (int port = 0; port < 6; port++) {
            if (h.servoOn[port]) on.append(" s").append(port).append('(').append(servoName(h, port)).append(')');
        }
        telemetry.addLine(String.format(Locale.US, "%s servos moving:%s   servo current %d mA", h.label,
                on.length() == 0 ? " none" : on.toString(), TestUtil.servoCurrentMa(h.module)));
    }

    private String motorReadings(Hub h, int port) {
        if (!(h.motors instanceof DcMotorControllerEx)) return "";
        DcMotorControllerEx ex = (DcMotorControllerEx) h.motors;
        return String.format(Locale.US, "  vel %5.0f tps  %.2f A", ex.getMotorVelocity(port),
                ex.getMotorCurrent(port, CurrentUnit.AMPS));
    }

    private void prepare(Hub h) {
        if (h.motors == null) return;
        for (int port = 0; port < 4; port++) {
            h.motors.setMotorMode(port, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            h.motors.setMotorZeroPowerBehavior(port, DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    private void stopAll() {
        for (Hub h : new Hub[] { ch, eh }) {
            if (h.motors != null) for (int port = 0; port < 4; port++) h.motors.setMotorPower(port, 0);
            if (h.servos != null) for (int port = 0; port < 6; port++) release(h, port);
        }
    }

    private void release(Hub h, int port) {
        if (h.servos instanceof ServoControllerEx) ((ServoControllerEx) h.servos).setServoPwmDisable(port);
        else h.servos.setServoPosition(port, 0.5);
        h.servoOn[port] = false;
    }

    private String motorName(Hub h, int port) {
        for (DcMotor m : hardwareMap.getAll(DcMotor.class)) {
            if (m.getController() == h.motors && m.getPortNumber() == port) {
                for (String n : hardwareMap.getNamesOf(m)) return n;
            }
        }
        return "-";
    }

    private String servoName(Hub h, int port) {
        for (Servo s : hardwareMap.getAll(Servo.class)) {
            if (s.getController() == h.servos && s.getPortNumber() == port) {
                for (String n : hardwareMap.getNamesOf(s)) return n;
            }
        }
        return "-";
    }
}
