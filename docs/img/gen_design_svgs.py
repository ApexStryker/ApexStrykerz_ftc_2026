#!/usr/bin/env python3
"""Generates the robot v1 design figures in docs/img. Run: python3 docs/img/gen_design_svgs.py"""
import math, os

OUT = os.path.dirname(os.path.abspath(__file__))
INK = "#1A1611"; ORANGE = "#FF8A3D"; YELLOW = "#F5C518"; BLUE = "#3B82F6"; GREEN = "#22C55E"
RED = "#EF4444"; GRAY = "#9CA3AF"; LIGHT = "#F3EFE7"; CARD = "#FFFDF7"; EDGE = "#E5E0D8"; PURPLE = "#8B5CF6"
FONT = "font-family='Arial, Helvetica, sans-serif'"

def esc(s): return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

class SVG:
    def __init__(self, w, h, title):
        self.w, self.h, self.parts, self.defs, self._id = w, h, [], [], 0
        self.parts.append(f"<rect x='1' y='1' width='{w-2}' height='{h-2}' rx='18' fill='{CARD}' stroke='{EDGE}' stroke-width='2'/>")
        self.text(w/2, 40, title, 24, INK, "middle", "bold")
    def text(self, x, y, s, size=14, fill=INK, anchor="start", weight="normal", extra=""):
        self.parts.append(f"<text x='{x}' y='{y}' font-size='{size}' fill='{fill}' text-anchor='{anchor}' font-weight='{weight}' {FONT} {extra}>{esc(s)}</text>")
    def lines(self, x, y, items, size=13, fill=INK, anchor="start", lh=None, weight="normal"):
        lh = lh or size*1.35
        for i, s in enumerate(items): self.text(x, y+i*lh, s, size, fill, anchor, weight)
    def box(self, x, y, w, h, fill=LIGHT, stroke=INK, rx=8, sw=2, dash=""):
        d = f"stroke-dasharray='{dash}'" if dash else ""
        self.parts.append(f"<rect x='{x}' y='{y}' width='{w}' height='{h}' rx='{rx}' fill='{fill}' stroke='{stroke}' stroke-width='{sw}' {d}/>")
    def circle(self, cx, cy, r, fill, stroke="none", sw=2, dash=""):
        d = f"stroke-dasharray='{dash}'" if dash else ""
        self.parts.append(f"<circle cx='{cx}' cy='{cy}' r='{r}' fill='{fill}' stroke='{stroke}' stroke-width='{sw}' {d}/>")
    def line(self, x1, y1, x2, y2, stroke=INK, sw=2, dash=""):
        d = f"stroke-dasharray='{dash}'" if dash else ""
        self.parts.append(f"<line x1='{x1}' y1='{y1}' x2='{x2}' y2='{y2}' stroke='{stroke}' stroke-width='{sw}' stroke-linecap='round' {d}/>")
    def path(self, d, fill="none", stroke=INK, sw=2, dash=""):
        dd = f"stroke-dasharray='{dash}'" if dash else ""
        self.parts.append(f"<path d='{d}' fill='{fill}' stroke='{stroke}' stroke-width='{sw}' stroke-linecap='round' stroke-linejoin='round' {dd}/>")
    def poly(self, pts, fill, stroke=INK, sw=2):
        self.parts.append(f"<polygon points='{' '.join(f'{x},{y}' for x,y in pts)}' fill='{fill}' stroke='{stroke}' stroke-width='{sw}' stroke-linejoin='round'/>")
    def arrow(self, x1, y1, x2, y2, stroke=INK, sw=3, head=10):
        ang = math.atan2(y2-y1, x2-x1)
        self.line(x1, y1, x2-head*0.8*math.cos(ang), y2-head*0.8*math.sin(ang), stroke, sw)
        p2 = (x2-head*math.cos(ang-0.5), y2-head*math.sin(ang-0.5)); p3 = (x2-head*math.cos(ang+0.5), y2-head*math.sin(ang+0.5))
        self.parts.append(f"<polygon points='{x2:.1f},{y2:.1f} {p2[0]:.1f},{p2[1]:.1f} {p3[0]:.1f},{p3[1]:.1f}' fill='{stroke}'/>")
    def dim(self, x1, y1, x2, y2, label, color=GRAY, off=0):
        self.line(x1, y1, x2, y2, color, 1.5)
        for (x, y) in ((x1, y1), (x2, y2)):
            if abs(x2-x1) > abs(y2-y1): self.line(x, y-5, x, y+5, color, 1.5)
            else: self.line(x-5, y, x+5, y, color, 1.5)
        mx, my = (x1+x2)/2, (y1+y2)/2
        if abs(x2-x1) > abs(y2-y1): self.text(mx, my-6+off, label, 12, color, "middle")
        else: self.text(mx+8+off, my+4, label, 12, color, "start")
    def save(self, name):
        defs = f"<defs>{''.join(self.defs)}</defs>" if self.defs else ""
        open(os.path.join(OUT, name), "w").write(
            f"<svg xmlns='http://www.w3.org/2000/svg' width='{self.w}' height='{self.h}' viewBox='0 0 {self.w} {self.h}'>{defs}{''.join(self.parts)}</svg>")
        print("wrote", name)

