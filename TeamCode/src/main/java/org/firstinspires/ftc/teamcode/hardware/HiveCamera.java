package org.firstinspires.ftc.teamcode.hardware;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.ArrayList;
import java.util.List;

/**
 * The webcam, looking up at the AprilTags under the HIVE cells.
 *
 * - Optional like every device: without "webcam" in the configuration, present is false and it
 *   simply sees no tags.
 * - The live picture shows on the Driver Station (menu > Camera Stream) while an OpMode is in INIT.
 * - Only the HIVE tags (IDs 30-45, from Constants.Camera) are reported; anything else is ignored.
 * - The camera runs in its own thread. Call close() when the OpMode ends.
 *
 * Not part of Robot on purpose: starting a camera costs time and CPU, so only OpModes that use it
 * build one.
 */
public class HiveCamera {

    public final boolean present;
    private final VisionPortal portal;
    private final AprilTagProcessor tags;

    public HiveCamera(HardwareMap hw) {
        WebcamName webcam = hw.tryGet(WebcamName.class, Constants.Config.WEBCAM);
        present = webcam != null;
        if (!present) {
            portal = null;
            tags = null;
            return;
        }

        AprilTagLibrary.Builder library = new AprilTagLibrary.Builder();
        for (int id = Constants.Camera.RED_FIRST_ID; id <= Constants.Camera.BLUE_LAST_ID; id++) {
            library.addTag(id, (isRed(id) ? "Red" : "Blue") + " HIVE " + id,
                    Constants.Camera.TAG_SIZE_IN, DistanceUnit.INCH);
        }
        tags = new AprilTagProcessor.Builder()
                .setTagLibrary(library.build())
                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                .setLensIntrinsics(Constants.Camera.FX, Constants.Camera.FY, Constants.Camera.CX, Constants.Camera.CY)
                .build();
        portal = new VisionPortal.Builder()
                .setCamera(webcam)
                .setCameraResolution(new Size(Constants.Camera.WIDTH, Constants.Camera.HEIGHT))
                .addProcessor(tags)
                .build();
    }

    /** Every HIVE tag in the latest picture; empty if none or no camera. */
    public List<AprilTagSingleDetection> hiveTags() {
        List<AprilTagSingleDetection> found = new ArrayList<>();
        if (!present) return found;
        for (AprilTagDetection d : tags.getDetections()) {
            // SDK 12 reports single tags and tag clusters; only single tags carry an ID.
            if (d instanceof AprilTagSingleDetection && ((AprilTagSingleDetection) d).metadata != null) {
                found.add((AprilTagSingleDetection) d);
            }
        }
        return found;
    }

    /** The closest HIVE tag with a distance, or null. */
    public AprilTagSingleDetection nearestHiveTag() {
        AprilTagSingleDetection best = null;
        for (AprilTagSingleDetection d : hiveTags()) {
            if (d.ftcPose != null && (best == null || d.ftcPose.range < best.ftcPose.range)) best = d;
        }
        return best;
    }

    public static boolean isRed(int id) {
        return id >= Constants.Camera.RED_FIRST_ID && id <= Constants.Camera.RED_LAST_ID;
    }

    /** e.g. STREAMING when working; anything else while starting up or if something is wrong. */
    public String state() {
        return present ? portal.getCameraState().toString() : "MISSING";
    }

    public void close() {
        if (portal != null) portal.close();
    }
}
