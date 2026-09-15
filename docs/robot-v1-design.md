# Robot v1 Design: Intake and Launcher

First-iteration mechanical and electrical design for BIOBUZZ, built on the Strafer V5 chassis
from parts in [inventory.md](inventory.md). Rule references are to the
[Competition Manual digest](BIOBUZZ-manual-digest.md).

**Goal for v1:** collect Pollen off the floor and launch it into our Hive cell, reliably, from a
fixed spot. Leave and park in auto. Nothing else. Flower placement and Garden scoring come in v2.

![Side view](img/robot-v1-side.svg)

---

## 1. Design decisions and why

| Decision | Choice | Why |
|---|---|---|
| Ball path | Front intake, single-file inclined channel to a rear-top flywheel | Keeps all 4 balls in a line so the count is enforced by geometry (G407), puts the heavy flywheel over the rear wheels, and leaves the front open for the intake. |
| Capacity | Exactly 4 | Matches the rule limit and the 4 preloads. A 5th ball physically cannot enter once the channel is full. |
| Launcher | Single flywheel shaft with two 90 mm wheels, compliant hood | Simplest launcher that handles both ball sizes. One motor. Every part is in the Starter Kit. |
| Hood | Hinged plate with rubber-band preload | Two ball sizes differ by 20 mm. A fixed hood tuned for 91 mm barely touches a 71 mm ball; a hinged hood compresses both. |
| Feeder | One 1 in soft grip wheel on a Smart Servo, continuous mode | Balls are 25 to 41 g. A servo is enough, and it saves a motor port for v2. |
| Intake | Through the frame: front cross bar removed, ball rolls into the bay between the rails, TPU flapper inside the frame pushes it onto a 45° ramp | A 17 in chassis leaves 25 mm of the 18 in cube in front, so no fixed intake can sit ahead of the frame, and the Strafer's channel sits 36 to 68 mm up, right at ball height. Opening the frame is the only way in without a deployable arm. |
| Aiming | Fixed hood angle, variable flywheel speed, drive to a marked spot; AprilTags for distance in v1.5 | Angle adjustment is a second axis to tune. Speed alone covers 3 to 5 ft. |
| Frame | goBILDA channel for the intake, REV 15 mm extrusion for the tower, printed adapter plates between them | Uses what's on the shelf. Printed plates bridge the M4/M3 hole patterns. |

## 2. Geometry and the 18 inch cube

The Strafer is 17 in long and about 15 in wide at the rails. The rails and cross bars are
goBILDA 1120 U-channel, about 32 mm tall, centered on the 52 mm wheel axle, so the frame
occupies roughly **36 to 68 mm above the tiles** (measure yours). A Pollen's center is at
35.5 mm and a Nectar's at 45.5 mm: a ball on the floor meets the frame head on. It cannot
pass under and nothing on a 17 in chassis can reach far enough ahead to lift it over,
because the 18 in cube leaves only 25 mm in front.

So the frame is opened up. Both goBILDA cross bars move to the rear 5 in of the rails, the
front becomes a 264 mm wide bay, and a printed bridge over the bay (two side plates and a
REV 15 mm cross member) restores the frame's stiffness while carrying the intake shaft and
the webcam.

![Intake detail](img/robot-v1-intake.svg)

| Element | Position | Notes |
|---|---|---|
| Bay entry | Rail fronts, 264 mm wide between rails, tiles as floor | Printed funnel guides narrow it to the 110 mm channel over 114 mm. |
| Flapper shaft | 76 mm behind the rail fronts, 108 mm above tiles | Sweep radius 60 mm: front-most tip is 16 mm inside the frame, tips bottom out at 48 mm. Side plates have three shaft holes at 100, 108, 116 mm for tuning. |
| Ramp foot | 114 mm behind the rail fronts, 10 mm above tiles | Behind the sweep, so the ball is caught first and lifted second. |
| Ramp and channel | 45° incline from the ramp foot to the feeder at ~12.4 in back, 8.3 in up | Four 91 mm balls single file need 14.4 in of run; a 45° line of 11.2 in gives 15.8 in. |
| Flywheel axle | 11.3 in | Two 90 mm wheels, wheel top at 13 in. |
| Ball exit | ~14.2 in | Highest point of the robot. |
| Hubs | Standing on the two rear cross bars, 2.7 to 6.6 in | LEDs facing the rear (R606). |
| Battery | Under the channel, 9.6 to 12.9 in back, 2.7 to 4.6 in up | REV holder plate and hook-and-loop strap. |

