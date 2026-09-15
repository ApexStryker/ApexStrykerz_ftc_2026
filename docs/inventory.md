# ApexStrykerz Parts Inventory (September 2026)

Compiled from four orders: the REV Robotics order, the DigiKey order, and the mechanical team's
goBILDA and AndyMark order. Kit contents come from each vendor's published bill of materials.
Legal limits from the BIOBUZZ manual: **8 motors, 8 servos, 1 battery, 1 Control Hub (+ up to 1 Expansion Hub)** on the robot.

Assumption to confirm: the goBILDA Strafer kit appears on two lists. This inventory counts **one** kit,
the one already built. If a second unopened kit exists, add 4 more 312 RPM motors and 4 mecanum wheels.

---

## 1. Control system and power

| Item | SKU | Qty | Source | Notes |
|---|---|---:|---|---|
| REV Control Hub | REV-31-1595 | 2 | REV order, DigiKey | Only one may be on the robot. Second is a spare. Built-in IMU. |
| REV Expansion Hub | REV-31-1153 | 1 | DigiKey | Adds 4 motor + 6 servo ports. |
| REV Driver Hub | REV-31-1596 | 2 | REV order, DigiKey | Driver Station. One spare. |
| Driver Hub Battery | REV-31-1876 | 2 | REV order, DigiKey | |
| REV USB PS4-compatible Gamepad | | 2 | REV order | |
| 12V Slim Battery | REV-31-1302 | 3 | 2 REV order, 1 in Starter Kit | Legal main battery. |
| AndyMark FTC Flat Pack Battery | am-5290 | 1 | Robits kit | Legal main battery. 4 batteries total. |
| REV Battery Charger | REV-31-1299 | 2 | REV order, Starter Kit | |
| AndyMark FTC Battery Charger | am-5473 | 1 | Robits kit | |
| REV Switch Cable and Bracket | REV-31-1387 | 3 | REV order, DigiKey, Cable Bundle | Legal main power switch. |
| AndyMark FTC Power Switch w/ bracket | am-4969 | 1 | Robits kit | Legal main power switch. |
| SRS Programmer | REV-31-1108 | 2 | REV order, Starter Kit | Programs Smart Robot Servos. |
| Logitech C920 webcam | C920 | 1 | DigiKey order | UVC, legal for AprilTag vision. |

## 2. Motors (12 total, 8 allowed on the robot)

| Motor | SKU | Qty | Output | Where it came from | Good for |
|---|---|---:|---|---|---|
| goBILDA 5203 Yellow Jacket 19.2:1, 312 RPM, 8mm REX | 5203-2402-0019 | 4 | 312 RPM, 24.3 kg·cm | Strafer kit | Drivetrain (installed) |
| goBILDA 5203 Yellow Jacket 50.9:1, 117 RPM, 8mm REX | 5203-2402-0051 | 1 | 117 RPM, 62 kg·cm | DigiKey | Intake roller or feeder |
| REV HD Hex Motor + UltraPlanetary gearbox kit | REV-41-1600 | 3 | 6000 RPM bare, gearbox stages 3:1, 4:1, 5:1 mix and match | Starter Kit | Flywheel (bare or 3:1), lift |
| REV Core Hex Motor | REV-41-1300 | 1 | 125 RPM, 3.2 N·m | Starter Kit | Slow mechanisms |
| AndyMark NeveRest Hex 19.2 | am-5442 | 2 | 340 RPM | Robits kit | Intake, feeder |
| AndyMark NeveRest Hex 50.9 | am-5443 | 1 | 128 RPM | Robits kit | Slow mechanisms |

All are on the legal motor list (R501). Mixed shaft standards: goBILDA is 8mm REX, REV is 5mm hex, AndyMark NeveRest Hex is 5mm hex (the Robits system then adapts to 3/8 in hex).

## 3. Servos (10 total, 8 allowed)

| Servo | SKU | Qty | Source | Notes |
|---|---|---:|---|---|
| REV Smart Robot Servo V2 (Balanced) | REV-41-3334 | 6 | 3 Starter Kit, 2 REV order, 1 DigiKey | Programmable, continuous or angular. Legal. |
| AndyMark Programmable Torque Servo | am-4954a | 2 | Robits kit | Legal (listed as example in R502). |
| AndyMark Programmable Speed Servo | am-4955a | 2 | Robits kit | Confirm ≤ 8 W / ≤ 4 A at inspection. |
| Servo accessories | | | Starter Kit | 2 Aluminum Servo Horns V2, 2 Aluminum Double Servo Arms, 4 Servo Gear Adapters, 4 Servo Shaft Adapters |

