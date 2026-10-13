package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/** Small helpers shared by the hardware test OpModes. */
final class TestUtil {
    private TestUtil() {}

    /** Robot log tag for every test OpMode. Read with: adb logcat -s HwTest */
    static final String TAG = "HwTest";

    /** Servo position for a servo button held this many seconds: 0 to 1 and back every 2 s. */
    static double sweep(double heldSeconds) {
        return 0.5 - 0.5 * Math.cos(2 * Math.PI * heldSeconds / 2.0);
    }

    /**
     * Total current drawn by every servo port on one hub, in mA, or -1 if unreadable.
     * Servos send nothing back, so this is the only way to tell from software that a servo is
     * plugged in and powered: a connected servo draws noticeably more while it moves.
     */
    static int servoCurrentMa(LynxModule hub) {
        if (hub == null) return -1;
        try {
            LynxGetADCResponse r = new LynxGetADCCommand(hub,
                    LynxGetADCCommand.Channel.SERVO_CURRENT, LynxGetADCCommand.Mode.ENGINEERING).sendReceive();
            return r.getValue();
        } catch (Exception e) {
            return -1;
        }
    }

    /** The Control Hub (parent) or Expansion Hub module, or null if not configured. */
    static LynxModule hub(HardwareMap map, boolean controlHub) {
        for (LynxModule m : map.getAll(LynxModule.class)) if (m.isParent() == controlHub) return m;
        return null;
    }

    /** True if this controller, motor, or servo belongs to the Control Hub. */
    static boolean onControlHub(HardwareMap map, HardwareDevice d) {
        for (String n : map.getNamesOf(d)) if (n.toLowerCase().contains("control")) return true;
        return false;
    }

    /** "Control Hub port 2" style location of a configured motor or servo. */
    static String where(HardwareMap map, HardwareDevice d) {
        if (d == null) return "NOT FOUND in configuration";
        HardwareDevice controller = null;
        int port = -1;
        if (d instanceof DcMotor) { controller = ((DcMotor) d).getController(); port = ((DcMotor) d).getPortNumber(); }
        if (d instanceof Servo) { controller = ((Servo) d).getController(); port = ((Servo) d).getPortNumber(); }
        String hubName = "?";
        if (controller != null) for (String n : map.getNamesOf(controller)) hubName = n;
        return hubName + " port " + port;
    }

    /** Names of the buttons currently held, e.g. "a dpad_up". */
    static String heldButtons(Gamepad g) {
        StringBuilder sb = new StringBuilder();
        if (g.a) sb.append("a "); if (g.b) sb.append("b "); if (g.x) sb.append("x "); if (g.y) sb.append("y ");
        if (g.dpad_up) sb.append("dpad_up "); if (g.dpad_right) sb.append("dpad_right ");
        if (g.dpad_down) sb.append("dpad_down "); if (g.dpad_left) sb.append("dpad_left ");
        if (g.left_bumper) sb.append("lb "); if (g.right_bumper) sb.append("rb ");
        if (g.left_trigger > 0.5) sb.append("lt "); if (g.right_trigger > 0.5) sb.append("rt ");
        if (g.back) sb.append("back "); if (g.start) sb.append("start ");
        return sb.toString().trim();
    }

    /** Stick value with a small deadzone so a resting stick reads exactly 0. */
    static double deadzone(double v) {
        return Math.abs(v) < 0.10 ? 0 : v;
    }
}
