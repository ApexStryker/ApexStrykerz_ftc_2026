package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Constants;

/**
 * A servo used as a continuous wheel, whichever way it is configured.
 *
 * The goBILDA dual-mode servos must be switched to continuous mode with the goBILDA programmer.
 * After that, the Driver Station configuration should say "Continuous Rotation Servo". Until it
 * does, this class also accepts a plain "Servo" entry and sends the same signal (position 0.5 is
 * stop for a continuous servo), so nothing breaks during the changeover.
 *
 * Missing from the configuration: present is false and every call does nothing.
 */
public class ContinuousServo {

    public final String name;
    public final boolean present;
    private final CRServo crServo;
    private final Servo servo;
    private double power;

    public ContinuousServo(HardwareMap hw, String name) {
        this.name = name;
        CRServo cr = hw.tryGet(CRServo.class, name);
        Servo s = cr == null ? hw.tryGet(Servo.class, name) : null;
        this.crServo = cr;
        this.servo = s;
        this.present = cr != null || s != null;
        if (cr instanceof CRServoImplEx) {
            ((CRServoImplEx) cr).setPwmRange(new PwmControl.PwmRange(
                    Constants.Servos.CONTINUOUS_PWM_LOW_US, Constants.Servos.CONTINUOUS_PWM_HIGH_US));
        }
        stop();
    }

    /** -1 full one way, 0 stop, +1 full the other way. */
    public void setPower(double p) {
        power = Range.clip(p, -1, 1);
        if (crServo != null) crServo.setPower(power);
        else if (servo != null) servo.setPosition(0.5 + 0.5 * power);
    }

    public void stop() { setPower(0); }

    public double getPower() { return power; }

    /** True if still configured as a plain Servo, which should be changed after programming. */
    public boolean configuredAsPositional() { return servo != null; }
}
