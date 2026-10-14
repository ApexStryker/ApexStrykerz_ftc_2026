package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.Constants;

/** The front roller bar. Missing from the configuration: present is false and calls do nothing. */
public class Intake {

    public final boolean present;
    private final DcMotorEx motor;
    private double power;

    public Intake(HardwareMap hw) {
        motor = hw.tryGet(DcMotorEx.class, Constants.Config.INTAKE);
        present = motor != null;
        if (present) {
            motor.setDirection(Constants.Intake.DIRECTION);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setPower(0);
        }
    }

    public void in() { set(Constants.Intake.IN_POWER); }
    public void out() { set(Constants.Intake.OUT_POWER); }
    public void feed() { set(Constants.Intake.FEED_POWER); }
    public void stop() { set(0); }

    public void set(double p) {
        power = p;
        if (present) motor.setPower(p);
    }

    /** Positive when pulling balls in. */
    public double getPower() { return power; }

    public double getCurrentAmps() { return present ? motor.getCurrent(CurrentUnit.AMPS) : 0; }
}