## 4. Sensors (FTC Sensor Bundle REV-45-1885, plus hubs)

| Sensor | SKU | Qty | Use |
|---|---|---:|---|
| 2m Distance Sensor | REV-31-1505 | 2 | Ball detection in hopper, wall distance |
| Color Sensor V3 | REV-31-1557 | 2 | Pollen vs Nectar, our color vs theirs |
| Touch Sensor | REV-31-1425 | 2 | Limit switches |
| Magnetic Limit Switch | REV-31-1462 | 2 | Limit switches |
| Digital LED Indicator (red/green) | REV-31-2010 | 4 | Driver feedback |
| IMU | built into Control Hub | 1 | Heading |
| Webcam | Logitech C920 | 1 | AprilTags on the Hive cells |

## 5. Cables and wiring (FTC Cable Bundle REV-45-1901 plus kits)

| Item | SKU | Qty |
|---|---|---:|
| 36 in PWM cable | REV-11-1130 | 12 (2 packs bundle + 1 pack kit) |
| 24 in PWM cable | am-0694a | 2 |
| 36 in PWM cable | am-0695b | 2 |
| PWM cable clips | REV-11-1229 | 10 |
| Resistive grounding strap | REV-31-1269 | 2 |
| Sensor splitter cable | REV-31-1386 | 2 |
| JST PH 4-pin joiner board | REV-31-1388 | 12 (8 bundle + 4 sensor bundle) |
| XT30 extension 30 cm | REV-31-1392 | 2 |
| XT30 extension 50 cm | REV-31-1394 | 4 |
| JST PH 4-pin sensor cable 50 cm | REV-31-1408 | 8 |
| JST PH 4-pin sensor cable 100 cm | REV-31-1409 | 8 (4 bundle + 4 sensor bundle) |
| JST VH 2-pin motor cable | REV-31-1413 | 8 |
| JST VH 2-pin motor cable 100 cm | REV-31-1526 | 4 |
| JST VH 2-pin joiner board | REV-31-1429 | 8 |
| JST PH 3-pin comm cable 50 cm | REV-31-1418 | 4 |
| JST PH 3-pin comm cable 100 cm | REV-31-1565 | 2 |
| goBILDA encoder cable 600 mm (JST PH to XH) | 3801-0709-0600 | 4 (in use on drive motors) |
| goBILDA JST VH to 3.5mm bullet motor adaptor 100 mm | 3801-0613-0100 | 4 (in use) |
| AndyMark 12 in motor encoder cable | am-3926b-1200 | 3 |
| AndyMark 12 in motor power wire | am-5198-1200 | 3 |
| Zip ties 160 mm | REV-41-1161 | 100 |
| Cable ties 4 in | am-1589 | 100 |

**Gap:** the 117 RPM goBILDA motor and the REV/AndyMark motors need their own power and encoder leads. The goBILDA motor ships with bullet leads; you have zero spare goBILDA bullet-to-JST VH adaptors and zero spare goBILDA encoder cables. Order 2 to 4 of each (3801-0613-0100 and 3801-0709-0600) when buying anything else from goBILDA.

## 6. Structure

### goBILDA (in the built chassis)
| Item | SKU | Qty |
|---|---|---:|
| 1120 U-Channel 10 hole, 264 mm | 1120-0010-0264 | 2 |
| 1120 U-Channel 17 hole, 432 mm | 1120-0017-0432 | 2 |
| Quad Block Pattern Mount | 1201-0043-0002 | 8 |
| M4 thread-locking screws 8 mm / 10 mm | 2822-0004-0008 / -0010 | 25 / 75 (mostly used) |