Nothing extends after the match starts in v1, so the 29 in limit (R105) is not used.

## 3. Launcher sizing

Target: the upward-facing CELL. The Hive pivot is 43.95 in (1.12 m) up, the cell is 20 in wide,
14 in tall, 12 in deep, and its opening faces up and outward. Treat the aim point as about
48 in (1.22 m) high. Ball exits at about 14.2 in (0.36 m), so the ball must climb about 0.86 m.

Projectile speed needed at a 60° launch angle:

| Horizontal distance to aim point | Exit speed |
|---|---|
| 3 ft (0.9 m) | 4.7 m/s |
| 5 ft (1.5 m) | 5.0 m/s |
| 7 ft (2.1 m) | 5.6 m/s |

A ball rolled between a wheel and a hood leaves at about half the wheel surface speed. For 5 m/s
the wheel surface needs about 10 m/s, which on a 90 mm wheel is **about 2100 to 2200 RPM**. The HD Hex
motor runs 6000 RPM bare, so drive the flywheel shaft 1:1 by chain (two 20T sprockets) and run the
motor at roughly a third of full speed under velocity control. That leaves lots of headroom for
spin-up between shots. Do not add an UltraPlanetary stage; 3:1 would cap the wheel at 2000 RPM
with no margin.

Energy check: a 41 g Nectar at 5 m/s carries 0.5 J. Two 90 mm grip wheels at 2100 RPM store
about 5 J, so each shot drops wheel speed roughly 10 percent and the motor recovers it in well
under half a second. If shots get inconsistent, add a third wheel as a flywheel mass.

**Compression:** aim for 10 to 15 mm of squeeze. That means a wheel-to-hood gap near 78 mm for
Nectar and 58 mm for Pollen. The hinged hood does this automatically: set the resting gap at
about 58 mm and let the rubber bands give 20 mm for the big ball. Tune band count with real
balls. This is the number one thing to test the day the game pieces arrive.

## 4. Mechanical detail by subsystem

### 4.1 Intake (through the frame)
- **Frame change.** Unbolt the front 10-hole cross bar and move it to the rear, next to the
  other one, so both sit in the last 5 in of the rails. The rails are now free at the front.
  Reinstall the M4 screws with the nylon patch.
- **Bridge.** Two printed side plates bolt to the goBILDA hole pattern on the rail tops at
  x = 40 to 115 mm from the rail fronts. Each is about 75 × 140 mm and carries a flanged
  8 mm bearing for the flapper shaft, three shaft positions (100, 108, 116 mm), and a slot
  for a REV 15 mm extrusion cut to ~264 mm that spans the bay at ~205 mm height. The webcam
  and the intake top guard mount to this bridge.
- **Shaft and wheels.** 8mm REX shaft across the bay at 76 mm behind the rail fronts, 108 mm
  above the tiles. Three TPU spoke wheels at 40 mm pitch, centered, alternate wheels turned a
  quarter turn, spaced with goBILDA 8 mm spacers. Print from
  `hardware/intake-spokes/spoke_wheel.stl`.
- **Drive.** 117 RPM Yellow Jacket outside one rail, coupled to the shaft through the side
  plate with an 8mm REX hub, or 1:1 by goBILDA sprockets if it needs to sit further back.
  Rotation: tips move down at the front and back along the bottom.
- **Funnel guides.** Two printed wedges on the bay floor edges, flush with the rail fronts,
  taper the 264 mm opening to the 110 mm channel over the first 114 mm. Print in two pieces
  each if they exceed the bed.
