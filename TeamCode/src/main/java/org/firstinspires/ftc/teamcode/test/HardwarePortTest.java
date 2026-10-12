package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorControllerEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Hardware Port Test: drive every motor and servo port, one at a time, to find out
 * what is plugged in where.
 *
 * It talks to the hub ports directly, so it works whatever the devices are named.
 * Each port's name in the active Driver Station configuration is shown next to it,
 * or "(not configured)".
 *
 * PUT THE ROBOT ON BLOCKS. Drive wheels will spin.
 *
 * Gamepad 1:
 *   D-pad up / down  -> pick the previous / next port
 *   Left stick Y     -> drive the picked port
 *                       motors: power, 50% max (hold right bumper for 100%)
 *                       servos: position 0 to 1; a continuous servo spins instead
 *   A                -> stop everything
 *
 * Only the picked port ever moves. Switching ports stops the old one first.
 *
 * Every reading is also written to the robot log with the tag "PortTest", about four
 * times a second while a port is being driven, so it can be reviewed from a laptop:
 *   adb logcat -s PortTest
 */
@TeleOp(name = "Hardware Port Test", group = "Test")
public class HardwarePortTest extends LinearOpMode {

    private static final double NORMAL_MOTOR_LIMIT = 0.5;
    private static final double DEADZONE = 0.05;
    private static final String TAG = "PortTest";
    private static final double LOG_PERIOD_S = 0.25;

    /** One motor or servo port on one hub. */
    private static class Port {
        final String hub;          // "Control Hub", "Expansion Hub 2", ...
        final boolean isMotor;
        final int number;
        final DcMotorController motorCtrl;
        final ServoController servoCtrl;
        String configuredName = "(not configured)";

        Port(String hub, int number, DcMotorController c) {
            this.hub = hub; this.isMotor = true; this.number = number; this.motorCtrl = c; this.servoCtrl = null;
        }
        Port(String hub, int number, ServoController c) {
            this.hub = hub; this.isMotor = false; this.number = number; this.motorCtrl = null; this.servoCtrl = c;
        }
        String label() {
            return hub + "  " + (isMotor ? "motor " : "servo ") + number;
        }
    }

    private final List<Port> ports = new ArrayList<>();

    @Override
    public void runOpMode() {
        buildPortList();
        lookUpConfiguredNames();

        for (Port p : ports) stop(p);

        for (Port p : ports) RobotLog.ii(TAG, "port %-28s config=%s", p.label(), p.configuredName);

        telemetry.addLine("Hardware Port Test. Robot on blocks!");
        telemetry.addLine("D-pad up/down picks a port, left stick drives it, A stops.");
        telemetry.addData("Ports found", ports.size());
        if (!hasHubNamed("Expansion")) {
            telemetry.addLine("WARNING: no Expansion Hub in the active configuration.");
        }
        telemetry.update();

        waitForStart();

        int selected = 0;
        boolean prevUp = false, prevDown = false;
        ElapsedTime logTimer = new ElapsedTime();
        if (!ports.isEmpty()) {
            start(ports.get(selected));
            RobotLog.ii(TAG, "PICKED %s config=%s", ports.get(selected).label(), ports.get(selected).configuredName);
        }

        while (opModeIsActive() && !ports.isEmpty()) {
            // Pick a port. Edge-detect so one press moves one step.
            boolean up = gamepad1.dpad_up, down = gamepad1.dpad_down;
            if ((up && !prevUp) || (down && !prevDown)) {
                stop(ports.get(selected));
                int step = up ? -1 : 1;
                selected = (selected + step + ports.size()) % ports.size();
                start(ports.get(selected));
                RobotLog.ii(TAG, "PICKED %s config=%s", ports.get(selected).label(), ports.get(selected).configuredName);
            }
            prevUp = up;
            prevDown = down;

            Port p = ports.get(selected);
            double stick = -gamepad1.left_stick_y;           // up = positive
            if (Math.abs(stick) < DEADZONE) stick = 0;

            if (gamepad1.a) {
                for (Port q : ports) stop(q);
            } else if (p.isMotor) {
                double limit = gamepad1.right_bumper ? 1.0 : NORMAL_MOTOR_LIMIT;
                p.motorCtrl.setMotorPower(p.number, stick * limit);
            } else {
                // 0.5 is the middle of a servo's range, and "stopped" for a continuous servo.
                p.servoCtrl.setServoPosition(p.number, 0.5 + 0.5 * stick);
            }

            // ---- Robot log: readings while the port is being driven ----
            if (stick != 0 && logTimer.seconds() >= LOG_PERIOD_S) {
                logTimer.reset();
                if (p.isMotor) {
                    String extra = "";
                    if (p.motorCtrl instanceof DcMotorControllerEx) {
                        DcMotorControllerEx ex = (DcMotorControllerEx) p.motorCtrl;
                        extra = String.format(java.util.Locale.US, " vel=%.0f tps current=%.2f A",
                                ex.getMotorVelocity(p.number), ex.getMotorCurrent(p.number, CurrentUnit.AMPS));
                    }
                    RobotLog.ii(TAG, "DRIVE %s stick=%+.2f power=%+.2f ticks=%d%s", p.label(), stick,
                            p.motorCtrl.getMotorPower(p.number), p.motorCtrl.getMotorCurrentPosition(p.number), extra);
                } else {
                    RobotLog.ii(TAG, "DRIVE %s stick=%+.2f servoCmd=%.2f", p.label(), stick,
                            p.servoCtrl.getServoPosition(p.number));
                }
            }

            // ---- Telemetry: the picked port in detail, then the whole list ----
            telemetry.addData("PICKED", p.label());
            telemetry.addData("Config name", p.configuredName);
            if (p.isMotor) {
                telemetry.addData("Power", "%.2f  (%s)", p.motorCtrl.getMotorPower(p.number),
                        gamepad1.right_bumper ? "100% allowed" : "50% max, hold RB for 100%");
                telemetry.addData("Encoder ticks", p.motorCtrl.getMotorCurrentPosition(p.number));
                if (p.motorCtrl instanceof DcMotorControllerEx) {
                    DcMotorControllerEx ex = (DcMotorControllerEx) p.motorCtrl;
                    telemetry.addData("Velocity", "%.0f ticks/s", ex.getMotorVelocity(p.number));
                    telemetry.addData("Current", "%.2f A", ex.getMotorCurrent(p.number, CurrentUnit.AMPS));
                }
            } else {
                telemetry.addData("Servo command", "%.2f  (0.5 = center / stopped)",
                        p.servoCtrl.getServoPosition(p.number));
            }
            telemetry.addLine();
            for (int i = 0; i < ports.size(); i++) {
                Port q = ports.get(i);
                telemetry.addLine((i == selected ? ">> " : "    ") + q.label() + "  =  " + q.configuredName);
            }
            telemetry.update();
        }

        for (Port q : ports) stop(q);
    }

