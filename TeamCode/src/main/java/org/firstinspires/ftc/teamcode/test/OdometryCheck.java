package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.localization.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.util.Debug;
import org.firstinspires.ftc.teamcode.util.OdometryMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Odometry Check: a guided test of the goBILDA Pinpoint, with PASS / FAIL and what to change.
 * Works while Constants.USE_PINPOINT is still false. Run it before Odometry Path Test.
 *
 * KEEP THE ROBOT STILL while pressing INIT: the Pinpoint calibrates its IMU then.
 * Gamepad 1: CROSS = start the next test, CIRCLE = skip it. During the spin, ANY button stops.
 *
 *   1. Connected   status READY
 *   2. Stays put   10 s still: drift under 0.2 in and 0.5 degrees
 *   3. Push fwd    push the robot forward by hand: x goes UP, y hardly moves
 *   4. Push left   push it left by hand: y goes UP, x hardly moves
 *   5. Spin        the robot turns 2 times left on its own: x and y stay within 4 in, and the
 *                  Pinpoint and the Control Hub IMU agree on the turn within 2 degrees
 *
 * Every result also goes to the robot log (tag Apex). Limits: Constants.Odometry.
 */
@TeleOp(name = "Odometry Check", group = "Test")
public class OdometryCheck extends LinearOpMode {

    private PinpointLocalizer pinpoint;
    private Drivetrain drive;
    private IMU imu;
    private final Gamepad prev = new Gamepad();
    private final List<String> results = new ArrayList<>();

    @Override
    public void runOpMode() {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        pinpoint = new PinpointLocalizer(hardwareMap);
        drive = new Drivetrain(hardwareMap);
        imu = Robot.initImu(hardwareMap);

        while (opModeInInit()) {
            pinpoint.update();
            telemetry.addLine("Odometry Check. KEEP THE ROBOT STILL until the status is READY.");
            telemetry.addData("Pinpoint", pinpoint.statusText());
            if (!pinpoint.present) telemetry.addLine("Add it on the Driver Hub: I2C bus 1, 'goBILDA Pinpoint Odometry Computer', name 'pinpoint'.");
            telemetry.addLine("Gamepad 1: CROSS = next test, CIRCLE = skip.");
            telemetry.update();
        }
        if (!opModeIsActive()) return;
        Debug.log("Odometry Check: start, %s", pinpoint.statusText());

        testConnected();
        if (opModeIsActive()) testStill();
        if (opModeIsActive()) testPush(true);
        if (opModeIsActive()) testPush(false);
        if (opModeIsActive()) testSpin();

        drive.stop();
        while (opModeIsActive()) {
            telemetry.addLine("ODOMETRY CHECK RESULTS");
            for (String r : results) telemetry.addLine(r);
            telemetry.addLine("All PASS? Next: Odometry Path Test (Autonomous list).");
            telemetry.update();
            idle();
        }
    }

    // ---- 1. Connected ------------------------------------------------------------------------
    private void testConnected() {
        while (opModeIsActive()) {
            pinpoint.update();
            if (pinpoint.isTrusted()) { record("1 Connected", true, "READY"); return; }
            telemetry.addLine("1. CONNECTED: waiting for READY (green light). Keep the robot still.");
            telemetry.addData("Pinpoint", pinpoint.statusText());
            telemetry.addLine("CIRCLE = skip");
            telemetry.update();
            if (pressed(gamepad1.circle, prev.circle)) { record("1 Connected", false, pinpoint.statusText()); return; }
            prev.copy(gamepad1);
        }
    }

