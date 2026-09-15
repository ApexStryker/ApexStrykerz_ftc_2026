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
# Intake is THROUGH the frame: front cross bar removed, ball enters the bay between the rails.
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

# rails (side view): channel ~32 mm tall centred on the 52 mm axle -> 1.4 .. 2.7 in
S.box(ix(0.5), iy(2.7), 17*PX, 1.3*PX, "#d9d9d9", INK, 3)
# cross bars, both moved to the rear (drawn as end view rectangles)
for cx in (13.2, 16.3):
    S.box(ix(cx), iy(2.7), 1.9*PX, 1.3*PX, "#bdbdbd", INK, 2)
S.text(ix(15.2), FLOOR+34, "both cross bars moved to the rear", 10, GRAY, "middle")
# ghost of the removed front cross bar
S.box(ix(0.5), iy(2.7), 1.9*PX, 1.3*PX, "none", RED, 2, 1.5, "4 3")
S.text(ix(1.5), iy(3.2), "front bar removed", 10, RED, "middle")
for wx in (2.6, 15.4):
    S.circle(ix(wx), iy(2.05), 2.05*PX, "#2b2b2b", INK, 2); S.circle(ix(wx), iy(2.05), 0.6*PX, YELLOW)
# electronics: hubs standing on the rear cross bars, battery ahead of them under the channel
S.box(ix(13.4), iy(6.6), 3.8*PX, 3.8*PX, "#3a352e", INK, 4); badge(ix(15.3), iy(4.7), 7, "#3a352e")
S.box(ix(9.6), iy(4.6), 3.3*PX, 1.9*PX, "#2b2b2b", INK, 3); badge(ix(11.25), iy(3.65), 8, "#2b2b2b")

# intake bridge: side plates on the rails carry the shaft and a cross member above the ball path
S.box(ix(1.6), iy(8.2), 2.9*PX, 5.5*PX, "#f0e6d8", INK, 4, 1.5)      # printed side plate (near side)
S.box(ix(1.9), iy(8.0), 1.2*PX, 0.6*PX, "#bdbdbd", INK, 2)           # REV extrusion bridge end view
S.text(ix(2.5), iy(8.7), "bridge", 10, GRAY, "middle")
# flapper: shaft 3.0 in behind the rail front, 108 mm (4.25 in) up, tips sweep r = 60 mm (2.36 in)
SX, SZ = ix(3.0), iy(4.25)
S.circle(SX, SZ, 2.36*PX, "none", ORANGE, 2, "6 4"); S.circle(SX, SZ, 0.47*PX, "#e0e0e0", INK, 1.5)
for a in (120, 300):
    S.line(SX, SZ, SX+2.2*PX*math.cos(math.radians(a)), SZ-2.2*PX*math.sin(math.radians(a)), ORANGE, 5)
badge(SX, SZ-2.36*PX-16, 1, ORANGE)
# rotation arrow: tips move down at the front, back along the bottom
S.path(f"M{SX-2.7*PX},{SZ-1.2*PX} A{2.7*PX},{2.7*PX} 0 0 0 {SX-1.2*PX},{SZ+2.4*PX}", stroke=ORANGE, sw=2)
S.arrow(SX-1.25*PX, SZ+2.38*PX, SX-0.9*PX, SZ+2.5*PX, ORANGE, 2, 8)
# ball entering the bay on the tiles (Nectar, 91 mm = 3.6 in)
S.circle(ix(-2.0), iy(1.8), 1.8*PX, "#e87070", INK, 1.5)
S.arrow(ix(-2.0), iy(0.2)-2, ix(0.4), iy(0.2)-2, INK, 2, 8)
S.text(ix(-2.0), FLOOR+34, "ball rolls into the bay", 10, INK, "middle")

