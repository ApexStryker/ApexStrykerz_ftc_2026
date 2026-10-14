package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Constants;

/**
 * An example of a longer autonomous written as steps, for learning. The distances are made up:
 * measure the field and change them.
 *
 * It is @Disabled so it never shows on the Driver Station by accident. To try it on the practice
 * field, delete the @Disabled line, rebuild and install.
 */
@Disabled
@Autonomous(name = "Auto Example", group = "ApexStrykerz", preselectTeleOp = "Main TeleOp")
public class AutoExample extends AutoBase {

    @Override
    protected String plan() {
        return "fire 4, leave, collect, come back, fire, park";
    }

    @Override
    protected void runSteps() {
        boolean red = alliance == Constants.Alliance.RED;

        fire(4);                    // shoot the preloads from the start spot
        driveForward(24);           // off the wall: LEAVE points

        if (red) turnLeft(90);      // face the balls; the field is mirrored for blue
        else turnRight(90);

        intakeOn();
        driveForward(18);           // drive through the balls to pick them up
        pause(0.5);                 // let the last ball get in
        intakeOff();
        driveBackward(18);

        if (red) turnRight(90);     // face the HIVE again
        else turnLeft(90);
        driveBackward(24);          // back to the shooting spot
        fire(3);

        strafeRight(24);            // park
    }
}
