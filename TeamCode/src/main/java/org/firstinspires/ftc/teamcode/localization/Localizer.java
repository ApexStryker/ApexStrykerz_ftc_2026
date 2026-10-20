package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/**
 * Where the robot is on the field. The seam the goBILDA Pinpoint drops into: everything else
 * uses this interface, so switching from drive encoders to the Pinpoint is one line in Robot.
 *
 * Field frame: x forward, y left, heading counter-clockwise positive, all from the start pose
 * unless setPose says otherwise.
 */
public interface Localizer {

    /** Call once per loop, before anything reads the pose. */
    void update();

    /** Field-frame pose. */
    Pose2D getPose();

    /** Re-zero, e.g. at the autonomous start position or from an AprilTag fix. */
    void setPose(Pose2D pose);

    /** Heading in radians, normalized to (-pi, pi]. */
    double getHeading();

    /** false once the estimate can no longer be relied on (drift, missing hardware). */
    boolean isTrusted();

    /** Short name for telemetry. */
    String name();

    /** Extra detail for the debug page (raw counts, status). */
    default String debugInfo() { return ""; }
}
