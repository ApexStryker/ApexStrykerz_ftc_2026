package org.firstinspires.ftc.teamcode.test;

import static org.firstinspires.ftc.teamcode.Constants.Config;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.hardware.ContinuousServo;

import java.util.Locale;

/**
 * Config Name Test: drives every device BY ITS CONFIGURATION NAME (from Constants.Config) to
 * prove each name points at the right physical motor or servo. If the stick labelled
 * "launcher" spins the intake, the configuration has those two swapped.
 *
 * Devices are fetched with hardwareMap.tryGet, so a missing name shows "NOT FOUND" instead of
 * crashing the OpMode. PUT THE ROBOT ON BLOCKS.
 *
 * GAMEPAD 1 - drive wheels
 *   Left  stick up/down = front_left      Left  stick left/right = front_right
 *   Right stick up/down = back_left       Right stick left/right = back_right
 *
 * GAMEPAD 2 - mechanisms
 *   Left stick up/down = launcher         Right stick up/down = intake
 *   Hold D-pad LEFT  (or X) = servo_left
 *   Hold D-pad RIGHT (or B) = servo_right
 *   Hold D-pad UP    (or Y) = servo_windmill
 *
 * Either gamepad: RIGHT TRIGGER allows 100% motor power (50% otherwise). A held servo button
 * sweeps the servo end to end every 2 s, so a standard-mode servo swings and a continuous-mode
 * servo spins each way; LEFT TRIGGER holds it at one end (full speed one way) instead.
 * Releasing stops it. Servos work whether configured as "Servo" or "Continuous Rotation Servo".
 *
 * The screen and the robot log (tag "HwTest") show each device's hub and port, motor
 * readings, the Control Hub's servo current, and which buttons the robot is receiving.
 */
@TeleOp(name = "Config Name Test", group = "Test")
public class ConfigNameTest extends LinearOpMode {

    private static final double NORMAL_MOTOR_LIMIT = 0.5;
    private static final double LOG_PERIOD_S = 0.25;

    private static class NamedServo {
        final String name;
        final ContinuousServo servo;
        final ElapsedTime held = new ElapsedTime();
        boolean on;

        NamedServo(String name, ContinuousServo servo) { this.name = name; this.servo = servo; }
    }

    private boolean logNow;
    private LynxModule controlHub;
    private String lastButtons = "";

