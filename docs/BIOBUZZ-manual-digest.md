# BIOBUZZ (2026-27) Competition Manual Digest

Team notes from a full read of the **V1 Competition Manual** (published at kickoff, September 2026).
Source of truth is the manual itself: <https://ftc-resources.firstinspires.org/ftc/game/manual>.
Team Updates post every **Thursday**; they change this document. The Q&A opens **Sept 28, 2026**.

Where this digest gives an opinion for our robot, it is marked **[our take]**.

---

## 1. The game in one paragraph

Two alliances of two robots. Each match is **30 s AUTO, 8 s hands-off transition, 2:00 TELEOP**.
Robots collect two kinds of balls, POLLEN and NECTAR, and score them three ways:
**LAUNCH** them into the upward-facing CELL of their HIVE to make it **TIP** (20 pts each tip),
**place** them into the top of FLOWERS in the last minute (2 pts each, only for the alliance that "owns" the flower),
and push them into their **GARDEN** corner (1 pt each). Robots also score for leaving the wall in AUTO and parking in the LOADING ZONE.

## 2. Game pieces (section 9.8)

| Piece | Size | Weight | Count per match | Where they start |
|---|---|---|---|---|
| POLLEN, yellow | 2.8 in / 71 mm | ~25 g | 40 | 4 in each of 4 FLOWERS (16), 4 in each GARDEN (8), 4 preloaded on each robot (16) |
| NECTAR, red or blue | 3.6 in / 91 mm | ~41 g | 8 per alliance | 3 in each alliance's upward CELL (6), 5 held by each alliance's humans (10) |

Both are Gopher ResisDent polyethylene balls, "not perfectly spherical and may vary in size." Design for that.

## 3. Field (section 9)

- 12 ft × 12 ft, 36 soft foam tiles, standard AndyMark perimeter. FIRST Championship fields sit on 24 in risers.
- **HIVE Structure** in the center: a frame 49.5 in wide × 39 in deep at the base. Two HIVES (one red, one blue) each pivot on an axis **43.95 in above the tiles**. Each HIVE has two CELLS 18.8 in apart; only one faces up at a time. A CELL opening is about **20 in wide × 14 in tall × 12 in deep**. The HIVE is bi-stable and tips when enough balls land in the upward cell.
- **AprilTags** (36h11, 3.25 in) are on the underside of every CELL, facing down toward the tiles: IDs 30-33 and 34-37 on the red cells, 38-41 and 42-45 on the blue cells.
- **FLOWERS**: 4 of them, on the perimeter walls. Top opening is a **4 in diameter ring, 21.5 in above the tiles**, with a 1.25 in backstop. A retrieval opening at the bottom (3.55 in tall × 3.57 in deep) lets robots pull POLLEN out, but NECTAR cannot come out.
- **LOADING ZONE**: 23 in × 11 in against the wall by each alliance area. Humans enter NECTAR here; robots PARK here.
- **GARDEN**: a 23 in × 2 in tape strip in opposite corners, one per alliance. Not a protected zone.
- **ALLIANCE AREA**: 97 in × 54 in outside the field where the drive team stands.

## 4. Scoring (Table 10-2)

| Achievement | AUTO | TELEOP |
|---|---|---|
| LEAVE (robot no longer touching perimeter wall) | 3 | |
| PARK (robot at least partially in LOADING ZONE) | 5 | 5 |
| HIVE TIP | 20 | 20 |
| Each POLLEN or NECTAR left in an upward CELL at match end | | 2 |
| Bottom NECTAR bonus (lowest scoring nectar in a flower is yours) | | 5 |
| Each ball in a FLOWER you own | | 2 |
| Each ball in your GARDEN | | 1 |

**Ranking points** (qualification only): WIN 3, TIE 1, **SWARM RP** for LEAVE + PARK points at or above a threshold (16 at regular events), **POLLINATOR 1** for at least 4 tips, **POLLINATOR 2** for at least 7 tips. Championship thresholds are TBA.
Ranking is by average RP, then average match points minus fouls, then average tips, then average AUTO points.

### How the mechanics chain together