### REV 15 mm system (FTC Starter Kit V3.1)
| Item | SKU | Qty |
|---|---|---:|
| 15mm Extrusion 420 mm | REV-41-1432 | 16 |
| 15mm Extrusion 225 mm | REV-41-1431 | 8 |
| 15mm Extrusion 120 mm | REV-41-1568 | 10 |
| 45x15 mm C Channel 408 mm | REV-41-1762 | 4 |
| 45x15 mm C Channel 248 mm | REV-41-1767 | 2 |
| U Channel Endcap | REV-41-1687 | 4 |
| Corrugated plastic sheet 450x300x4 mm | REV-41-1839 | 2 |
| Plastic 90° bracket | REV-41-1305 | 32 |
| Metal 90° bracket V2 | REV-41-1480 | 16 |
| Plastic 60° / 45° / 30° / 120° brackets | REV-41-1306/1307/1308/1311 | 8 / 8 / 8 / 6 |
| Inside corner bracket | REV-41-1320 | 16 |
| Lap corner bracket | REV-41-1321 | 24 |
| Motion bracket / Rod end bracket | REV-41-1303 / -1304 | 8 / 8 |
| Gearbox motion bracket | REV-41-1315 | 4 |
| Variable angle bracket | REV-41-1318 | 4 |
| Plastic servo bracket / Metal bent servo bracket | REV-41-1319 / -1485 | 2 / 4 |
| Bent Core Hex motor bracket | REV-41-1433 | 8 |
| UltraPlanetary outside / bent mounting brackets | REV-41-1621 / -1623 | 4 / 4 |
| Battery holder plate | REV-41-1166 | 2 |
| 15mm Hinge kit | REV-45-1259 | 1 |
| M3 standoffs 30 / 40 / 45 mm | REV-41-1491/1492/1493 | 8 / 8 / 8 |
| M3 hardware | | 500 × 8 mm, 100 × 16 mm, 100 × 20 mm, 50 × 35 mm screws; 500 nyloc nuts; 50 T-slot screws |

### AndyMark Robits (1/2 in grid, 10-32 hardware)
| Item | SKU | Qty |
|---|---|---:|
| 0.5 in square tube: 2 / 4 / 6 / 8 / 10 / 12 / 14 / 16 in | am-5001-xxxx | 4 / 4 / 4 / 4 / 6 / 4 / 2 / 2 |
| 1 in square tube 15.5 in | am-5002-1550 | 4 |
| Beams 1×4 / 1×8 / 1×12 / 1×16, slotted | am-5011 | 4 each |
| Gussets: 45°, 135°, 2×3 L, 3×3 corner, 5×5 corner, 3×3 U | am-5010 / 5004 / 5005 / 5008 | 4 / 4 / 8 / 8 / 4 / 4 |
| Plates 2×4 / 4×4 / 4×8 | am-5006 | 4 / 4 / 2 |
| Perforated polycarbonate 15.5 × 6 × 1/32 in | am-4964a | 2 |
| Motor mounts (80-100T, side 2-hole) | am-5017 / am-5012 | 3 / 1 |
| Bushing and shaft carriers (end, side, double) | am-5013/5014/5015/5016 | 4 each |
| 10-32 standoffs 1 / 2 / 4 in | am-1701/1702/1704 | 4 / 8 / 4 |
| 10-32 SHCS 1/2 to 2-1/4 in, nylock jam nuts, washers | | ~340 screws, 100 nuts, 100 washers |
| Nylon and aluminum spacers | | 24 of each of 6 sizes |

## 7. Motion components

### REV 5mm hex (Starter Kit)
| Item | SKU | Qty |
|---|---|---:|
| Hex shaft 75 / 90 / 135 / 400 mm | REV-41-1347/1348/1349/1362 | 8 / 12 / 4 / 4 |
| Plastic gears 15T / 30T / 45T / 60T / 72T / 90T / 125T | REV-41-133x | 8 / 4 / 4 / 8 / 4 / 4 / 2 |
| #25 sprockets 10T / 15T / 20T / 40T | REV-41-1338/1339/1340/1343 | 8 / 4 / 4 / 2 |
| #25 roller chain 10 ft, 5 master links, chain tool | REV-41-1365 / -1366 / REV-42-1442 | 1 / 5 / 1 |
| High strength hex hub | REV-41-1147 | 8 |
| Bearing pillow block / hex pillow block | REV-41-1317 / -1316 | 32 / 4 |
| Through-bore bearings short / long | REV-41-1326 / -1329 | 24 / 24 |
| 5mm hex bearing block | REV-41-1683 | 2 |
| Shaft collars | REV-41-1327 | 40 |
| Hex shaft spacers 15 / 3 / 1.5 mm | REV-41-1323/1324/1325 | 24 / 48 / 28 |
| Tensioning bushings 39 mm | REV-41-1702 | 8 |
| Surgical tubing 3 mm | REV-41-1163 | 1 |