    @Override
    public void runOpMode() {
        DcMotorEx fl = motor(Config.FRONT_LEFT), fr = motor(Config.FRONT_RIGHT);
        DcMotorEx bl = motor(Config.BACK_LEFT), br = motor(Config.BACK_RIGHT);
        DcMotorEx launcher = motor(Config.LAUNCHER), intake = motor(Config.INTAKE);
        DcMotorEx[] motors = { fl, fr, bl, br, launcher, intake };
        String[] motorNames = { Config.FRONT_LEFT, Config.FRONT_RIGHT, Config.BACK_LEFT, Config.BACK_RIGHT,
                Config.LAUNCHER, Config.INTAKE };

        NamedServo left = servo(Config.SERVO_LEFT), right = servo(Config.SERVO_RIGHT), windmill = servo(Config.WINDMILL);
        NamedServo[] servos = { left, right, windmill };
        for (NamedServo s : servos) release(s);
        controlHub = TestUtil.hub(hardwareMap, true);

        telemetry.addLine("Config Name Test. Robot on blocks!");
        telemetry.addLine("GP1 sticks = 4 wheels. GP2 sticks = launcher, intake.");
        telemetry.addLine("GP2 D-pad ← servo_left, → servo_right, ↑ servo_windmill.");
        telemetry.update();

        waitForStart();
        ElapsedTime logTimer = new ElapsedTime();

        while (opModeIsActive()) {
            logNow = logTimer.seconds() >= LOG_PERIOD_S;
            if (logNow) logTimer.reset();

            double limit = (gamepad1.right_trigger > 0.5 || gamepad2.right_trigger > 0.5) ? 1.0 : NORMAL_MOTOR_LIMIT;
            boolean park = gamepad1.left_trigger > 0.5 || gamepad2.left_trigger > 0.5;

            drive(fl, Config.FRONT_LEFT, -gamepad1.left_stick_y, limit);
            drive(fr, Config.FRONT_RIGHT, gamepad1.left_stick_x, limit);
            drive(bl, Config.BACK_LEFT, -gamepad1.right_stick_y, limit);
            drive(br, Config.BACK_RIGHT, gamepad1.right_stick_x, limit);
            drive(launcher, Config.LAUNCHER, -gamepad2.left_stick_y, limit);
            drive(intake, Config.INTAKE, -gamepad2.right_stick_y, limit);

            hold(left, gamepad2.dpad_left || gamepad2.x, park);
            hold(right, gamepad2.dpad_right || gamepad2.b, park);
            hold(windmill, gamepad2.dpad_up || gamepad2.y, park);

            String buttons = "GP1[" + TestUtil.heldButtons(gamepad1) + "] GP2[" + TestUtil.heldButtons(gamepad2) + "]";
            if (!buttons.equals(lastButtons)) {
                RobotLog.ii(TestUtil.TAG, "BUTTONS %s", buttons);
                lastButtons = buttons;
            }

            telemetry.addLine("GP1: L↕ front_left  L↔ front_right  R↕ back_left  R↔ back_right");
            telemetry.addLine("GP2: L↕ launcher  R↕ intake   D-pad ← left  → right  ↑ windmill");
            telemetry.addLine("RT = 100% power   LT = park servo");
            telemetry.addLine();
            for (int i = 0; i < motors.length; i++) show(motors[i], motorNames[i]);
            for (NamedServo s : servos) {
                telemetry.addLine(String.format(Locale.US, "%-15s %s", s.name,
                        !s.servo.present ? "NOT FOUND in configuration"
                                : (s.on ? String.format(Locale.US, "MOVING %+.2f", s.servo.getPower()) : "idle")
                                + (s.servo.configuredAsPositional() ? "  (type: Servo)" : "  (type: Continuous)")));
            }
            telemetry.addLine(String.format(Locale.US, "Control Hub servo current %d mA",
                    TestUtil.servoCurrentMa(controlHub)));
            telemetry.addLine("Buttons received: " + buttons);
            telemetry.update();
        }

        for (DcMotorEx m : motors) if (m != null) m.setPower(0);
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
        RobotLog.ii(TestUtil.TAG, "NAME motor %-14s -> %s", name, TestUtil.where(hardwareMap, m));
        return m;
    }

    private NamedServo servo(String name) {
        ContinuousServo s = new ContinuousServo(hardwareMap, name);
        RobotLog.ii(TestUtil.TAG, "NAME servo %-14s -> %s", name, !s.present ? "NOT FOUND"
                : s.configuredAsPositional() ? "configured as Servo" : "configured as Continuous Rotation Servo");
        return new NamedServo(name, s);
    }

    private void drive(DcMotorEx m, String name, double stick, double limit) {
        if (m == null) return;
        double p = TestUtil.deadzone(stick) * limit;
        m.setPower(p);
        if (p != 0 && logNow) {
            RobotLog.ii(TestUtil.TAG, "NAME %s power=%+.2f ticks=%d vel=%.0f tps current=%.2f A", name, p,
                    m.getCurrentPosition(), m.getVelocity(), m.getCurrent(CurrentUnit.AMPS));
        }
    }

    private void hold(NamedServo s, boolean held, boolean park) {
        if (!s.servo.present) return;
        if (held) {
            if (!s.on) {
                s.held.reset();
                RobotLog.ii(TestUtil.TAG, "NAME %s on", s.name);
            }
            // sweep() runs 0..1; as a continuous power that is -1..+1.
            double power = park ? -1.0 : 2 * TestUtil.sweep(s.held.seconds()) - 1;
            s.servo.setPower(power);
            if (logNow) RobotLog.ii(TestUtil.TAG, "NAME %s power=%+.2f servoCurrent=%d mA", s.name, power,
                    TestUtil.servoCurrentMa(controlHub));
            s.on = true;
        } else if (s.on) {
            release(s);
            RobotLog.ii(TestUtil.TAG, "NAME %s released", s.name);
        }
    }

    private void release(NamedServo s) {
        s.servo.stop();
        s.on = false;
    }

    private void show(DcMotorEx m, String name) {
        if (m == null) {
            telemetry.addLine(String.format(Locale.US, "%-15s NOT FOUND in configuration", name));
            return;
        }
        telemetry.addLine(String.format(Locale.US, "%-15s pwr %+.2f  vel %5.0f  %.1f A  %s", name, m.getPower(),
                m.getVelocity(), m.getCurrent(CurrentUnit.AMPS), TestUtil.where(hardwareMap, m)));
    }
}