# ramp + channel: starts 4.5 in back at 10 mm, rises 45 deg to the feeder
def ramp_pt(x): return iy(0.4 + (x-4.5))     # 45 degrees: 1 in up per 1 in back
S.path(f"M{ix(4.5)},{ramp_pt(4.5)} L{ix(12.4)},{ramp_pt(12.4)} L{ix(13.4)},{ramp_pt(12.4)}", stroke=INK, sw=3)
S.path(f"M{ix(4.5)},{ramp_pt(4.5)-4.1*PX} L{ix(12.4)},{ramp_pt(12.4)-4.1*PX} L{ix(13.4)},{ramp_pt(12.4)-4.1*PX}", stroke=INK, sw=2, dash="6 5")
for t in (0.12, 0.38, 0.64, 0.90):
    x = 4.5 + t*(12.4-4.5); z = 0.4 + (x-4.5)
    S.circle(ix(x)-1.27*PX, iy(z)-1.27*PX, 1.75*PX, "#e87070" if t > 0.5 else YELLOW, INK, 1.5)
badge(ix(6.2), iy(6.4), 2)
# indexer roller pairs (two stations) drawn as small wheels above the channel
for x in (6.4, 9.6):
    S.circle(ix(x)-2.1*PX, ramp_pt(x)-4.1*PX-0.5*PX, 0.5*PX, GRAY, INK, 1.5)
# feeder, flywheel, hood
S.circle(ix(13.3), ramp_pt(12.4)-3.9*PX, 0.5*PX, GRAY, INK, 1.5); badge(ix(13.3), ramp_pt(12.4)-3.9*PX-1.6*PX, 3, GRAY)
FW = (ix(15.3), iy(11.3)); S.circle(*FW, 1.77*PX, "#2b2b2b", INK, 2); S.circle(*FW, 0.3*PX, GRAY); badge(FW[0], FW[1], 4, "#2b2b2b")
S.path(f"M{ix(13.1)},{iy(9.9)} Q{ix(12.7)},{iy(14.2)} {ix(16.6)},{iy(14.1)}", stroke=BLUE, sw=4); badge(ix(13.7), iy(13.6), 5, BLUE)
S.arrow(ix(16.6), iy(14.2), ix(20.7), iy(20.9), GREEN, 4, 12)
S.text(ix(21.1), iy(20.5), "~60°, 4.7 to 5 m/s", 12, GREEN, "start", "bold")
# webcam on the bridge
S.box(ix(9.0), iy(15.0), 1.6*PX, 0.7*PX, INK, INK, 2); badge(ix(9.8), iy(13.8), 6)
S.line(ix(9.8), iy(14.3), ix(9.8), iy(8.7), GRAY, 1.5, "3 3")
# dimensions
S.dim(ix(-6.4), iy(0), ix(-6.4), iy(4.25), "4.25 in", GRAY)
S.text(ix(-6.4), iy(5.1), "flapper shaft", 11, GRAY, "middle")
S.dim(ix(19.2), iy(0), ix(19.2), iy(14.2), "14.2 in exit", GRAY)
S.dim(ix(0.5), iy(-1.3), ix(17.5), iy(-1.3), "17 in", GRAY)

