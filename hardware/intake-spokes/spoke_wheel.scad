// ApexStrykerz - BIOBUZZ intake spoke wheel
// ---------------------------------------------------------------
// A two-spoke paddle wheel that mounts on a REV 5 mm hex shaft (or, with
// bore = "rex8", a goBILDA 8mm REX shaft) and
// sweeps POLLEN (2.8 in / 71 mm) and NECTAR (3.6 in / 91 mm) balls into
// the robot. Print in TPU so the spokes flex instead of jamming.
//
// Export the printable part:
//   openscad -o spoke_wheel.stl -D 'part="wheel"' spoke_wheel.scad
// Render the assembly preview (3 wheels on a shaft with both balls):
//   openscad -o preview.png -D 'part="assembly"' --autocenter --viewall spoke_wheel.scad
//
// Every dimension is in millimetres.
// ---------------------------------------------------------------

part = "wheel";          // "wheel" = printable part, "assembly" = preview only, "profile" = 2D outline

// ---- Game pieces (2026-27 BIOBUZZ Competition Manual, section 9.8) ----
pollen_d = 71;           // 2.8 in, yellow, ~25 g
nectar_d = 91;           // 3.6 in, red/blue, ~41 g

// ---- Spoke wheel geometry ----
spokes        = 2;       // number of spokes, evenly spaced
tip_radius    = 60;      // shaft centre to spoke tip (tip-to-tip = 120)
width         = 20;      // length along the shaft
hub_d         = 24;      // hub outside diameter
root_t        = 6;       // spoke thickness where it leaves the hub
tip_t         = 4;       // spoke thickness at the rounded tip
fillet_r      = 6;       // radius of the fillet between spoke and hub

// ---- Bore ----
bore          = "hex5";  // "hex5" = REV 5 mm hex shaft (what we have), "rex8" = goBILDA 8mm REX
hex5_af       = 5;       // REV hex shaft across flats
rex_round_d   = 8;       // goBILDA REX: an 8 mm round intersected with a 7 mm hex
rex_hex_af    = 7;
bore_clearance = -0.10;  // TPU: slight interference grips the shaft. PETG/PLA: use +0.15.

// ---- Where the shaft should sit on the robot (for the assembly preview) ----
// Spoke tips should meet the ball a little above its centre so the ball is
// pushed down and in, not popped up. 60% of the average ball diameter:
contact_h    = 0.6 * (pollen_d + nectar_d) / 2;    // = 48.6 mm above the tiles
shaft_h      = contact_h + tip_radius;             // = 108.6 mm shaft centre height
wheel_count  = 3;                                  // wheels shown in the preview
wheel_pitch  = 40;                                 // spacing between wheels in the preview
wheel_angle  = 90;                                 // preview only: 90 puts one spoke straight down

$fn = 128;

// ---------------------------------------------------------------
// 2D building blocks
// ---------------------------------------------------------------
module bore_2d(clearance = 0) {
    // circle($fn=6) puts corners on the circle, so diameter = across_flats / cos(30)
    if (bore == "hex5")
        circle(d = (hex5_af + clearance) / cos(30), $fn = 6);
    else
        intersection() {
            circle(d = rex_round_d + clearance, $fn = 96);
            circle(d = (rex_hex_af + clearance) / cos(30), $fn = 6);
        }
}

module spoke_2d() {
    // Tapered blade with a fully rounded tip: hull of a thin bar at the root and a circle at the tip
    hull() {
        translate([hub_d / 2 - 2, -root_t / 2]) square([0.1, root_t]);
        translate([tip_radius - tip_t / 2, 0]) circle(d = tip_t);
    }
}

module wheel_outline_2d() {
    // offset(+r) then offset(-r) fills concave corners with radius r (the root fillets)
    offset(r = -fillet_r) offset(r = fillet_r)
        union() {
            circle(d = hub_d);
            for (i = [0 : spokes - 1])
                rotate(i * 360 / spokes) spoke_2d();
        }
}

module wheel_profile_2d() {
    difference() {
        wheel_outline_2d();
        bore_2d(bore_clearance);
    }
}

// ---------------------------------------------------------------
// 3D part
// ---------------------------------------------------------------
module spoke_wheel() {
    linear_extrude(height = width, convexity = 4) wheel_profile_2d();
}

// ---------------------------------------------------------------
// Assembly preview: shaft horizontal along Y, robot drives toward +X
// ---------------------------------------------------------------
module shaft(length) {
    color("silver") rotate([-90, 0, 0]) linear_extrude(height = length, center = true)
        bore_2d(0);
}

module sweep_ring() {
    // the circle the spoke tips travel on
    color([0.1, 0.1, 0.1, 0.35]) rotate([90, 0, 0])
        rotate_extrude($fn = 180) translate([tip_radius, 0]) circle(d = 1.2, $fn = 12);
}

module assembly() {
    floor_size = 320;
    // tiles
    color([0.55, 0.55, 0.58]) translate([-floor_size / 2, -floor_size / 2, -3]) cube([floor_size, floor_size, 3]);

    shaft_len = (wheel_count + 1) * wheel_pitch;
    translate([0, 0, shaft_h]) {
        shaft(shaft_len);
        for (i = [0 : wheel_count - 1])
            translate([0, (i - (wheel_count - 1) / 2) * wheel_pitch + width / 2, 0])
                rotate([90, 0, 0]) rotate([0, 0, wheel_angle])
                    color([1.0, 0.54, 0.24]) spoke_wheel();
        translate([0, -shaft_len / 2 - 6, 0]) sweep_ring();
    }

    // game pieces resting on the tiles, just touching the tip circle, in front of the intake
    // (a ball of radius r touches the sweep circle when its centre is tip_radius + r from the shaft)
    for (b = [[pollen_d, -wheel_pitch, [1.0, 0.85, 0.1]], [nectar_d, wheel_pitch, [0.85, 0.15, 0.15]]]) {
        r = b[0] / 2;
        dx = sqrt(pow(tip_radius + r, 2) - pow(shaft_h - r, 2));
        color(b[2]) translate([dx, b[1], r]) sphere(d = b[0], $fn = 72);
    }

    // contact-height marker: where the spoke tips bottom out
    color("black") translate([-90, -shaft_len / 2 - 8, contact_h]) cube([220, 1, 0.8]);
}

// ---------------------------------------------------------------
if (part == "wheel")         spoke_wheel();
else if (part == "assembly") assembly();
else if (part == "profile")  wheel_profile_2d();

echo(str("Bore: ", bore, "   Tip-to-tip diameter: ", 2 * tip_radius, " mm"));
echo(str("Mount the shaft centre ", shaft_h, " mm above the tiles (spoke tips bottom out at ", contact_h, " mm)"));