- **Capture sequence.** The robot drives into a ball; the ball rolls onto the tiles inside
  the bay. The flapper tips, sweeping down at the front, strike it above center and push it
  back onto the ramp foot. A 71 mm Pollen is squeezed about 15 mm by the tip as it passes the
  ramp foot, a 91 mm Nectar about 35 mm, which is why the spokes are TPU and why the shaft
  height is adjustable: drop the shaft if Pollen slips, raise it if Nectar stalls.
- **Top guard.** A printed or corrugated shroud on the bridge so a ball can't ride up over
  the flapper onto the robot (G407, G409).
- **Flower slot.** The tips sweep to 48 mm, low enough to hook Pollen out of a Flower's
  3.55 in bottom opening when the bay is driven up against the flower base.

### 4.2 Ramp and channel
- Ramp and channel are one 45° incline from the ramp foot (114 mm back, 10 mm up) to the
  feeder. Two side walls of corrugated plastic or printed panels 110 mm apart on REV 15 mm
  extrusion rails; floor is corrugated plastic over the rear cross bars. Balls ride single file.
- The ramp clears the relocated cross bars because they now sit under the channel's upper end.
- Two indexer roller stations along the channel, each a pair of REV 1 in soft grip wheels on
  5 mm hex, chained together and to one NeveRest 19.2. They keep balls moving up and hold them
  against the feeder.
- Hard stop at the feeder end so the last ball waits under the feeder wheel.
- A 2 m distance sensor at the channel mouth reads "channel full" so the driver stops the intake.

### 4.3 Feeder
- One REV 1 in soft grip wheel on a Smart Servo set to continuous rotation, mounted above the
  last ball position, pinching it against the channel floor and pushing it into the flywheel.
- Software only runs the feeder when the flywheel is at speed.

### 4.4 Flywheel and hood
- Shaft: 5 mm hex, 135 mm, in two REV bearing pillow blocks on the tower uprights.
- Wheels: two REV 90 mm grip wheels side by side, centered on the channel.
- Motor: HD Hex bare (no gearbox), mounted below and behind, 20T to 20T #25 chain.
- Hood: printed curved plate, about 120° of arc, 110 mm wide, hinged at its lower edge on a
  5 mm hex shaft in pillow blocks, pulled toward the wheel by two to four #33 rubber bands on
  printed hooks. Exit angle about 60°. Print the hood in PETG in two halves if it exceeds the bed.
- Guard: a flat cover over the top of the flywheel so nothing can drop onto it from above.

### 4.5 Tower and mounting
- Four REV 420 mm extrusion uprights (cut to length) and 225 mm cross members form the tower
  over the rear half of the chassis, with its feet on the two relocated cross bars. Bolt to the Strafer rails through printed adapter plates that carry the goBILDA
  8 mm hole pattern on one face and REV M3 slots on the other.
- Keep every printed plate under 200 mm in its longest dimension so it fits the bed with margin.

### 4.6 Electronics deck
- Control Hub and Expansion Hub on a printed or corrugated plate spanning the two rear cross
  bars, standing on edge with LEDs and ports facing the rear so inspectors can see them.
- Battery on a REV battery holder plate under the channel just ahead of the hubs, strapped with
  hook and loop. Keep the flywheel chain guarded on that side.
- Main switch on the top rear where the driver can reach it, away from the flywheel.
- Webcam on the intake bridge, tilted up about 30°, looking forward and up at the Hive cells.

## 5. Electrical

![Electrical block diagram](img/robot-v1-electrical.svg)

### Port map

