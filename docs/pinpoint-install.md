# Installing the Pinpoint odometry on our Strafer

Step by step, for the **goBILDA 4-Bar Odometry Pack** (SKU 3203-3110-0002) on our
**Strafer chassis with 104 mm GripForce mecanum wheels**. The code is already written
(`localization/PinpointLocalizer.java`); this guide is the hardware, the wiring and the checks.

> **Right pack?** Yes. goBILDA makes the 4-Bar pack for wheels of about 96 to 104 mm, and the
> Swingarm pack for the 140 mm Strafer. Ours has 104 mm wheels.

## What's in the box

| Part | How many | Notes |
|---|---|---|
| 4-Bar Odometry Pod (32 mm omni wheel, 2000-count encoder) | 2 | Arrive assembled, spring already tuned. Do not take them apart. |
| M4 × 6 mm socket head screws | 4 per pod | Hold each pod in a 1120 U-channel. 3 mm hex key. |
| 4-pin JST PH cable, 600 mm | 1 per pod | Pod to Pinpoint. |
| Pinpoint Odometry Computer | 1 | Reads both pods plus its own IMU. |
| Cable and hardware for the Pinpoint | | goBILDA: "all the hardware and cables you need … to connect them to your REV Control Hub." |

Also get: a 3 mm hex key, a ruler or tape measure in **millimetres**, zip ties.

## The words we use

- **X pod**: its wheel rolls **forward and backward**. Its axle runs side to side.
- **Y pod**: its wheel rolls **sideways**. Its axle runs front to back.
- **Robot centre**: the middle of the four wheel axles, which is the middle of the frame.
- **A**: how far **sideways** the X pod's wheel is from the centre, in mm. **Left +, right −.**
- **B**: how far **forward** the Y pod's wheel is from the centre, in mm. **Front +, back −.**

Only those two distances matter. Where a pod sits along its own rolling direction does not.

## 1. Plan where the pods go

Turn the robot upside down. The Strafer frame is goBILDA 1120 U-channel: two long **side
channels** (432 mm) that hold the motors, wheels and corner miter gears, and two short **cross
channels** (264 mm) between them, set in from the ends.

We follow **goBILDA's recommended layout for two pods and a Pinpoint on the Strafer**
(4-Bar Odometry Pod, Product Insight #2):

- **Both pods inside one cross channel, near its two ends**, close to the wheels. We use the
  **rear** cross channel: the front is busy with the intake and balls.
- **Y pod** near one end, wheel rolling **sideways** (along the channel).
- **X pod** near the other end, wheel rolling **forward** (across the channel).
- **Pinpoint on the same cross channel, in the middle**, sticker side up.

goBILDA's reasons: near the wheels the pods are protected, and those spots are rarely the robot's
centre of rotation, which helps accuracy.

> You may also see the pods **in the middle of the long side channels, between the motors**.
> That is goBILDA's layout for **three** pods without a Pinpoint (Product Insight #3), and it
> needs the front cross channel moved forward 48 mm so its screws don't block the pods. Not ours.

## 2. Mount the pods

1. Slide the pod's square mounting block up into the U-channel from below (it fits the 43 mm
   channel in either direction), wheel pointing at the floor, rolling the right way (X or Y).
2. Fix it with its four M4 × 6 mm screws through the channel's side holes.
3. **Cable:** plug the 600 mm cable into the pod first. Route it through a 14 mm hole in the
   channel, or tuck it along the side of the pod as you slide the pod in. It must not stop the
   arm moving.
4. **Height check.** The pod reaches up to **41 mm below the channel**, so the channel must sit
   less than 41 mm above the floor. Robot on the floor: each pod wheel touches the tile with its
   arm **partly pushed up**. Lift the robot: the wheel drops. A wheel that floats above the tile
   means the channel is too high for the pod.
5. Push the robot gently: each pod wheel must roll freely and not rub on the frame or screws.

## 3. Mount the Pinpoint

- **Sticker side up** (goBILDA: "it is important that it be sticker-side up"); otherwise its
  position does not matter.
- goBILDA puts it in the middle of the same cross channel, with two M4 × 20 mm screws and M4
  locknuts (included with the Pinpoint).
- Keep its small button reachable: it resets the position.

## 4. Wire it