1. Every HIVE TIP unlocks **one NECTAR** that a human may throw into the LOADING ZONE (G426). With **60 s left**, all remaining NECTAR may be entered.
2. Humans enter NECTAR by hand only, and it must **touch the tile inside the LOADING ZONE before touching anything else** (G427).
3. NECTAR may only go into a FLOWER in the **last 60 seconds** (G410, major foul per nectar). The manual says flower scoring starts at one minute; earlier placements "are still scored but subject to penalties." Watch the Q&A for whether early POLLEN is penalized.
4. The alliance whose NECTAR is **top-most** in a flower **owns** it and gets 2 pts for every ball in it, no matter who placed them. Lowest NECTAR of your color earns the 5 pt bonus.
5. Balls that spill from a tipped HIVE must hit something before a robot may collect them (G409).

## 5. Gameplay rules that shape the robot

- **G407: never CONTROL more than 4 balls at once.** Momentary 5th is tolerated, 6 is strategic. Guards to stop balls piling on top of the robot are explicitly encouraged.
- **G408:** don't control the opponent's NECTAR.
- **G417:** the only legal way to move a HIVE is launching into the upward cell. No ramming, no launching at the outside of a cell.
- **G418:** POLLEN and NECTAR go in the top of a FLOWER only; only POLLEN comes out, only through the bottom. Contacting the flower while scoring is fine.
- **G405:** don't deliberately eject balls from the field. **G406:** don't gouge or mark the balls.
- **G411:** no hoarding or blocking access to balls. Gardens are not protected.
- **G421:** 3 second pin count. **G419/G420:** no damage, tipping, or entangling. Expect a "highly interactive," high-contact game. Exposed intakes and wiring get no protection from the rules.
- **G402:** stay on your own half in AUTO (columns A-C red, D-F blue).
- **G403/G404:** no powered motion during the 8 s transition or after the buzzer.

### AUTO start (G304)

Robot starts touching the perimeter wall on its own side, not in the LOADING ZONE, not touching a FLOWER, inside the 18 in cube, **touching exactly 4 preloaded POLLEN** (on the robot or on the tiles touching it). An OpMode must be INIT'd before the match (G305), even if there's no autonomous.

## 6. Robot construction rules (section 12)

| Rule | Limit |
|---|---|
| R102 | **18 × 18 × 18 in** starting configuration. Preloads may stick out. |
| R105 | Expansion limit **18 × 24 in footprint, 29 in tall**, at all times, enforced mechanically, not in software. |
| R104 | No weight limit. |
| R503 | **8 motors and 8 servos** total across all configurations. |
| R501 | goBILDA 5201/5202/5203/5204 and 5000 series are legal, plus REV, AndyMark, Studica, Tetrix, etc. |
| R502 | Servos: ≤ 8 W and ≤ 4 A stall at 6 V. Linear servos ≤ 1 A. |
| R601 | Exactly one 12 V NiMH battery from the approved list (goBILDA 3100-0012-0020 is on it). |
| R603 | One approved main power switch (goBILDA Floodgate is legal). |
| R701 | One REV Control Hub (or phone + Expansion Hub), plus at most one Expansion Hub. |
| R707/R708 | USB is for UVC webcams and the **Limelight 3A** only. No stereo cameras. Limelight 3G, OAK, OpenMV are banned. |
| R704.D | **No FTC Dashboard or other streaming tools during matches.** Only the Driver Station app may carry telemetry. No video streams. |
| R705 | Name devices `<team>-RC` and `<team>-DS`. |
| R711 | Change the Control Hub Wi-Fi password. Bluetooth off. |
| R801 | No pneumatics, blowers, vacuums, or fans that move balls. Flywheels and rollers are fine. |
| R301/R303 | No COTS mechanism that solves a game task by itself, except drive chassis and StarterBot parts. COTS mechanisms must be single degree of freedom. |
| R305 | Can't build the robot out of game pieces or replicas. |
| R201 | Nothing that damages tiles or sheds material. Sharp edges and abrasive surfaces get inspected. |

**Robot signs (R401-R403):** at least two, on faces ≥ 90° apart, each with a solid red or blue rectangle at least 6.5 × 2.5 in and white team numbers about 2.25 in tall, not vertically stacked, not illuminated. Must be readable from 12 ft.