    // ---------------------------------------------------------------------------------

    /** Every motor port, then every servo port, Control Hub first. */
    private void buildPortList() {
        List<DcMotorController> motorCtrls = new ArrayList<>(hardwareMap.getAll(DcMotorController.class));
        List<ServoController> servoCtrls = new ArrayList<>(hardwareMap.getAll(ServoController.class));
        motorCtrls.sort((a, b) -> hubRank(nameOf(a)) - hubRank(nameOf(b)));
        servoCtrls.sort((a, b) -> hubRank(nameOf(a)) - hubRank(nameOf(b)));

        for (DcMotorController c : motorCtrls) {
            for (int i = 0; i < 4; i++) {
                c.setMotorMode(i, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                c.setMotorZeroPowerBehavior(i, DcMotor.ZeroPowerBehavior.BRAKE);
                ports.add(new Port(nameOf(c), i, c));
            }
        }
        for (ServoController c : servoCtrls) {
            for (int i = 0; i < 6; i++) ports.add(new Port(nameOf(c), i, c));
        }
    }

    /** Find which configured device, if any, sits on each port. */
    private void lookUpConfiguredNames() {
        for (DcMotor m : hardwareMap.getAll(DcMotor.class)) {
            setName(m.getController(), m.getPortNumber(), true, nameOf(m) + " (motor)");
        }
        for (Servo s : hardwareMap.getAll(Servo.class)) {
            setName(s.getController(), s.getPortNumber(), false, nameOf(s) + " (servo)");
        }
        for (CRServo s : hardwareMap.getAll(CRServo.class)) {
            setName(s.getController(), s.getPortNumber(), false, nameOf(s) + " (continuous servo)");
        }
    }

    private void setName(Object controller, int port, boolean motor, String name) {
        for (Port p : ports) {
            Object c = motor ? p.motorCtrl : p.servoCtrl;
            if (p.isMotor == motor && c == controller && p.number == port) p.configuredName = name;
        }
    }

    private void start(Port p) {
        if (!p.isMotor && p.servoCtrl instanceof ServoControllerEx) {
            p.servoCtrl.setServoPosition(p.number, 0.5);
            ((ServoControllerEx) p.servoCtrl).setServoPwmEnable(p.number);
        }
    }

    private void stop(Port p) {
        if (p.isMotor) {
            p.motorCtrl.setMotorPower(p.number, 0);
        } else if (p.servoCtrl instanceof ServoControllerEx) {
            // No signal: a continuous servo stops, a positional servo goes limp.
            ((ServoControllerEx) p.servoCtrl).setServoPwmDisable(p.number);
        } else {
            p.servoCtrl.setServoPosition(p.number, 0.5);
        }
    }

    private String nameOf(HardwareDevice d) {
        Set<String> names = hardwareMap.getNamesOf(d);
        return names.isEmpty() ? "?" : names.iterator().next();
    }

    private int hubRank(String name) {
        String n = name.toLowerCase();
        if (n.contains("control")) return 0;
        if (n.contains("expansion")) return 1;
        return 2;
    }

    private boolean hasHubNamed(String part) {
        for (Port p : ports) if (p.hub.toLowerCase().contains(part.toLowerCase())) return true;
        return false;
    }
}
