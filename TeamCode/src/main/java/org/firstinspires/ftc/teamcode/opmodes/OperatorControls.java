package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.util.Debug;

/**
 * Gamepad 2, the operator: runs the ball path by hand. One copy, used by Main TeleOp and
 * Ball Path Test, so the two always behave the same.
 *
 *   Hold RIGHT BUMPER     intake + side wheels pull in, while held
 *   CROSS (A)             intake + side wheels on / off
 *   RIGHT STICK up/down   intake, side wheels and windmill together, as fast as pushed:
 *                         up = toward the launcher, down = backward (push balls out)
 *   Hold RIGHT TRIGGER    shoot: launcher spins up, then the windmill turns once it is at speed,
 *                         pausing after each ball until the wheel is back to speed
 *   LEFT TRIGGER          launcher on / off
 *   LEFT BUMPER           windmill on / off
 *   D-pad UP / DOWN       launcher speed +10 / -10 tps (Constants.Launcher.OPERATOR_DPAD_SPEED)
 *   D-pad RIGHT / LEFT    windmill speed +0.05 / -0.05          } both OFF while Constants.Launcher.
 *   CIRCLE (B)            jump to the other speed preset         } OPERATOR_SPEED_CHANGES is false
 *
 * Short rumble = launcher at speed. Hub lights: green ready, yellow spinning up, blue idle.
 *
 * This does not use BallPath's automatic one-ball-at-a-time firing. That comes back (parked as
 * Smart Fire TeleOp) once the windmill index sensor makes each 90-degree step exact.
 * Call robot.updateManual() each loop, not robot.update(): BallPath would fight these controls.
 */
public class OperatorControls {

    private final Robot robot;
    private final ElapsedTime shootTimer = new ElapsedTime();

    private boolean intakeOn, launcherOn, windmillOn, windmillStarted, lastReady;
    /** A ball just went through: the windmill waits for the wheel to recover. */
    private boolean recovering;
    private final ElapsedTime recoverTimer = new ElapsedTime();
    private int lastLed;

    // What happened this loop, for telemetry.
    private boolean shooting, stickActive, runIntake, runWindmill, runLauncher, ready;
    private double stick;

    public OperatorControls(Robot robot) {
        this.robot = robot;
    }

