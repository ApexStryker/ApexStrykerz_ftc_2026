# ApexStrykerz Software Stack (BIOBUZZ 2026-27)

How the robot code is organized, how to drive it, and what to tune on the robot.
All code lives in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

## What's on the Driver Station

| OpMode | Type | Use it for |
|---|---|---|
| **Main TeleOp** | TeleOp | Matches. Two drivers. |
| **Auto Basic** | Autonomous | Matches. Leaves the wall; can fire the preloads. Picks Main TeleOp next. |
| Strafer TeleOp | TeleOp | Drive-only fallback when something else misbehaves. |
| Motor Direction Test | Test | Checking wheel directions and the IMU after any rebuild. |
| Launcher Tuner | Test | Finding launcher speeds and PIDF on the practice field. |
| Config Name Test | Test | Checking every configuration name moves the right part. |
| Auto Pathed | Autonomous | Appears only once the Pinpoint is installed (`USE_PINPOINT = true`). |
| Hardware Port Test | Test | Hidden. Probing ports by number after rewiring. |

## Driving: Main TeleOp

**During INIT:** D-pad left = red alliance, D-pad right = blue alliance. The robot needs this
to spit out the other alliance's NECTAR.

**Gamepad 1, driver**

| Control | Action |
|---|---|
| Left stick | Drive and strafe. Field-centric: up always moves away from you. |
| Right stick X | Rotate |
| Hold left bumper | Robot-centric instead |
| Hold left trigger | Precision, 40% speed |
| Options | Re-zero field-centric. Point the robot away from you first. |

**Gamepad 2, operator**

| Control | Action |
|---|---|
| Right bumper | Intake on / off |
| Hold left bumper | Clear a jam: intake and windmill run backward |
| Left trigger | Launcher spin up / down |
| Hold right trigger | Fire. One ball each time the launcher is back at speed. |
| Hold cross (A) | Flower mode: pull POLLEN out of a flower's bottom opening |
| D-pad up / down | Launcher speed preset: near / far |

Feedback: a short rumble means the launcher is ready; a long rumble means the robot holds 4 balls
and the intake stopped itself. Hub LEDs are green when ready, yellow while spinning up, blue when idle.

## How it fits together

```
opmodes/  MainTeleOp, AutoBasic, AutoPathed, StraferTeleOp
              |  each builds one:
hardware/ Robot ── Drivetrain        mecanum math (the only copy)
            ├──── BallPath ─┬─ Intake        roller bar
            │               ├─ Windmill      4-blade feeder, one ball per quarter turn
            │               ├─ Launcher      speed-controlled wheel, ready gate, shot detection
            │               ├─ FlowerWheels  front lateral wheels
            │               └─ BallSensor    intake color sensor (optional)
            ├──── IMU
            └──── Localizer: DriveEncoderLocalizer today, PinpointLocalizer later
Constants   every configuration name and tuning number
```

- **Every device is optional.** A missing one shows up on the Driver Station screen as
  `MISSING:` and its part simply does nothing, so a half-wired robot still runs.
- **BallPath** is the coordinator. The operator says what they want (collect, fire, flower,
  clear); BallPath decides what each motor and servo does, so they never fight. Its one hard
  rule: **the windmill never feeds a ball unless the launcher is at speed.**
- **Localizer** is the slot the Pinpoint plugs into. Today position comes from the drive
  encoders and heading from the IMU; when the Pinpoint arrives, flipping `USE_PINPOINT` is the
  only change.

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