# =====================================================================
# Figure 1: side view layout, ball path, heights. Scale: 1 in = 18 px.
# =====================================================================
S = SVG(1100, 660, "Robot v1 side view: ball path from floor to Hive")
PX = 18.0
X0, FLOOR = 190, 580          # robot front is to the LEFT, +x toward the rear
def ix(inch): return X0 + inch*PX
def iy(inch): return FLOOR - inch*PX
def badge(x, y, n, col=INK):
    S.circle(x, y, 11, col, "white", 2); S.text(x, y+4, str(n), 12, "white", "middle", "bold")

S.line(40, FLOOR, 740, FLOOR, INK, 3); S.text(50, FLOOR+20, "tiles", 12, GRAY)
S.box(ix(0), iy(18), 18*PX, 18*PX, "none", GRAY, 0, 1.5, "8 6")
S.text(ix(0)+6, iy(18)-8, "18 in starting cube (R102)", 12, GRAY)
S.line(ix(-3), iy(29), ix(22), iy(29), RED, 1.5, "10 6")
S.text(ix(22)+8, iy(29)+4, "29 in expansion limit (R105)", 12, RED)

# chassis + wheels
S.box(ix(0.5), iy(4.6), 17*PX, 1.9*PX, "#d9d9d9", INK, 3)
for wx in (2.6, 15.4):
    S.circle(ix(wx), iy(2.05), 2.05*PX, "#2b2b2b", INK, 2); S.circle(ix(wx), iy(2.05), 0.6*PX, YELLOW)
# electronics deck and battery, rear low
S.box(ix(11), iy(7.6), 6*PX, 2.4*PX, "#3a352e", INK, 4); badge(ix(14), iy(6.4), 7, "#3a352e")
S.box(ix(11.2), iy(9.7), 3.2*PX, 1.8*PX, "#2b2b2b", INK, 3); badge(ix(12.8), iy(8.8), 8, "#2b2b2b")
# intake
S.circle(ix(-0.6), iy(4.3), 2.36*PX, "none", ORANGE, 2, "6 4"); S.circle(ix(-0.6), iy(4.3), 0.16*PX, INK)
for a in (90, 270):
    S.line(ix(-0.6), iy(4.3), ix(-0.6)+2.2*PX*math.cos(math.radians(a)), iy(4.3)-2.2*PX*math.sin(math.radians(a)), ORANGE, 5)
badge(ix(-0.6), iy(7.4), 1, ORANGE)
S.circle(ix(-3.2), iy(1.4), 1.4*PX, YELLOW, INK, 1.5)
S.arrow(ix(-5.2), iy(3.6), ix(-2.1), iy(4.8), INK, 2, 8)
# ramp / channel
ramp = [(ix(0.2), iy(4.9)), (ix(2.4), iy(5.0)), (ix(12.5), iy(12.6)), (ix(13.6), iy(12.6))]
S.path("M" + " L".join(f"{x},{y}" for x, y in ramp), stroke=INK, sw=3)
upper = [(ix(0.2), iy(9.0)), (ix(2.4), iy(9.1)), (ix(12.5), iy(16.7)), (ix(13.6), iy(16.7))]
S.path("M" + " L".join(f"{x},{y}" for x, y in upper), stroke=INK, sw=2, dash="6 5")
for t in (0.18, 0.40, 0.62, 0.84):
    x = ix(2.4) + t*(ix(12.5)-ix(2.4)); y = iy(5.0) + t*(iy(12.6)-iy(5.0))
    S.circle(x, y-1.8*PX, 1.75*PX, "#e87070" if t > 0.5 else YELLOW, INK, 1.5)