### AndyMark 3/8 in hex (Robits)
| Item | SKU | Qty |
|---|---|---:|
| 3/8 hex shaft 3 / 4 / 6 / 8 / 10 in | am-5003 | 2 / 4 / 2 / 1 / 1 |
| 5 mm hex to 3/8 hex shaft adapter | am-4957-0100 | 4 |
| 20DP plastic gears 30T / 40T / 50T / 60T | am-5020 | 6 / 6 / 2 / 2 |
| HTD 5mm 14T pulley halves | am-4960_half | 16 (8 pulleys) |
| HTD 5M belts 45T / 60T / 75T / 121T | am-5209 | 2 each |
| Motion bushings 3/8 hex | am-5021 | 24 |
| #33 rubber bands | am-5024 | 50 |

### goBILDA 8mm REX (in chassis)
| Item | SKU | Qty |
|---|---|---:|
| Shouldered 8mm REX hub-shaft, 20 mm offset | 2110-0608-0002 | 4 (in use) |
| Clamping steel miter gear 24T | 2320-4008-0024 | 8 (in use) |
| 8mm ID spacers 9 mm, flanged bearings 6 mm / 8 mm ID, shims | 1522-0010-0090, 1611-0514-0006/0008, 2807-0811-0500 | 4 / 4 / 4 / 12 (in use) |

## 8. Wheels and rollers

| Wheel | SKU | Qty | Bore | Use |
|---|---|---:|---|---|
| GripForce Mecanum 104 mm, 40A rollers | 3625-0202-0104 | 4 | 8mm REX | Drivetrain (installed) |
| 90 mm Grip Wheel | REV-41-1267 | 4 | 5mm hex | Flywheel candidates |
| 90 mm Traction Wheel | REV-41-1354 | 2 | 5mm hex | Flywheel candidates |
| 90 mm Omni Wheel | REV-41-1190 | 2 | 5mm hex | Guide rollers |
| 1 in Grip Wheel, soft | REV-41-2150 | 8 | 5mm hex | Feeder / indexer rollers |
| Flap Wheel, medium | REV-41-2702 | 8 | 5mm hex | Intake roller alternative |
| 2 in Compliant Wheel, green | am-3571_green | 4 | 3/8 hex | Intake or feeder |
| 3 in Omni Wheel | am-4967a | 2 | 3/8 hex | Guide rollers |
| 3 in Stealth Wheel | am-4718_gray | 2 | 3/8 hex | Flywheel candidates |
| TPU intake spoke wheels (3D printed) | `hardware/intake-spokes/` | as printed | 8mm REX | Intake |

## 9. Tools on hand

Wera 3 mm ball-end hex key, 1.5 mm and 2 mm Allen wrenches, 2 × 5.5 mm nut drivers, 5.5 mm combination wrench, 5/32 in and 2.5 mm ball-end hex drivers, 2.5 mm L-wrench, 3/8 in combination wrench, #25 chain tool, Robits ruler, two storage totes.

---

## What this means for the first robot

- **Actuators are not the constraint.** 12 motors and 10 servos on hand, 8 + 8 allowed. Iteration 1 needs about 7 motors and 3 servos.
- **Three build systems that don't bolt together.** goBILDA (M4, 8mm REX), REV (M3, 5mm hex, 15 mm extrusion), and Robits (10-32, 3/8 hex, 1/2 in grid). Plan on 3D-printed adapter plates where a REV or Robits mechanism meets the goBILDA chassis. The 5 mm to 3/8 in hex adapters let REV motors drive Robits gears and belts.
- **Flywheel launcher is buildable from the box.** An HD Hex motor run bare (6000 RPM) or through a single 3:1 UltraPlanetary stage, driving 90 mm REV grip or traction wheels on 5 mm hex shaft, with the pillow blocks and bearings from the Starter Kit.
- **Intake is covered.** The printed TPU spokes on a REX shaft driven by the 117 RPM goBILDA motor, or REV flap wheels on 5 mm hex driven by a NeveRest 19.2.
- **Vision is covered.** The C920 on a Control Hub USB port is exactly what R707/R708 allow.
- **Buy list before the launcher build:** goBILDA motor cables and encoder cables (see section 5), a second webcam mount or printed bracket, and whatever REX shaft length the intake needs (the chassis kit includes none spare). Everything else for iteration 1 is on the shelf.
