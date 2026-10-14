package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * Base for every autonomous: write the plan as a list of simple steps in runSteps().
 *
 *   driveForward(24);     inches forward       driveBackward(24);   inches backward
 *   strafeRight(12);      inches to the right  strafeLeft(12);      inches to the left
 *   turnLeft(90);         degrees, IMU         turnRight(90);       degrees, IMU
 *   intakeOn();           start collecting     intakeOff();         stop collecting
 *   fire(4);              shoot up to 4 balls  pause(1.5);          wait 1.5 seconds
 *
 * Every move is measured from where the robot is now, facing the way it faces now. Each step
 * waits until it is finished (or its time runs out) before the next one starts. The intake keeps
 * collecting during the steps that follow intakeOn().
 *
 * During INIT, gamepad 1 D-pad left = RED, right = BLUE; the choice is in the field "alliance",
 * so a plan can mirror itself:   if (alliance == Constants.Alliance.RED) turnLeft(90); else turnRight(90);
 *
 * To make a new autonomous, copy AutoBasic.java, rename the class and the name in @Autonomous,
 * and change the steps.
 */
public abstract class AutoBase extends LinearOpMode {

    protected Robot robot;
    protected Constants.Alliance alliance = Constants.DEFAULT_ALLIANCE;

    /** The plan: the steps of this autonomous, in order. */
    protected abstract void runSteps();

    /** One line shown on the Driver Station during INIT, so the drivers know what will happen. */
    protected String plan() { return ""; }

    @Override
    public final void runOpMode() {
        robot = new Robot(hardwareMap);

        while (opModeInInit()) {
            if (gamepad1.dpad_left) alliance = Constants.Alliance.RED;
            if (gamepad1.dpad_right) alliance = Constants.Alliance.BLUE;
            telemetry.addData("Alliance", "%s   (gamepad 1 D-pad left = RED, right = BLUE)", alliance);
            telemetry.addData("Plan", plan());
            robot.reportMissing(telemetry);
            telemetry.update();
        }
        if (!opModeIsActive()) return;

        robot.ballPath.setAlliance(alliance);
        robot.setPose(0, 0, 0);
        runSteps();

        robot.stop();
        while (opModeIsActive()) {
            telemetry.addLine("Auto done. Waiting for TeleOp.");
            telemetry.update();
            idle();
        }
    }

    // ---- Steps ---------------------------------------------------------------------------------

    protected void driveForward(double inches) { move("forward", inches, 0); }
    protected void driveBackward(double inches) { move("backward", -inches, 0); }
    protected void strafeRight(double inches) { move("strafe right", 0, inches); }
    protected void strafeLeft(double inches) { move("strafe left", 0, -inches); }
    protected void turnLeft(double degrees) { turn("turn left", degrees); }
    protected void turnRight(double degrees) { turn("turn right", -degrees); }

    protected void intakeOn() { robot.ballPath.setCollect(true); }
    protected void intakeOff() { robot.ballPath.setCollect(false); }

    /** Keep everything running (intake, launcher) for this long without driving. */
    protected void pause(double seconds) {
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < seconds) {
            robot.update();
            show("pause", "%.1f / %.1f s", timer.seconds(), seconds);
        }
    }

    /** Spin up and shoot until this many balls are gone, the windmill comes up empty, or time runs out. */
    protected void fire(int balls) {
        if (!robot.launcher.present || !robot.windmill.present) return;
        robot.ballPath.setBallCount(balls);
        robot.launcher.selectPreset(Constants.Auto.PRELOAD_PRESET);
        robot.ballPath.setFire(true);
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive()
                && robot.ballPath.getShotsFired() < balls
                && robot.ballPath.getEmptyFeeds() < balls
                && timer.seconds() < Constants.Auto.FIRE_TIMEOUT_S) {
            robot.update();
            show("fire", "%d of %d shots, launcher %.0f / %.0f tps", robot.ballPath.getShotsFired(), balls,
                    robot.launcher.getVelocityTps(), robot.launcher.getTargetTps());
        }
        robot.ballPath.setFire(false);
        robot.ballPath.update();
        robot.launcher.stop();
    }

    // ---- How the steps work --------------------------------------------------------------------

    /** Drive in a straight line (robot's own frame), holding the heading it started with. */
    private void move(String name, double forwardIn, double rightIn) {
        double distance = Math.hypot(forwardIn, rightIn);
        if (distance < 0.1 || !robot.drive.present) return;
        double axial = forwardIn / distance, lateral = rightIn / distance;
        Pose2D start = robot.localizer.getPose();
        double holdHeading = robot.localizer.getHeading();
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < Constants.Auto.DRIVE_TIMEOUT_S) {
            robot.update();
            Pose2D now = robot.localizer.getPose();
            double travelled = Math.hypot(now.getX(DistanceUnit.INCH) - start.getX(DistanceUnit.INCH),
                    now.getY(DistanceUnit.INCH) - start.getY(DistanceUnit.INCH));
            if (travelled >= distance) break;
            double errorDeg = Math.toDegrees(AngleUnit.normalizeRadians(robot.localizer.getHeading() - holdHeading));
            // Drifted counter-clockwise (positive error) -> turn right (positive yaw).
            double yaw = Constants.Auto.HEADING_KP * errorDeg;
            double p = Constants.Auto.DRIVE_POWER;
            robot.drive.driveRobotCentric(p * axial, p * lateral, yaw, 1.0);
            show(name, "%.1f / %.1f in, heading error %.1f°", travelled, distance, errorDeg);
        }
        robot.drive.stop();
    }

    /** Turn on the spot by this many degrees, counter-clockwise positive, using the IMU. */
    private void turn(String name, double degrees) {
        if (!robot.drive.present || (robot.imu == null && !Constants.USE_PINPOINT)) return;   // no heading: skip
        double target = robot.localizer.getHeading() + Math.toRadians(degrees);
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < Constants.Auto.TURN_TIMEOUT_S) {
            robot.update();
            double errorDeg = Math.toDegrees(AngleUnit.normalizeRadians(target - robot.localizer.getHeading()));
            if (Math.abs(errorDeg) < Constants.Auto.TURN_TOLERANCE_DEG) break;
            double power = Range.clip(Constants.Auto.TURN_KP * errorDeg,
                    -Constants.Auto.TURN_MAX_POWER, Constants.Auto.TURN_MAX_POWER);
            if (Math.abs(power) < Constants.Auto.TURN_MIN_POWER) power = Math.signum(power) * Constants.Auto.TURN_MIN_POWER;
            // Still to turn counter-clockwise (positive error) -> negative yaw.
            robot.drive.driveRobotCentric(0, 0, -power, 1.0);
            show(name, "%.1f° to go", errorDeg);
        }
        robot.drive.stop();
    }

    private void show(String step, String format, Object... args) {
        telemetry.addData("Step", step);
        telemetry.addData("Progress", format, args);
        telemetry.update();
    }
}