badge(ix(5.6), iy(12.9), 2)
# feeder, flywheel, hood
S.circle(ix(13.7), iy(13.0), 0.5*PX, GRAY, INK, 1.5); badge(ix(13.7), iy(11.2), 3, GRAY)
FW = (ix(15.6), iy(15.0)); S.circle(*FW, 1.77*PX, "#2b2b2b", INK, 2); S.circle(*FW, 0.3*PX, GRAY); badge(FW[0], FW[1], 4, "#2b2b2b")
S.path(f"M{ix(13.4)},{iy(13.6)} Q{ix(13.0)},{iy(17.9)} {ix(16.9)},{iy(17.8)}", stroke=BLUE, sw=4); badge(ix(14.0), iy(17.3), 5, BLUE)
S.arrow(ix(16.9), iy(17.9), ix(21.0), iy(24.6), GREEN, 4, 12)
S.text(ix(21.4), iy(24.2), "~60°, 4.5 to 5 m/s", 12, GREEN, "start", "bold")
# webcam
S.box(ix(9.4), iy(17.6), 1.6*PX, 0.7*PX, INK, INK, 2); badge(ix(10.2), iy(16.4), 6)
# dimensions
S.dim(ix(-7.3), iy(0), ix(-7.3), iy(4.3), "4.3 in", GRAY)
S.text(ix(-7.3), iy(5.2), "intake shaft", 11, GRAY, "middle")
S.dim(ix(19.2), iy(0), ix(19.2), iy(17.9), "17.9 in exit", GRAY)
S.dim(ix(0.5), iy(-1.3), ix(17.5), iy(-1.3), "17 in", GRAY)

# legend column
LX = 780
S.box(LX, 70, 300, 335, "white", EDGE, 10, 1.5)
items = [(1, ORANGE, "Intake", "TPU spoke wheels on 8mm REX, 117 RPM motor"),
         (2, INK, "Channel", "4 balls single file; indexer rollers, NeveRest 19.2"),
         (3, GRAY, "Feeder", "1 in grip wheel on a Smart Servo, one ball per shot"),
         (4, "#2b2b2b", "Flywheel", "2 x 90 mm grip wheels, HD Hex bare, ~2100 RPM"),
         (5, BLUE, "Hood", "hinged, rubber-band preload, fits 71 and 91 mm"),
         (6, INK, "Webcam", "C920 looking up at cell AprilTags"),
         (7, "#3a352e", "Hubs", "Control + Expansion Hub, LEDs facing out"),
         (8, "#2b2b2b", "Battery", "low and rear, REV holder plate + strap")]
for i, (n, col, t, d) in enumerate(items):
    y = 98 + i*40
    badge(LX+22, y, n, col); S.text(LX+42, y-2, t, 13, INK, "start", "bold"); S.text(LX+42, y+14, d, 11, INK)
S.box(LX, 420, 300, 150, "white", EDGE, 10, 1.5)
S.text(LX+150, 445, "Target: upward-facing CELL", 14, INK, "middle", "bold")
S.lines(LX+15, 470, ["Hive pivot 43.95 in above tiles",
                     "Cell opening 20 x 14 in, 12 in deep",
                     "Frame base 49.5 x 39 in",
                     "Shoot from 3 to 5 ft out, ball descending",
                     "Flywheel speed set from AprilTag distance"], 12, INK, lh=19)
S.save("robot-v1-side.svg")

# =====================================================================
# Figure 2: electrical block diagram
# =====================================================================
S = SVG(1000, 640, "Robot v1 electrical: one battery, one switch, two hubs")
def node(x, y, w, h, title, sub="", fill="white", stroke=INK, tcol=INK):
    S.box(x, y, w, h, fill, stroke, 8, 2)
    S.text(x+w/2, y+22, title, 13, tcol, "middle", "bold")
    if sub: S.text(x+w/2, y+40, sub, 11, tcol if tcol != "white" else GRAY, "middle")