1. **X pod → Pinpoint X port**, **Y pod → Pinpoint Y port**, with the 600 mm cables.
2. **Pinpoint → Control Hub I2C port, bus 1.** (Bus 0 is the hub's own IMU.)
3. Route cables along the channels, away from wheels and corner gears. Zip-tie them.

## 5. Add it to the configuration

Driver Hub › Configure Robot › `Apex-1` › Control Hub › **I2C Bus 1** › Add ›
**goBILDA Pinpoint Odometry Computer**, name it **`pinpoint`**, save, activate.

## 6. Measure A and B, put them in the code

Measure from the robot centre to the middle of each pod's wheel, in mm:

- **A** = X pod, sideways: left of centre +, right of centre −.
- **B** = Y pod, forward: in front of centre +, behind −.

Example: X pod near the right end of the rear cross channel, 110 mm right of centre → **A = −110**;
Y pod in the same channel, 150 mm behind centre → **B = −150**. Use your real numbers.

In `Constants.Odometry`:

```java
PINPOINT_POD = GoBildaOdometryPods.goBILDA_4_BAR_POD;   // already set: that's our pack
PINPOINT_X_POD_OFFSET_MM = -110;   // A, your measurement
PINPOINT_Y_POD_OFFSET_MM = -150;   // B, your measurement
```

Leave `USE_PINPOINT = false` for now. Build and install.

## 7. Prove it works

### Odometry Check (Driver Station, Test list)

Guided, with PASS / FAIL and what to change. **Keep the robot still while pressing INIT.**
Gamepad 1: CROSS = next test, CIRCLE = skip. During the spin, any button stops the robot.

| Test | What happens | Pass | If it fails, it tells you |
|---|---|---|---|
| 1 Connected | Waits for READY | Green light | The status in words (which cable) |
| 2 Stays put | 10 s, nobody touching | Drift under 0.2 in and 0.5° | Restart with the robot still |
| 3 Push forward | You push it about a foot | x goes **up**, y hardly moves | Flip `PINPOINT_X_DIR`, or pods swapped |
| 4 Push left | You push it about a foot | y goes **up**, x hardly moves | Flip `PINPOINT_Y_DIR`, or pods swapped |
| 5 Spin | It turns 2 times left by itself | x and y stay within 4 in; Pinpoint and Control Hub IMU agree within 2° | How far A or B is off (in mm); Pinpoint sticker side up? |

### Odometry Path Test (Driver Station, Autonomous list)

The robot drives a path by itself on the Pinpoint and comes back to where it started. Mark the
start with tape. In INIT choose the path with the D-pad (up / down) and the laps (left / right):

| Path | Room needed | Tests |
|---|---|---|
| Square | 4 × 4 ft | Forward, strafe, back, strafe; no turning |
| Diamond | 4 ft ahead, 2 ft each side | Diagonals: driving and strafing at once |
| Square with turns | 4 × 4 ft | A 90° turn at every corner while moving |
| Field loop | 8 × 8 ft | Long straights, turns and a diagonal; start about 2 ft from the back-right corner |

**The robot always thinks it got back**, because it steered there by the Pinpoint. **Measure**
from the robot's centre to the tape mark: goal **½ in or less** (goBILDA). More laps show whether
the error grows. The screen also shows where the drive encoders and the Control Hub IMU think the
robot is, for comparison.

Any button on either gamepad stops it. It also stops itself if it drives away from the target
(a flipped pod direction), leaves the path's box, takes too long, or the Pinpoint stops being
READY. Speeds and limits: `Constants.Auto` (path following) and `Constants.Odometry`.

*Pinpoint Test* (Test list) still shows the raw numbers for a quick look.

## 8. Switch it on

Set `Constants.USE_PINPOINT = true`, build, install. Now:

- Main TeleOp's Pose line comes from the Pinpoint, and the Driver Station no longer shows
  "MISSING: pinpoint".
- **Auto Pathed** appears in the Autonomous list.
- Autonomous steps (`driveForward`, `strafeRight`, …) measure distance with the pods, which
  don't slip like the mecanum wheels.

## Status light

| Light | Means | Do |
|---|---|---|
| Green | Ready | Drive |
| Red | Starting up or calibrating | Keep the robot still |
| Purple | No pods found | Check both pod cables |
| Blue | X pod not found | Check the X pod cable / port |
| Orange | Y pod not found | Check the Y pod cable / port |

The same words appear in Pinpoint Test and in the robot log (tag `Apex`).

## Sources

- [goBILDA Pinpoint user guide (PDF, 3110-0002-0001)](https://www.gobilda.com/content/user_manuals/3110-0002-0001%20User%20Guide.pdf): setup steps, offsets, status light.
- [4-Bar Odometry Pod spec sheet (PDF, 3110-0001-0002)](https://www.gobilda.com/content/spec_sheets/3110-0001-0002_spec-sheet.pdf): drawings of the pod.
- [4-Bar Odometry Pack page (3203-3110-0002)](https://gobilda.com/4-bar-odometry-pack-2-pods-1-pinpoint-computer): contents, wheel sizes, CAD, video.
- [4-Bar Odometry Pod page (3110-0001-0002)](https://gobilda.com/4-bar-odometry-pod-32mm-wheel): screws, cable, encoder, and the
  **Product Insight pictures**: #1 fit inside 1120 channel, **#2 recommended Strafer layout with a Pinpoint**,
  #3 three-pod layout, #5 the 41 mm reach, #6 cable routing.
- [Strafer Chassis Kit, 104 mm GripForce Mecanum Wheels](https://www.gobilda.com/strafer-chassis-kit-104mm-gripforce-mecanum-wheels/): frame channels.
