# ApexStrykerz Software Stack (BIOBUZZ 2026-27)

How the robot code is organized, how to drive it, and what to tune on the robot.
All code lives in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

## What's on the Driver Station

| OpMode | Type | Use it for |
|---|---|---|
| **Main TeleOp** | TeleOp | Matches. Two drivers; the operator runs the ball path by hand (same controls as Ball Path Test, without ±25). |
| **Auto Basic** | Autonomous | Matches. Leaves the wall; can fire the preloads. Picks Main TeleOp next. |
| Strafer TeleOp | TeleOp | Drive-only fallback when something else misbehaves. |
| Motor Direction Test | Test | Checking wheel directions and the IMU after any rebuild. |
| Launcher Tuner | Test | Finding launcher speeds and PIDF on the practice field. |
| Pinpoint Test | Test | Setting up the goBILDA Pinpoint: status in words, x / y / heading, raw pod counts, the four checks. Works before `USE_PINPOINT` is on. |
| Odometry Check | Test | Guided Pinpoint test with PASS / FAIL and what to change: connected, stays put, push forward / left by hand, spin twice on the spot (offset error estimate, Pinpoint vs Control Hub IMU). |
| Odometry Path Test | Autonomous (Test) | The robot drives a path by itself on the Pinpoint (square, diamond, square with turns, 8 ft field loop; 1-5 laps) and returns to a tape mark: measure how far off it ends. Any button stops it; it also stops itself if it drives the wrong way or leaves the path's box. |
| Ball Path Test | Test | Main TeleOp's controls plus D-pad right/left = launcher speed ±25 tps, for tuning. Camera (optional, `webcam`): live picture on the Driver Station during INIT, and the HIVE AprilTags it sees with the nearest one's distance. |
| Config Name Test | Test | Checking every configuration name moves the right part. |
| Auto Example | Autonomous | Hidden (`@Disabled`). A longer step-by-step plan to learn from. |
| Auto Pathed | Autonomous | Appears only once the Pinpoint is installed and `PinpointLocalizer` is written (`USE_PINPOINT = true`). |
| Hardware Port Test | Test | Hidden. Probing ports by number after rewiring. |
| Smart Fire TeleOp | TeleOp | Hidden (`@Disabled`), parked. The first Main TeleOp: BallPath fires one ball per windmill step once the launcher is back at speed, with the automatic 4-ball stop and NECTAR reject. Comes back once the windmill index sensor makes each 90° step exact. |

## Driving: Main TeleOp

**During INIT:** D-pad left = red alliance, D-pad right = blue alliance.

**Gamepad 1, driver** (`opmodes/DriverControls.java`)

| Control | Action |
|---|---|
| Left stick | Drive and strafe. Robot-centric for now: up = the way the robot faces. |
| Right stick X | Rotate |
| Hold left trigger | Precision, 40% speed |
| Hold left bumper | Robot-centric instead (matters once field-centric is back on) |
| Options | Re-zero field-centric. Point the robot away from you first. |
| Share | Debug page on / off |

Field-centric (up always moves away from you) comes back by setting
`Constants.Drive.FIELD_CENTRIC_DEFAULT = true` once the IMU check passes.

**Gamepad 2, operator** (`opmodes/OperatorControls.java`, shared with Ball Path Test)

| Control | Action |
|---|---|
| Hold right bumper | Intake and side wheels pull in, while held |
| Cross (A) | Intake and side wheels on / off |
| Right stick up / down | Intake, side wheels and windmill together, as fast as pushed: up = toward the launcher, down = push balls out |
| Hold right trigger | Shoot: launcher spins up, then the windmill turns once it is at speed (2 s at most) |
| Left trigger | Launcher on / off |
| Left bumper | Windmill on / off |
| D-pad up / down | Launcher speed +100 / −100 tps |
| Circle (B) | Jump to the other speed preset (near / far) |

Feedback: a short rumble means the launcher is ready. Hub LEDs are green when ready, yellow while
spinning up, blue when idle.

## How it fits together

```
opmodes/  MainTeleOp, AutoBase (+ AutoBasic, AutoExample), AutoPathed, StraferTeleOp
              |  each builds one:
hardware/ Robot ── Drivetrain        mecanum math (the only copy)
            ├──── BallPath ─┬─ Intake        roller bar
            │               ├─ Windmill      4-blade feeder, one ball per quarter turn
            │               ├─ Launcher      speed-controlled wheel, ready gate, shot detection
            │               ├─ FlowerWheels  front lateral wheels
            │               └─ BallSensor    intake color sensor (optional)
            ├──── IMU
            └──── Localizer: DriveEncoderLocalizer today; PinpointLocalizer is an empty stub
Constants   every configuration name and tuning number
```

- **Every device is optional.** A missing one shows up on the Driver Station screen as
  `MISSING:` and its part simply does nothing, so a half-wired robot still runs.
- **BallPath** is the coordinator. The operator says what they want (collect, fire, flower,
  clear); BallPath decides what each motor and servo does, so they never fight. Its one hard
  rule: **the windmill never feeds a ball unless the launcher is at speed.**
- **Localizer** is the slot the Pinpoint plugs into. `PinpointLocalizer` is written but off
  (`USE_PINPOINT = false`), so position still comes from the drive encoders and heading from
  the IMU. To switch over: mount the pods, add `pinpoint` on I2C bus 1, put pod type and
  offsets in `Constants.Odometry`, pass the four checks in *Pinpoint Test*, then set
  `USE_PINPOINT = true`.