    // ---- 2. Stays put ------------------------------------------------------------------------
    private void testStill() {
        if (!waitForCross("2. STAYS PUT", "Robot on the floor, nobody touching it.",
                "CROSS: watch it for " + (int) Constants.Odometry.STILL_TEST_S + " s.")) { skipped("2 Stays put"); return; }
        zero();
        double lastHeading = headingDeg(), turned = 0, maxDrift = 0;
        ElapsedTime t = new ElapsedTime();
        while (opModeIsActive() && t.seconds() < Constants.Odometry.STILL_TEST_S) {
            pinpoint.update();
            double h = headingDeg();
            turned += OdometryMath.headingChange(lastHeading, h);
            lastHeading = h;
            maxDrift = Math.max(maxDrift, distanceFromZero());
            telemetry.addLine("2. STAYS PUT: do not touch the robot.");
            telemetry.addData("Time", "%.0f / %.0f s", t.seconds(), Constants.Odometry.STILL_TEST_S);
            telemetry.addData("Drift", "%.2f in, %.2f°", maxDrift, turned);
            telemetry.update();
        }
        boolean pass = OdometryMath.judgeStill(maxDrift, turned);
        record("2 Stays put", pass, String.format("drift %.2f in, %.2f°%s", maxDrift, turned,
                pass ? "" : " -> restart with the robot still during INIT"));
    }

    // ---- 3 and 4. Push by hand -----------------------------------------------------------------
    private void testPush(boolean forward) {
        String name = forward ? "3 Push forward" : "4 Push left";
        String dir = forward ? "FORWARD" : "to the LEFT";
        if (!waitForCross((forward ? "3. PUSH FORWARD" : "4. PUSH LEFT"),
                "CROSS, then push the robot " + dir + " by hand about a foot, without turning it.",
                "Then press CROSS again.")) { skipped(name); return; }
        zero();
        while (opModeIsActive()) {
            pinpoint.update();
            Pose2D p = pinpoint.getPose();
            double x = p.getX(DistanceUnit.INCH), y = p.getY(DistanceUnit.INCH);
            telemetry.addLine("Push the robot " + dir + ", then CROSS.");
            telemetry.addData("x (forward)", "%.2f in", x);
            telemetry.addData("y (left)", "%.2f in", y);
            telemetry.update();
            if (pressed(gamepad1.cross, prev.cross)) {
                prev.copy(gamepad1);
                double along = forward ? x : y, across = forward ? y : x;
                OdometryMath.Push r = OdometryMath.judgePush(along, across);
                if (r == OdometryMath.Push.TOO_SHORT) continue;   // keep going until far enough
                String pod = forward ? "PINPOINT_X_DIR" : "PINPOINT_Y_DIR";
                record(name, r == OdometryMath.Push.PASS, String.format("%s %.1f in, other axis %.1f in%s",
                        forward ? "x" : "y", along, across,
                        r == OdometryMath.Push.PASS ? "" : " -> " + r.advice.replace("this pod's direction", pod)));
                return;
            }
            prev.copy(gamepad1);
        }
    }