# legend column
LX = 780
S.box(LX, 70, 300, 335, "white", EDGE, 10, 1.5)
items = [(1, ORANGE, "Flapper intake", "3 TPU spoke wheels on 8mm REX, 117 RPM, in the frame"),
         (2, INK, "Ramp + channel", "45° incline, 4 balls single file, indexer rollers"),
         (3, GRAY, "Feeder", "1 in grip wheel on a Smart Servo, one ball per shot"),
         (4, "#2b2b2b", "Flywheel", "2 x 90 mm grip wheels, HD Hex bare, ~2200 RPM"),
         (5, BLUE, "Hood", "hinged, rubber-band preload, fits 71 and 91 mm"),
         (6, INK, "Webcam", "C920 on the bridge, looking up at cell AprilTags"),
         (7, "#3a352e", "Hubs", "on the rear cross bars, LEDs facing out"),
         (8, "#2b2b2b", "Battery", "under the channel, ahead of the hubs")]
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
# Figure 3: intake detail, side and top, in millimetres.
# =====================================================================
S = SVG(1100, 640, "Intake detail: the ball comes in through the frame, not over it")
# ---- left: side view, scale 1.5 px/mm ----
M = 1.5
OX, OZ = 240, 440
def mx(mm): return OX + mm*M
def mz(mm): return OZ - mm*M
S.text(60, 100, "SIDE VIEW (near rail ghosted)", 13, GRAY, "start", "bold")
S.line(40, OZ, 560, OZ, INK, 3); S.text(48, OZ+18, "tiles", 11, GRAY)
# cube face 25 mm ahead of the rails
S.line(mx(-25), mz(0), mx(-25), mz(225), GRAY, 1.5, "8 6"); S.text(mx(-25), OZ+34, "18 in cube face", 10, GRAY, "middle")
# ramp
S.line(mx(114), mz(10), mx(205), mz(101), INK, 3); S.text(mx(175), mz(58), "ramp 45°", 10, INK, "start")
# balls: Nectar in the bay, Pollen outline at the ramp foot
S.circle(mx(58), mz(45.5), 45.5*M, "#e87070", INK, 1.5); S.text(mx(58), mz(45.5)+4, "91", 11, INK, "middle", "bold")
S.circle(mx(143), mz(45), 35.5*M, "none", "#c9a400", 2.5); S.text(mx(143), mz(45)+4, "71", 11, "#c9a400", "middle", "bold")
# rail (near side) ghosted over the ball, 36..68 mm
S.box(mx(0), mz(68), 205*M, 32*M, "rgba(217,217,217,0.55)", INK, 3, 1.5)
S.text(mx(150), mz(52)+4, "rail 36 to 68 mm (measure)", 10, INK, "middle")
S.box(mx(0), mz(68), 48*M, 32*M, "none", RED, 2, 1.5, "4 3"); S.text(mx(24), mz(84), "bar removed", 9, RED, "middle")
# side plate and bridge
S.box(mx(40), mz(210), 75*M, 142*M, "rgba(240,230,216,0.85)", INK, 4, 1.5)
S.box(mx(62), mz(208), 28*M, 15*M, "#bdbdbd", INK, 2); S.text(mx(76), mz(222), "REV 15 mm bridge, ~205 mm up", 10, GRAY, "middle")
S.text(mx(77), mz(180), "printed side plate", 10, INK, "middle")
for hz in (100, 108, 116):
    S.circle(mx(76), mz(hz), 4.5*M, "white" if hz != 108 else "#e0e0e0", INK, 1.2)
S.text(mx(92), mz(116)+4, "116", 9, GRAY); S.text(mx(92), mz(100)+4, "100", 9, GRAY)
# flapper sweep and spokes
S.circle(mx(76), mz(108), 60*M, "none", ORANGE, 2, "6 4")
for a_ in (140, 320):
    S.line(mx(76), mz(108), mx(76)+55*M*math.cos(math.radians(a_)), mz(108)-55*M*math.sin(math.radians(a_)), ORANGE, 5)
S.text(mx(10), mz(150), "tips bottom out", 10, ORANGE, "middle"); S.text(mx(10), mz(140), "at 48 mm", 10, ORANGE, "middle")
# dims
S.dim(mx(0), mz(-16), mx(76), mz(-16), "76", GRAY); S.dim(mx(0), mz(-34), mx(114), mz(-34), "114 to ramp foot", GRAY)
S.dim(mx(-60), mz(0), mx(-60), mz(108), "108", GRAY); S.text(mx(-60), mz(118), "shaft", 10, GRAY, "middle")
S.dim(mx(-100), mz(0), mx(-100), mz(48), "48", GRAY); S.text(mx(-100), mz(58), "tip", 10, GRAY, "middle")
S.arrow(mx(-120), mz(45), mx(-95), mz(45), INK, 2, 8)