| Hub | Port | Device | Wire | Notes |
|---|---|---|---|---|
| Control Hub | Motor 0 | front_left 312 RPM | goBILDA leads, 22 AWG | encoder to Enc 0 |
| Control Hub | Motor 1 | front_right 312 RPM | | encoder to Enc 1 |
| Control Hub | Motor 2 | back_left 312 RPM | | encoder to Enc 2 |
| Control Hub | Motor 3 | back_right 312 RPM | | encoder to Enc 3 |
| Control Hub | Servo 0 | feeder Smart Servo (continuous) | 36 in PWM | program with SRS Programmer |
| Control Hub | I2C 1 | Color Sensor V3 at intake mouth | 50 cm sensor cable | tells Pollen from Nectar |
| Control Hub | I2C 2 | 2 m Distance Sensor at channel top | 100 cm sensor cable | "channel full" |
| Control Hub | I2C 0 | IMU | internal | heading |
| Control Hub | USB | Logitech C920 | | AprilTags |
| Expansion Hub | Motor 0 | intake 117 RPM Yellow Jacket | goBILDA bullet-to-VH adaptor (buy) | encoder optional |
| Expansion Hub | Motor 1 | indexer NeveRest 19.2 | AndyMark 12 in power + encoder cable | |
| Expansion Hub | Motor 2 | flywheel HD Hex | REV JST VH motor cable | encoder required for velocity control |
| Expansion Hub | Motor 3 | spare | | v2 Flower lift |

### Power chain
Battery → REV switch cable → Control Hub XT30 in. Control Hub XT30 out → 50 cm XT30 extension →
Expansion Hub. RS485 3-pin cable between hubs. One resistive grounding strap from a Control Hub
XT30 to the goBILDA frame (R605). Use red/yellow for positive and black for negative on the 12 V
bus (R610). Everything on the 12 V bus is 18 AWG or heavier; the REV and goBILDA cables already are.

### Configuration checklist
- Rename the Control Hub `<team number>-RC` and the Driver Hub `<team number>-DS` (R705).
- Change the Control Hub Wi-Fi password from the default (R711).
- Driver Station config: 4 drive motors as now, plus `intake`, `indexer`, `flywheel` on the
  Expansion Hub, `feeder` servo, `intake_color`, `hopper_distance`, `Webcam 1`.
- Set the feeder Smart Servo to continuous mode with the SRS Programmer before wiring it in.

### Budget against the rules

| | Used in v1 | Limit | Left for v2 |
|---|---:|---:|---:|
| Motors | 7 | 8 | 1 |
| Servos | 1 | 8 | 7 |
| Hubs | 2 | 2 | 0 |
| USB devices | 1 | webcams only | more webcams |

## 6. Parts pulled from inventory

| Subsystem | Parts |
|---|---|
| Intake | 117 RPM Yellow Jacket; 2 goBILDA 8 mm flanged bearings (**buy**); 8mm REX shaft ~300 mm to span the bay (**buy**); 8mm REX hub (**buy**); 3 printed spoke wheels; goBILDA 8 mm spacers; 1 REV 420 mm extrusion cut to 264 mm for the bridge; the relocated front cross bar |
| Channel | 4 × REV 420 mm extrusion, 4 × 225 mm, 2 corrugated sheets, 90° and inside-corner brackets |
| Indexer | NeveRest 19.2, 4 × REV 1 in soft grip wheels, 2 × 90 mm hex shaft, 4 pillow blocks, #25 chain, 10T and 15T sprockets, shaft collars |
| Feeder | 1 Smart Robot Servo V2, 1 × 1 in grip wheel, servo shaft adapter, metal bent servo bracket |
| Flywheel | HD Hex motor (gearbox removed), 2 × 90 mm grip wheels, 135 mm hex shaft, 2 bearing pillow blocks, 2 × 20T sprockets, #25 chain, high strength hex hubs, shaft collars |
| Hood | printed PETG hood, 75 mm hex shaft hinge, 2 pillow blocks, #33 rubber bands, printed hooks |
| Electronics | Control Hub, Expansion Hub, 1 Slim battery, switch cable, XT30 50 cm, RS485 50 cm, grounding strap, 36 in PWM, sensor cables, C920, battery holder plate, hook and loop |