## 7. People and event rules

- **Drive team:** up to 4. One may be an adult (the DRIVE COACH). Drivers and the HUMAN PLAYER are students. Badges above the waist.
- Coaches may hold the Driver Station and press INIT/START/STOP, but may not touch gamepads (G423) or balls (G424).
- Safety glasses in the pits and arena. Closed shoes. No team Wi-Fi hotspots or Bluetooth anywhere in the venue (E301).
- Battery chargers: 3 A per channel max, proper polarized connectors only (E511).
- Pits: small benchtop tools only, no grinders or torches, soldering with an electric iron is fine.
- Only one robot may be inspected and played per event (E117).
- Re-inspection is required after most changes except code, fasteners, signs, and identical part swaps (I303).

## 8. Tournament format (section 13)

- 5 or 6 qualification matches per team. Rankings by RANKING SCORE (average RP).
- Alliance selection, then **double-elimination** playoffs. 2 alliances for 4-10 teams, 4 for 11-20, 6 for 21-40, 8 for 41-64.
- Only students may talk to the Head Referee at the question box, max two people, and questions must be asked before alliance selection.
- No video review, ever.
- **Advancement points:** qualification rank (2-16 pts), alliance captain or draft order (21 minus position), playoff finish (40/20/10/5), judged awards (Inspire 60/30/15, other awards 12/6/3). Teams advance from their first three qualifying or league events, in their home region only.
- Registration cutoff that affects championship slot allocation: **Nov 17, 2026**.

## 9. Judging (section 6)

- **Portfolio:** cover page + **15 pages max**, US Letter or A4, under 15 MB if digital, content since **Jan 1, 2026** only. First names and last initials only. Judges won't click links.
- **Initial interview:** at least 10 minutes, first 5 for an uninterrupted presentation, at least 2 students, one silent adult observer allowed. Two standard questions come from a published question bank.
- Awards: Inspire (needs portfolio and strength in all three categories), Think (portfolio), Connect / Reach / Sustain (team attributes), Innovate / Control / Design (machine). Control Award **requires** a portfolio section on sensors and software. A team can win only one judged award per event.

## 10. Match timeline cheat sheet

| Clock | What happens |
|---|---|
| 2:30 | Match starts. AUTO. No touching controllers. |
| 2:00 | AUTO ends, 3 buzzers. 8 s hands-off transition. Tips completed before TELEOP count as AUTO. |
| 1:52 | TELEOP begins. |
| 1:00 | FLOWER ownership unlocked. All remaining NECTAR may be entered. NECTAR may go into flowers. |
| 0:20 | Train whistle. |
| 0:00 | Robots stop. Cells, flowers, gardens, and TELEOP PARK are counted once everything settles. |

## 11. What this means for our robot [our take]

- **Two ball sizes, 71 and 91 mm.** Every intake, hopper, and shooter path must pass a 91 mm ball with margin and still grip a 71 mm one.
- **Capacity is capped at 4.** Design the hopper for exactly 4 and add guards so a 5th can't ride on top. This also matches the 4 preloaded POLLEN.
- **A launcher is required to score the big points.** The HIVE pivot is at 44 in and the cell opening is above that; our robot may only be 29 in tall when extended. Nothing else earns 20 pts a shot, and 4 tips is a ranking point.
- **Flower scoring needs a lift and precision.** The 4 in opening at 21.5 in is only 0.4 in wider than a NECTAR. A ball dropped from above with a guide is realistic; a shot is not.
- **Pulling POLLEN out of a flower bottom** means a ground-level intake that can reach into a 3.55 in tall slot against the wall. Our intake spokes are pointed at exactly this.
- **AprilTags face downward** from the cells, so a camera looking up at the hive is how you localize for shots. Only UVC webcams or a Limelight 3A are legal.
- **Programmers: no FTC Dashboard at competition.** Keep tuning tools switchable and use Driver Station telemetry in matches.
- **Robust matters.** The manual repeats that this is a contact game and that fragile intakes are your own problem.
- **Auto priorities:** LEAVE (3) + PARK (5) gets most of the SWARM RP by itself; tipping the hive with the 4 preloads is the big auto prize.
