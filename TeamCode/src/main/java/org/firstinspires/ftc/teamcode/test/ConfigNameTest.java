package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.Locale;

/**
 * Config Name Test: drives every device BY ITS DRIVER STATION CONFIGURATION NAME, to prove
 * each name points at the right physical motor or servo. If a stick labelled "launcher"
 * spins the intake, the configuration has those two swapped.
 *
 * Compare with Hardware Port Test, which drives ports by number and ignores names.
 *
 * PUT THE ROBOT ON BLOCKS.
 *
 * GAMEPAD 1 - drive wheels
 *   Left stick  up/down = front_left      Left stick  left/right = front_right
 *   Right stick up/down = back_left       Right stick left/right = back_right
 *
 * GAMEPAD 2 - mechanisms
 *   Left stick up/down = launcher_mtr      Right stick up/down = intake_mtr
 *   Hold X = servo_left    Hold B = servo_right    Hold Y = servo_windmill
 *
 * Either gamepad: LEFT TRIGGER reverses a servo, RIGHT TRIGGER allows 100% motor power.
 * A missing name is reported on screen and skipped; it never crashes the test.
 * Every movement is logged under the tag "NameTest".
 */
@TeleOp(name = "Config Name Test", group = "Test")
public class ConfigNameTest extends LinearOpMode {

    // Names exactly as saved in the Driver Station configuration "Apex-1" (2026-10-12, 19:49).
    private static final String FL = "front_left", FR = "front_right", BL = "back_left", BR = "back_right";
    private static final String LAUNCHER = "launcher_mtr", INTAKE = "intake_mtr";
    private static final String SERVO_LEFT = "servo_left", SERVO_RIGHT = "servo_right", WINDMILL = "servo_windmill";

    private static final double NORMAL_MOTOR_LIMIT = 0.5;
    private static final double DEADZONE = 0.10;
    private static final String TAG = "NameTest";
    private static final double LOG_PERIOD_S = 0.25;

    private boolean logNow;

    private static class NamedServo {
        final String name; final Servo servo; boolean on;
        NamedServo(String name, Servo servo) { this.name = name; this.servo = servo; }
    }

    @Override
    public void runOpMode() {
        DcMotorEx fl = motor(FL), fr = motor(FR), bl = motor(BL), br = motor(BR);
        DcMotorEx launcher = motor(LAUNCHER), intake = motor(INTAKE);
        NamedServo left = servo(SERVO_LEFT), right = servo(SERVO_RIGHT), windmill = servo(WINDMILL);
        NamedServo[] servos = { left, right, windmill };
        for (NamedServo s : servos) release(s);

        telemetry.addLine("Config Name Test. Robot on blocks!");
        telemetry.addLine("GP1 sticks = 4 wheels. GP2 L = launcher_mtr, R = intake_mtr.");
        telemetry.addLine("GP2 X = servo_left, B = servo_right, Y = servo_windmill.");
        telemetry.update();

        waitForStart();
        ElapsedTime logTimer = new ElapsedTime();

        while (opModeIsActive()) {
            logNow = logTimer.seconds() >= LOG_PERIOD_S;
            if (logNow) logTimer.reset();

            double limit = (gamepad1.right_trigger > 0.5 || gamepad2.right_trigger > 0.5) ? 1.0 : NORMAL_MOTOR_LIMIT;
            boolean reverse = gamepad1.left_trigger > 0.5 || gamepad2.left_trigger > 0.5;

            drive(fl, FL, -gamepad1.left_stick_y, limit);
            drive(fr, FR, gamepad1.left_stick_x, limit);
            drive(bl, BL, -gamepad1.right_stick_y, limit);
            drive(br, BR, gamepad1.right_stick_x, limit);
            drive(launcher, LAUNCHER, -gamepad2.left_stick_y, limit);
            drive(intake, INTAKE, -gamepad2.right_stick_y, limit);

            hold(left, gamepad2.x, reverse);
            hold(right, gamepad2.b, reverse);
            hold(windmill, gamepad2.y, reverse);

            telemetry.addLine("GP1: L↕ front_left  L↔ front_right  R↕ back_left  R↔ back_right");
            telemetry.addLine("GP2: L↕ launcher_mtr  R↕ intake_mtr  X servo_left  B servo_right  Y servo_windmill");
            telemetry.addLine();
            show(fl, FL); show(fr, FR); show(bl, BL); show(br, BR);
            show(launcher, LAUNCHER); show(intake, INTAKE);
            for (NamedServo s : servos) {
                telemetry.addLine(String.format(Locale.US, "%-15s %s", s.name,
                        s.servo == null ? "NOT FOUND in configuration" : (s.on ? "MOVING" : "idle") + "  " + where(s.servo)));
            }
            telemetry.update();
        }

        for (DcMotorEx m : new DcMotorEx[] { fl, fr, bl, br, launcher, intake }) if (m != null) m.setPower(0);
        for (NamedServo s : servos) release(s);
    }