    /** Once per loop, after robot.updateManual(). prev = the gamepad as it was last loop. */
    public void update(Gamepad g, Gamepad prev) {
        if (g.cross && !prev.cross) intakeOn = !intakeOn;
        if (g.left_trigger > 0.5 && prev.left_trigger <= 0.5) launcherOn = !launcherOn;
        if (g.left_bumper && !prev.left_bumper) windmillOn = !windmillOn;

        // Launcher speed: starts at the NEAR preset (same as Auto Compete); D-pad up/down nudges it
        // (OPERATOR_DPAD_SPEED). Circle and the windmill speed stay fixed (OPERATOR_SPEED_CHANGES).
        boolean adjust = Constants.Launcher.OPERATOR_SPEED_CHANGES;
        boolean dpadSpeed = Constants.Launcher.OPERATOR_DPAD_SPEED;
        if (adjust && g.circle && !prev.circle) {
            robot.launcher.selectPreset((robot.launcher.getPresetIndex() + 1) % Constants.Launcher.PRESETS_TPS.length);
            Debug.log("operator: launcher preset %s, %.0f tps", robot.launcher.getPresetName(), robot.launcher.getTargetTps());
        }
        double step = 0;
        if (dpadSpeed && g.dpad_up && !prev.dpad_up) step = Constants.Launcher.STEP_TPS;
        if (dpadSpeed && g.dpad_down && !prev.dpad_down) step = -Constants.Launcher.STEP_TPS;
        if (step != 0) {
            robot.launcher.setTargetTps(Range.clip(robot.launcher.getTargetTps() + step, 0, Constants.Launcher.MAX_TPS));
            Debug.log("operator: launcher target %.0f tps", robot.launcher.getTargetTps());
        }

        // Windmill speed: D-pad right faster, left slower.
        double wStep = 0;
        if (adjust && g.dpad_right && !prev.dpad_right) wStep = Constants.Windmill.SPEED_STEP;
        if (adjust && g.dpad_left && !prev.dpad_left) wStep = -Constants.Windmill.SPEED_STEP;
        if (wStep != 0) {
            robot.windmill.setFeedSpeed(robot.windmill.getFeedSpeed() + wStep);
            Debug.log("operator: windmill speed %.2f", robot.windmill.getFeedSpeed());
        }

        shooting = g.right_trigger > 0.5;
        if (shooting && prev.right_trigger <= 0.5) { shootTimer.reset(); windmillStarted = false; recovering = false; }
        if (shooting && !windmillStarted
                && (robot.launcher.atSpeed() || shootTimer.seconds() >= Constants.Windmill.SHOOT_MAX_WAIT_S)) {
            windmillStarted = true;
        }
        // After each ball the wheel dips; the windmill pauses until it is back to speed, so every
        // ball leaves at full speed (2026-11-04: continuous feeding sent follow-up balls short).
        if (shooting && windmillStarted) {
            double v = robot.launcher.getVelocityTps(), target = robot.launcher.getTargetTps();
            if (!recovering && v < target - Constants.Windmill.PAUSE_DIP_TPS) {
                recovering = true;
                recoverTimer.reset();
            } else if (recovering && (v >= target - Constants.Launcher.READY_TOLERANCE_TPS
                    || recoverTimer.seconds() >= Constants.Windmill.SHOOT_MAX_WAIT_S)) {
                recovering = false;
            }
        } else {
            recovering = false;
        }

        runLauncher = launcherOn || shooting;
        if (runLauncher && !robot.launcher.isEnabled()) robot.launcher.spinUp();
        if (!runLauncher && robot.launcher.isEnabled()) robot.launcher.stop();

        // Right stick up/down drives the intake, side wheels and windmill together. Out of the
        // middle it overrides the intake and windmill buttons.
        stick = -g.right_stick_y;
        stickActive = Math.abs(stick) > Constants.Intake.STICK_DEADZONE;
        runWindmill = !stickActive && (windmillOn || (shooting && windmillStarted && !recovering));
        runIntake = !stickActive && (intakeOn || g.right_bumper);
        if (stickActive) {
            robot.intake.set(stick > 0 ? stick * Constants.Intake.IN_POWER : -stick * Constants.Intake.OUT_POWER);
            robot.flower.drive(stick);
            robot.windmill.drive(stick);
        } else {
            if (runWindmill) robot.windmill.run(); else robot.windmill.hold();
            if (runIntake) { robot.intake.in(); robot.flower.pullIn(); }
            else { robot.intake.stop(); robot.flower.stop(); }
        }

        ready = robot.launcher.atSpeed();
        if (ready && !lastReady) g.rumble(150);
        lastReady = ready;
        int led = ready ? Robot.LED_READY : robot.launcher.isEnabled() ? Robot.LED_BUSY : Robot.LED_IDLE;
        if (led != lastLed) { robot.setHubLeds(led); lastLed = led; }
    }

    public void addTelemetry(Telemetry t) {
        if (stickActive) {
            t.addData("Right stick", "%s %.0f%%  (intake, side wheels, windmill)",
                    stick > 0 ? "FORWARD" : "BACKWARD", Math.abs(stick) * 100);
        }
        t.addData("Intake + side wheels", stickActive ? "on the right stick"
                : runIntake ? (intakeOn ? "ON (switch)" : "ON (held)") : "off");
        t.addData("Launcher", runLauncher ? (ready ? "READY" : "spinning up") : "off");
        t.addData("Speed", "target %.0f  actual %.0f tps  (%.0f RPM)",
                robot.launcher.getTargetTps(), robot.launcher.getVelocityTps(), robot.launcher.getRpm());
        String next = Constants.Launcher.PRESET_NAMES[(robot.launcher.getPresetIndex() + 1) % Constants.Launcher.PRESETS_TPS.length];
        if (Constants.Launcher.OPERATOR_SPEED_CHANGES) {
            t.addData("Adjust", "D-pad up/down launcher %.0f, right/left windmill %.2f, circle = %s preset",
                    Constants.Launcher.STEP_TPS, Constants.Windmill.SPEED_STEP, next);
        } else if (Constants.Launcher.OPERATOR_DPAD_SPEED) {
            t.addData("Adjust", "D-pad up/down launcher +/-%.0f (starts at %.0f, same as Auto Compete)",
                    Constants.Launcher.STEP_TPS, Constants.Launcher.PRESETS_TPS[0]);
        } else {
            t.addLine("Speeds fixed (same launcher speed as Auto Compete)");
        }
        t.addData("Windmill", "%s   speed %.2f", stickActive ? "on the right stick" : runWindmill ? "TURNING"
                : (shooting ? (recovering ? "paused, wheel recovering" : "waiting for launcher") : "stopped"), robot.windmill.getFeedSpeed());
        boolean windmillForward = runWindmill || (stickActive && stick > 0);
        if (windmillForward && !runLauncher) t.addLine("Careful: windmill feeding with the launcher off");
    }
}