# power chain
node(40, 90, 130, 56, "12V NiMH", "REV Slim 3000 mAh", "#2b2b2b", INK, "white")
node(210, 90, 130, 56, "Main switch", "REV-31-1387", YELLOW, INK)
node(380, 80, 230, 130, "Control Hub", "REV-31-1595 (IMU inside)", "#1A1611", INK, "white")
node(660, 80, 230, 130, "Expansion Hub", "REV-31-1153", "#1A1611", INK, "white")
S.arrow(170, 118, 210, 118, RED, 4, 10); S.text(190, 108, "XT30", 10, RED, "middle")
S.arrow(340, 118, 380, 118, RED, 4, 10); S.text(360, 108, "XT30", 10, RED, "middle")
S.arrow(610, 118, 660, 118, RED, 4, 10); S.text(635, 108, "XT30", 10, RED, "middle")
S.line(610, 165, 660, 165, PURPLE, 3); S.text(635, 158, "RS485", 10, PURPLE, "middle")
# ground strap
S.line(400, 210, 400, 236, GREEN, 2, "4 3"); S.text(400, 250, "ground strap to frame (R605)", 10, GREEN, "middle")

# Control Hub ports
S.text(495, 150, "Motors 0-3: drive FL, FR, BL, BR", 11, "white", "middle")
S.text(495, 168, "Servos 0: feeder   1: hood latch (later)", 11, "white", "middle")
S.text(495, 186, "I2C 1: color (intake)   2: distance (hopper)", 11, "white", "middle")
S.text(495, 204, "USB: Logitech C920", 11, "white", "middle")
# Expansion Hub ports
S.text(775, 150, "Motor 0: intake 117 RPM", 11, "white", "middle")
S.text(775, 168, "Motor 1: indexer NeveRest 19.2", 11, "white", "middle")
S.text(775, 186, "Motor 2: flywheel HD Hex", 11, "white", "middle")
S.text(775, 204, "Motor 3: spare (Flower lift, v2)", 11, "white", "middle")

# Devices row
devs = [(40, "FL 312", ORANGE), (140, "FR 312", ORANGE), (240, "BL 312", ORANGE), (340, "BR 312", ORANGE)]
for x, t, c in devs:
    node(x, 330, 90, 48, t, "5203, port CH", c, INK)
    S.line(x+45, 330, 495, 210, ORANGE, 1.5)
node(450, 330, 90, 48, "Feeder", "Smart Servo", BLUE, INK); S.line(495, 330, 495, 210, BLUE, 1.5)
node(560, 330, 100, 48, "Color", "REV V3, I2C", TEAL := "#14B8A6", INK); S.line(610, 330, 495, 210, TEAL, 1.5)
node(680, 330, 100, 48, "Distance", "2 m, I2C", TEAL, INK); S.line(730, 330, 495, 210, TEAL, 1.5)
node(800, 330, 120, 48, "Webcam", "C920, USB", "#e8f4ff", INK); S.line(860, 330, 495, 210, INK, 1.5)
devs2 = [(560, "Intake", "117 RPM"), (680, "Indexer", "NeveRest 19.2"), (800, "Flywheel", "HD Hex bare")]
for x, t, s2 in devs2:
    node(x, 420, 110, 48, t, s2, ORANGE, INK)
    S.line(x+55, 420, 775, 210, ORANGE, 1.5)

# Driver station
node(40, 420, 200, 90, "Driver Hub", "REV-31-1596 + 2 gamepads", "#e8f4ff", INK)
for r in (12, 22, 32):
    S.path(f"M{270-r*0.7},{462-r*0.7} A{r},{r} 0 0 1 {270+r*0.7},{462-r*0.7}", stroke=BLUE, sw=2.5)
S.text(270, 500, "Wi-Fi to Control Hub", 11, BLUE, "middle")

# Rules box
S.box(40, 540, 920, 80, LIGHT, "none", 10, 0)
S.lines(60, 562, ["Wire gauge (R609): 18 AWG battery and hub power, 22 AWG motor leads, 28 AWG sensor/encoder. Red/yellow/white = +, black/blue = - on the 12 V bus (R610).",
                  "Hub LEDs and fuses must be visible at inspection (R606). Name devices <team>-RC / <team>-DS (R705). Change the Control Hub Wi-Fi password (R711).",
                  "Max 2 motors per motor port, 2 servos per servo port (R505). Total on robot: 7 motors, 1 servo, so 1 motor and 7 servos in reserve."], 12, INK, lh=19)
S.save("robot-v1-electrical.svg")