    // ---------------------------------------------------------------------------------

    private DcMotorEx motor(String name) {
        DcMotorEx m = hardwareMap.tryGet(DcMotorEx.class, name);
        if (m != null) {
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            m.setPower(0);
        }
        RobotLog.ii(TAG, "motor %-14s %s", name, m == null ? "NOT FOUND" : where(m));
        return m;
    }

    private NamedServo servo(String name) {
        Servo s = hardwareMap.tryGet(Servo.class, name);
        RobotLog.ii(TAG, "servo %-14s %s", name, s == null ? "NOT FOUND" : where(s));
        return new NamedServo(name, s);
    }

    private void drive(DcMotorEx m, String name, double stick, double limit) {
        if (m == null) return;
        double s = Math.abs(stick) < DEADZONE ? 0 : stick;
        m.setPower(s * limit);
        if (s != 0 && logNow) {
            RobotLog.ii(TAG, "NAMED %s power=%+.2f ticks=%d vel=%.0f tps current=%.2f A", name, s * limit,
                    m.getCurrentPosition(), m.getVelocity(), m.getCurrent(CurrentUnit.AMPS));
        }
    }

    private void hold(NamedServo s, boolean held, boolean reverse) {
        if (s.servo == null) return;
        if (held) {
            double pos = reverse ? 0.0 : 1.0;
            s.servo.setPosition(pos);
            if (!s.on && s.servo instanceof ServoImplEx) ((ServoImplEx) s.servo).setPwmEnable();
            if (!s.on || logNow) RobotLog.ii(TAG, "NAMED %s command=%.1f", s.name, pos);
            s.on = true;
        } else if (s.on) {
            release(s);
            RobotLog.ii(TAG, "NAMED %s released", s.name);
        }
    }

    private void release(NamedServo s) {
        if (s.servo == null) return;
        if (s.servo instanceof ServoImplEx) ((ServoImplEx) s.servo).setPwmDisable();
        else s.servo.setPosition(0.5);
        s.on = false;
    }

    private void show(DcMotorEx m, String name) {
        if (m == null) { telemetry.addLine(String.format(Locale.US, "%-15s NOT FOUND in configuration", name)); return; }
        telemetry.addLine(String.format(Locale.US, "%-15s pwr %+.2f  vel %5.0f  %.1f A  %s", name, m.getPower(),
                m.getVelocity(), m.getCurrent(CurrentUnit.AMPS), where(m)));
    }

    /** "Control Hub port 2" style location, from the device's own controller. */
    private String where(HardwareDevice d) {
        String hub = "?";
        int port = -1;
        if (d instanceof DcMotor) {
            port = ((DcMotor) d).getPortNumber();
            for (String n : hardwareMap.getNamesOf(((DcMotor) d).getController())) hub = n;
        } else if (d instanceof Servo) {
            port = ((Servo) d).getPortNumber();
            for (String n : hardwareMap.getNamesOf(((Servo) d).getController())) hub = n;
        }
        return hub + " port " + port;
    }
}