# ---- right: top view, scale 0.85 px/mm ----
K = 0.72
TX, TY = 720, 190
def tx(mm): return TX + mm*K
def ty(mm): return TY + mm*K
S.text(600, 100, "TOP VIEW", 13, GRAY, "start", "bold")
W_IN, RAIL, WHEEL = 264, 48, 40
for side in (0, 1):
    y0 = -RAIL if side == 0 else W_IN
    S.box(tx(0), ty(y0), 230*K, RAIL*K, "#d9d9d9", INK, 3)
    wy = y0 - WHEEL - 6 if side == 0 else y0 + RAIL + 6
    S.box(tx(14), ty(wy), 104*K, WHEEL*K, "#2b2b2b", INK, 2)
    S.text(tx(66), ty(wy + WHEEL/2)+4, "front wheel", 9, "white", "middle")
S.text(tx(170), ty(-RAIL/2)+4, "rail", 10, INK, "middle"); S.text(tx(170), ty(W_IN+RAIL/2)+4, "rail", 10, INK, "middle")
S.box(tx(0), ty(0), 48*K, W_IN*K, "none", RED, 2, 1.5, "4 3"); S.text(tx(24), ty(-RAIL-WHEEL-16), "bar removed", 9, RED, "middle")
S.poly([(tx(0), ty(0)), (tx(114), ty(0)), (tx(114), ty(W_IN/2-55)), (tx(30), ty(0))], "#f0e6d8", INK, 1.5)
S.poly([(tx(0), ty(W_IN)), (tx(114), ty(W_IN)), (tx(114), ty(W_IN/2+55)), (tx(30), ty(W_IN))], "#f0e6d8", INK, 1.5)
S.text(tx(95), ty(22), "funnel guide", 9, INK, "middle"); S.text(tx(95), ty(W_IN-16), "funnel guide", 9, INK, "middle")
S.line(tx(114), ty(W_IN/2-55), tx(230), ty(W_IN/2-55), INK, 3); S.line(tx(114), ty(W_IN/2+55), tx(230), ty(W_IN/2+55), INK, 3)
S.text(tx(172), ty(W_IN/2-62), "channel 110 mm", 10, INK, "middle")
S.line(tx(76), ty(-RAIL-4), tx(76), ty(W_IN+RAIL+4), "#888", 4)
for k in (-1, 0, 1):
    yc = W_IN/2 + k*40
    S.box(tx(76-60), ty(yc-10), 120*K, 20*K, ORANGE, INK, 1.5)
S.text(tx(170), ty(W_IN/2+40+22), "3 spoke wheels, 40 mm pitch", 10, ORANGE, "middle")
S.box(tx(150), ty(W_IN+RAIL+52), 60*K, 32*K, "#b8b8b8", INK, 3); S.box(tx(168), ty(W_IN+RAIL+56), 24*K, 24*K, YELLOW, "none", 2, 0)
S.text(tx(180), ty(W_IN+RAIL+100), "117 RPM motor, outside the rail", 10, INK, "middle")
S.circle(tx(-12), ty(W_IN/2+55), 45.5*K, "#e87070", INK, 1.5)
S.circle(tx(150), ty(W_IN/2), 35.5*K, YELLOW, INK, 1.5)
S.arrow(tx(-90), ty(W_IN/2+55), tx(-62), ty(W_IN/2+55), INK, 2, 8)
S.dim(tx(0), ty(W_IN+RAIL+WHEEL+30), tx(76), ty(W_IN+RAIL+WHEEL+30), "76", GRAY)
S.dim(tx(242), ty(0), tx(242), ty(W_IN), "264 inside", GRAY)

S.box(40, 578, 1020, 46, LIGHT, "none", 10, 0)
S.text(550, 596, "Why: a 17 in chassis leaves 25 mm in front inside the 18 in cube, too little for any fixed intake ahead of the frame. Removing the front cross bar", 11, INK, "middle")
S.text(550, 614, "opens a 264 mm bay between the rails. The ball rolls in on the tiles, the flapper catches it inside, and pushes it onto a ramp that starts behind the sweep.", 11, INK, "middle")
S.save("robot-v1-intake.svg")

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
