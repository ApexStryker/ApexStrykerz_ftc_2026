package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Constants;

/**
 * Simple autonomous that needs no odometry hardware. A starting point to grow from.
 * The steps (driveForward, fire, ...) are explained in AutoBase.
 *
 *   1. (Optional, Constants.Auto.FIRE_PRELOADS) fire the 4 preloaded POLLEN at the HIVE from
 *      the starting spot. Off until a start spot that reaches the HIVE is chosen.
 *   2. Drive straight forward LEAVE_DISTANCE_IN inches. Earns LEAVE (3 points): no longer
 *      touching the wall.
 *   3. Stop and wait for TeleOp.
 *
 * Start position (rule G304): robot touching the perimeter wall on our side, not in the
 * LOADING ZONE, not touching a FLOWER, facing the open field, with 4 POLLEN preloaded.
 */
@Autonomous(name = "Auto Basic", group = "ApexStrykerz", preselectTeleOp = "Main TeleOp")
public class AutoBasic extends AutoBase {

    @Override
    protected String plan() {
        return (Constants.Auto.FIRE_PRELOADS ? "fire " + Constants.Auto.PRELOADS + ", then " : "")
                + "drive forward " + Constants.Auto.LEAVE_DISTANCE_IN + " in";
    }

    @Override
    protected void runSteps() {
        if (Constants.Auto.FIRE_PRELOADS) fire(Constants.Auto.PRELOADS);
        driveForward(Constants.Auto.LEAVE_DISTANCE_IN);

        // TODO: park in the LOADING ZONE (5 points) once the team picks a start spot, e.g.
        //   turnLeft(90);
        //   driveForward(36);
    }
}