### Buy list (small)
1. goBILDA 8mm REX shaft, 300 mm or longer (the bay is 264 mm plus two side plates), 1 to 2.
2. goBILDA 1611 flanged bearings 8 mm ID, 2 to 4.
3. goBILDA motor power adaptor 3801-0613-0100 and encoder cable 3801-0709-0600, 2 each.
4. goBILDA 8mm REX hub or clamping collar to lock the intake motor to the shaft, 1.
5. PETG and TPU filament.

## 7. 3D printed parts list

| Part | Material | Qty | Size check |
|---|---|---:|---|
| Intake spoke wheel | TPU 95A | 3 | 120 × 20 mm, done |
| Intake side plate: 8 mm bearing pocket, 3 shaft heights, bridge slot, motor mount | PETG | 2 | ~75 × 140 mm |
| Funnel guide wedges for the bay floor | PETG | 2 (4 halves) | each half < 120 mm |
| Intake top guard | PETG or corrugated plastic | 1 | print in 2 pieces if > 200 mm |
| goBILDA-to-REV adapter plate (8 mm pattern to 15 mm slots) | PETG | 4 | ~96 × 48 mm |
| Hood, curved, hinged | PETG | 1 (2 halves) | each half < 130 mm |
| Rubber band hooks | PETG | 4 | small |
| Hub mounting plate | PETG | 1 | ~180 × 120 mm, or use corrugated plastic |
| Webcam mount, 30° up | PETG | 1 | small |
| Robot sign holders | PETG | 2 | 6.5 × 2.5 in sign, R401 |

## 8. Build order and tests

1. **Electronics first.** Mount both hubs and the battery, wire the power chain, rename devices,
   run the existing TeleOp from the new layout. Self-inspect with the checklist. One session.
2. **Open the frame and build the intake.** Move the front cross bar to the rear, fit the side
   plates and bridge, shaft, three spoke wheels, motor, funnel guides, and a short test ramp.
   Test with game pieces on blocks first, then on the floor. Confirm both ball sizes are
   caught and climb the ramp; move the shaft between the three heights to tune.
3. **Flywheel on a bench rig.** Build the flywheel, hood, and feeder on a scrap frame before the
   tower exists. Shoot at a mark 4 ft up from 3, 4, and 5 ft away. Record RPM vs distance for
   both ball sizes. Tune rubber bands. This decides the final hood angle.
4. **Tower and channel.** Build the tower on the chassis, mount the launcher at 15 in axle
   height, add the channel and indexer, connect to the intake ramp.
5. **Integration drive tests.** Fill to 4, drive, shoot 4, repeat. Fix jams.
6. **Auto.** Drive off the wall (LEAVE, 3 pts), shoot 4 preloads, park in the LOADING ZONE
   (5 pts). That is 8 pts plus up to a tip, and most of the SWARM ranking point.

## 9. Risks to watch

- **Frame stiffness with the front bar removed.** The bridge replaces it 200 mm higher, which
  is a weaker geometry. If the rails flex, add a second REV cross member lower on the side
  plates, above ball height (over ~100 mm), or a corrugated floor plate tying the rails
  together behind the ramp foot.
- **Two ball sizes through one flapper.** A Nectar is squeezed 35 mm passing the ramp foot. If
  it stalls, raise the shaft to 116 mm or shorten the spoke tips; if Pollen then slips, the
  answer is a fourth, offset wheel rather than a lower shaft.
- **Two ball sizes through one launcher.** Mitigated by the hinged hood; test early.
- **Balls jamming** where the funnel meets the channel, or two balls entering side by side. Keep
  the funnel taper gentle and the walls smooth. Corrugated plastic is fine, plywood is not.
- **Flywheel chain tension** on 1:1 sprockets with no idler. Use the REV tensioning bushings.
- **Control Hub USB and the webcam.** Only one camera in v1. Confirm the C920 enumerates before
  building the mount.
- **Cable strain** at the intake motor. Route along the rail with cable clips; the intake takes hits.
- **Weight bias.** Battery and hubs low and rear balance the front-heavy intake. Check that the
  robot doesn't lift a front wheel under hard braking after the tower goes on.