    // ---- 5. Spin on the spot -------------------------------------------------------------------
    private void testSpin() {
        if (!waitForCross("5. SPIN", "Robot on the floor with a clear tile all round.",
                "CROSS: it turns " + (int) Constants.Odometry.SPIN_TURNS + " times LEFT by itself. ANY button stops it.")) {
            skipped("5 Spin"); return;
        }
        zero();
        if (imu != null) imu.resetYaw();
        double target = 360 * Constants.Odometry.SPIN_TURNS;
        double lastP = headingDeg(), lastI = imuDeg(), turnP = 0, turnI = 0, maxExc = 0;
        boolean stoppedByButton = false;
        ElapsedTime t = new ElapsedTime();
        sleep(300);   // let go of CROSS
        while (opModeIsActive() && t.seconds() < Constants.Odometry.SPIN_TIMEOUT_S) {
            pinpoint.update();
            double p = headingDeg(), i = imuDeg();
            turnP += OdometryMath.headingChange(lastP, p);
            turnI += OdometryMath.headingChange(lastI, i);
            lastP = p; lastI = i;
            maxExc = Math.max(maxExc, distanceFromZero());
            if (anyButton(gamepad1) || anyButton(gamepad2)) { stoppedByButton = true; break; }
            // Stop on whichever heading reaches the target, so a reversed Pinpoint still stops.
            if (Math.abs(turnP) >= target || (imu != null && Math.abs(turnI) >= target)) break;
            drive.driveRobotCentric(0, 0, -Constants.Odometry.SPIN_POWER, 1.0);   // negative yaw = turn left
            telemetry.addLine("5. SPIN: ANY button stops.");
            telemetry.addData("Pinpoint turn", "%.0f°", turnP);
            telemetry.addData("Control Hub IMU turn", imu == null ? "no IMU" : String.format("%.0f°", turnI));
            telemetry.addData("Furthest from start", "%.2f in", maxExc);
            telemetry.update();
        }
        drive.stop();
        sleep(500);   // settle, then the final readings
        pinpoint.update();
        turnP += OdometryMath.headingChange(lastP, headingDeg());
        turnI += OdometryMath.headingChange(lastI, imuDeg());
        maxExc = Math.max(maxExc, distanceFromZero());

        if (stoppedByButton) { record("5 Spin", false, "stopped by a button"); return; }
        if (turnP < 0 && turnI > 0) {
            record("5 Spin", false, String.format("Pinpoint turned %.0f° but the robot turned %.0f° LEFT -> is the Pinpoint sticker side up?", turnP, turnI));
            return;
        }
        double imuForCheck = imu == null ? turnP : turnI;
        boolean pass = OdometryMath.judgeSpin(maxExc, turnP, imuForCheck);
        StringBuilder msg = new StringBuilder(String.format("wandered %.2f in, Pinpoint %.1f°, IMU %s", maxExc, turnP,
                imu == null ? "n/a" : String.format("%.1f°", turnI)));
        if (maxExc > Constants.Odometry.SPIN_MAX_EXCURSION_IN) {
            msg.append(String.format(" -> A or B off by about %.0f mm: check their signs and measure again", OdometryMath.offsetErrorMm(maxExc)));
        }
        if (imu != null && Math.abs(turnP - turnI) > Constants.Odometry.SPIN_MAX_IMU_DIFF_DEG) {
            msg.append(" -> Pinpoint and IMU disagree on the turn: Pinpoint flat and sticker up? Constants.Hub right?");
        }
        record("5 Spin", pass, msg.toString());
    }

    // ---- helpers -------------------------------------------------------------------------------

    /** Show a test's instructions until CROSS (true) or CIRCLE (false, skip). */
    private boolean waitForCross(String title, String... lines) {
        while (opModeIsActive()) {
            pinpoint.update();
            telemetry.addLine(title);
            for (String l : lines) telemetry.addLine(l);
            telemetry.addLine("CROSS = start, CIRCLE = skip");
            telemetry.addData("Pinpoint", pinpoint.statusText());
            for (String r : results) telemetry.addLine(r);
            telemetry.update();
            boolean go = pressed(gamepad1.cross, prev.cross), skip = pressed(gamepad1.circle, prev.circle);
            prev.copy(gamepad1);
            if (go) return true;
            if (skip) return false;
        }
        return false;
    }

    private void zero() {
        pinpoint.setPose(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0));
        pinpoint.update();
    }

    private double distanceFromZero() {
        Pose2D p = pinpoint.getPose();
        return Math.hypot(p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH));
    }

    private double headingDeg() { return Math.toDegrees(pinpoint.getHeading()); }

    private double imuDeg() { return imu == null ? 0 : imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES); }

    private void record(String name, boolean pass, String detail) {
        String line = String.format("%s: %s  %s", name, pass ? "PASS" : "FAIL", detail);
        results.add(line);
        if (pass) Debug.log("Odometry Check: %s", line); else Debug.warn("Odometry Check: %s", line);
    }

    private void skipped(String name) {
        results.add(name + ": skipped");
        Debug.log("Odometry Check: %s skipped", name);
    }

    private static boolean pressed(boolean now, boolean before) { return now && !before; }

    static boolean anyButton(Gamepad g) {
        return g.a || g.b || g.x || g.y || g.dpad_up || g.dpad_down || g.dpad_left || g.dpad_right
                || g.left_bumper || g.right_bumper || g.left_trigger > 0.5 || g.right_trigger > 0.5
                || g.start || g.back || g.options || g.share;
    }
}