## Autonomous

No one touches the gamepads after START. During INIT, gamepad 1 D-pad left/right picks the
alliance. An autonomous is a list of steps in `runSteps()`; `AutoBase` provides them:

| Step | What the robot does |
|---|---|
| `driveForward(24)` / `driveBackward(24)` | Drive straight, inches, holding its heading |
| `strafeRight(12)` / `strafeLeft(12)` | Slide sideways, inches |
| `turnLeft(90)` / `turnRight(90)` | Turn on the spot, degrees, using the IMU |
| `intakeOn()` / `intakeOff()` | Start or stop collecting; collecting continues during the next steps |
| `fire(4)` | Spin up and shoot up to 4 balls |
| `pause(1.5)` | Wait, seconds, with everything still running |

Moves are measured from where the robot is, facing the way it faces. To make a new
autonomous, copy `AutoBasic.java`, rename the class and the `@Autonomous` name, and change the
steps. `AutoExample.java` shows a longer plan that mirrors itself for the blue alliance. Distances
come from the drive encoders, which slip a little, so measure on the practice field and adjust.

## Debugging

Two tools, both in every OpMode that uses `Robot` (`util/Debug.java` explains them):

- **Debug page.** Press **SHARE on gamepad 1** in Main TeleOp or Ball Path Test: the Driver
  Station shows every part at once (loop time, battery, drive powers and counts, intake current,
  launcher target and actual speed, windmill, BallPath mode, ball count, position, missing
  devices). Press SHARE again to go back.
- **Event log.** Parts write one line to the robot log, tag `Apex`, when something changes:
  BallPath mode changes, every shot and empty feed, balls in, rejected NECTAR, launcher at speed
  or off speed, windmill step timeouts, Pinpoint status, localizer trust, low battery
  (below 11.5 V) and slow loops (over 60 ms). Read it live with `adb logcat -s Apex`, or after a
  session with `bash tools/robot-sync.sh --logs-only` (saved in `robot-sync/logs/`).

Thresholds live in `Constants.Debug`. Log when something changes, never every loop.

## BallPath modes

| Mode | Intake | Windmill | Launcher | Ends when |
|---|---|---|---|---|
| IDLE | off | holds | as requested | a request |
| COLLECT | in | holds, balls queue | as requested | released, or 4 balls |
| FIRE | gentle in | one step per ball | spun up | released |
| FLOWER | in | holds | as requested | released, or 4 balls |
| CLEAR | out | reverse | off | released |
| REJECT | out | holds | as requested | 0.7 s after opponent NECTAR is seen |

Ball counting, the automatic 4-ball stop (rule G407) and rejecting opponent NECTAR (rule G408)
all need the intake color sensor. Without it, the operator watches the count.

## Wednesday: tune it on the robot

Everything to check is marked `TODO(Wed)` in `Constants.java`. In order:

1. **Servos to continuous mode.** Use the goBILDA programmer on both flower-wheel servos and
   the windmill. On the Driver Hub, change those three entries to *Continuous Rotation Servo*,
   same names.
2. **Names.** Run *Config Name Test*. Check launcher vs intake, and servo_left vs servo_right.
   Fix the names on the Driver Hub if they're swapped.
3. **Wheel directions.** Robot on blocks, run *Motor Direction Test*. Every wheel must roll the
   robot forward and its count must go up. Flip any wrong one in `Constants.Drive`.
4. **IMU mounting.** In the same test, turn the robot left by hand: yaw must go up. If not,
   set `Constants.Hub.LOGO` and `USB` to how the Control Hub is really mounted.
5. **Mechanism directions.** In *Main TeleOp*: intake must pull in, windmill must turn toward
   the launcher, flower wheels must pull inward. Flip `Intake.DIRECTION`, the sign of
   `Windmill.FEED_SPEED`, or `FlowerWheels.LEFT_IN / RIGHT_IN` as needed. Check that a stopped
   windmill blade holds the next ball back.
6. **Windmill step.** With no index switch, tune `Windmill.STEP_MS` until one press of fire
   moves exactly one blade. With the magnetic switch fitted, add it to the configuration as
   `windmill_index` and the step becomes exact.
7. **Launcher speeds.** Run *Launcher Tuner* at a near and a far shooting spot. Put the speeds
   that land in the HIVE cell into `Launcher.PRESETS_TPS`.
8. **Color sensor.** Mount it at the intake mouth, add it as `intake_color`, and check that
   POLLEN, red NECTAR and blue NECTAR read correctly on the Main TeleOp screen.
9. **Autonomous.** Choose a start spot. Set `Auto.LEAVE_DISTANCE_IN`, and turn on
   `Auto.FIRE_PRELOADS` if the HIVE can be reached from there.

After any change to `Constants.java`, rebuild and reinstall
(`./gradlew :TeamCode:assembleDebug`, then Run in Android Studio or `bash tools/robot-sync.sh`).

## Rules the code helps with

| Rule | How |
|---|---|
| G407, max 4 balls | Intake stops itself at 4 (needs color sensor) |
| G408, no opponent NECTAR | Spat straight back out (needs color sensor) |
| G418, flowers | Flower mode only pulls; only POLLEN can come out the bottom, and drivers must never force NECTAR out |
| G403 / G404, no motion between periods or after the buzzer | Every OpMode stops all motors and servos when it ends |
| R704, no streaming dashboards | Telemetry only through the Driver Station; Launcher Tuner logs to the robot log instead |
