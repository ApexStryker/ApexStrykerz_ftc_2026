#!/usr/bin/env python3
"""Generates the SVG illustrations for TeamCode/README.md into docs/img. Run: python3 TeamCode/docs/gen_svgs.py"""
import os, math

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "img")
os.makedirs(OUT, exist_ok=True)

# Palette
INK = "#1A1611"; ORANGE = "#FF8A3D"; YELLOW = "#F5C518"; BLUE = "#3B82F6"
GREEN = "#22C55E"; RED = "#EF4444"; GRAY = "#9CA3AF"; LIGHT = "#F3EFE7"
CARD = "#FFFDF7"; EDGE = "#E5E0D8"; PURPLE = "#8B5CF6"; TEAL = "#14B8A6"
FONT = "font-family='Arial, Helvetica, sans-serif'"

def esc(s): return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

class SVG:
    def __init__(self, w, h, title):
        self.w, self.h = w, h
        self.parts = []
        self.defs = []
        self.parts.append(f"<rect x='1' y='1' width='{w-2}' height='{h-2}' rx='18' fill='{CARD}' stroke='{EDGE}' stroke-width='2'/>")
        self.text(w/2, 42, title, 26, INK, "middle", "bold")
        self._id = 0
    def uid(self):
        self._id += 1; return f"id{self._id}"
    def add(self, s): self.parts.append(s)
    def text(self, x, y, s, size=16, fill=INK, anchor="start", weight="normal", extra=""):
        self.parts.append(f"<text x='{x}' y='{y}' font-size='{size}' fill='{fill}' text-anchor='{anchor}' font-weight='{weight}' {FONT} {extra}>{esc(s)}</text>")
    def lines(self, x, y, items, size=15, fill=INK, anchor="start", lh=None, weight="normal"):
        lh = lh or size*1.35
        for i, s in enumerate(items):
            self.text(x, y+i*lh, s, size, fill, anchor, weight)
    def box(self, x, y, w, h, fill=LIGHT, stroke=INK, rx=12, sw=2, dash=""):
        d = f"stroke-dasharray='{dash}'" if dash else ""
        self.parts.append(f"<rect x='{x}' y='{y}' width='{w}' height='{h}' rx='{rx}' fill='{fill}' stroke='{stroke}' stroke-width='{sw}' {d}/>")
    def circle(self, cx, cy, r, fill, stroke="none", sw=2):
        self.parts.append(f"<circle cx='{cx}' cy='{cy}' r='{r}' fill='{fill}' stroke='{stroke}' stroke-width='{sw}'/>")
    def line(self, x1, y1, x2, y2, stroke=INK, sw=3, dash="", cap="round"):
        d = f"stroke-dasharray='{dash}'" if dash else ""
        self.parts.append(f"<line x1='{x1}' y1='{y1}' x2='{x2}' y2='{y2}' stroke='{stroke}' stroke-width='{sw}' stroke-linecap='{cap}' {d}/>")
    def path(self, d, fill="none", stroke=INK, sw=3, extra=""):
        self.parts.append(f"<path d='{d}' fill='{fill}' stroke='{stroke}' stroke-width='{sw}' stroke-linecap='round' stroke-linejoin='round' {extra}/>")
    def arrow(self, x1, y1, x2, y2, stroke=INK, sw=4, head=12):
        ang = math.atan2(y2-y1, x2-x1)
        # shorten line so the head tip lands on (x2,y2)
        ex, ey = x2 - head*0.8*math.cos(ang), y2 - head*0.8*math.sin(ang)
        self.line(x1, y1, ex, ey, stroke, sw)
        p1 = (x2, y2)
        p2 = (x2 - head*math.cos(ang - 0.5), y2 - head*math.sin(ang - 0.5))
        p3 = (x2 - head*math.cos(ang + 0.5), y2 - head*math.sin(ang + 0.5))
        self.parts.append(f"<polygon points='{p1[0]:.1f},{p1[1]:.1f} {p2[0]:.1f},{p2[1]:.1f} {p3[0]:.1f},{p3[1]:.1f}' fill='{stroke}'/>")
    def badge(self, cx, cy, n, fill=ORANGE, r=16):
        self.circle(cx, cy, r, fill)
        self.text(cx, cy+6, str(n), 18, "white", "middle", "bold")
    def pill(self, x, y, s, fill, size=14, textfill="white", pad=10):
        w = len(s)*size*0.58 + 2*pad
        self.box(x, y, w, size+12, fill, "none", rx=(size+12)/2, sw=0)
        self.text(x+w/2, y+size+3, s, size, textfill, "middle", "bold")
        return w
    def save(self, name):
        defs = f"<defs>{''.join(self.defs)}</defs>" if self.defs else ""
        svg = (f"<svg xmlns='http://www.w3.org/2000/svg' width='{self.w}' height='{self.h}' "
               f"viewBox='0 0 {self.w} {self.h}'>{defs}{''.join(self.parts)}</svg>")
        with open(os.path.join(OUT, name), "w") as f: f.write(svg)
        print("wrote", name)

    # ---- drawings -------------------------------------------------------
    def gamepad(self, cx, cy, s=1.0, sticks=None, labels=False):
        """Gamepad centred at cx,cy. sticks = ((lx,ly),(rx,ry)) offsets -1..1 for the stick nubs."""
        sticks = sticks or ((0, 0), (0, 0))
        g = lambda x: cx + x*s
        h = lambda y: cy + y*s
        body = (f"M{g(-110)},{h(-40)} Q{g(-110)},{h(-70)} {g(-80)},{h(-70)} L{g(80)},{h(-70)} "
                f"Q{g(110)},{h(-70)} {g(110)},{h(-40)} L{g(130)},{h(40)} Q{g(135)},{h(80)} {g(100)},{h(80)} "
                f"Q{g(75)},{h(80)} {g(60)},{h(50)} L{g(45)},{h(30)} L{g(-45)},{h(30)} L{g(-60)},{h(50)} "
                f"Q{g(-75)},{h(80)} {g(-100)},{h(80)} Q{g(-135)},{h(80)} {g(-130)},{h(40)} Z")
        self.path(body, fill=INK, stroke=INK, sw=3)
        # d-pad
        self.path(f"M{g(-78)},{h(-42)} h12 v12 h12 v12 h-12 v12 h-12 v-12 h-12 v-12 h12 Z", fill="#3a352e", stroke="none")
        # ABXY
        for (dx, dy, col) in [(72, -42, YELLOW), (86, -28, RED), (58, -28, BLUE), (72, -14, GREEN)]:
            self.circle(g(dx), h(dy), 6*s, col)
        # start/back
        self.box(g(-18), h(-38), 12*s, 6*s, GRAY, "none", 3, 0)
        self.box(g(6), h(-38), 12*s, 6*s, GRAY, "none", 3, 0)
        # sticks
        for (bx, (ox, oy)) in [(-38, sticks[0]), (38, sticks[1])]:
            self.circle(g(bx), h(10), 20*s, "#3a352e")
            self.circle(g(bx + ox*9), h(10 + oy*9), 13*s, "#6b6459", INK, 2)
            self.circle(g(bx + ox*9), h(10 + oy*9), 5*s, "#8a8378")
        if labels:
            self.text(g(-38), h(62), "LEFT stick", 9*s, "white", "middle", "bold")
            self.text(g(38), h(62), "RIGHT stick", 9*s, "white", "middle", "bold")

    def phone(self, x, y, w=90, h=160, screen_lines=()):
        self.box(x, y, w, h, INK, INK, 12, 2)
        self.box(x+6, y+14, w-12, h-30, "#e8f4ff", "none", 4, 0)
        self.circle(x+w/2, y+h-9, 3, GRAY)
        for i, s in enumerate(screen_lines):
            self.text(x+w/2, y+40+i*18, s, 11, INK, "middle", "bold" if i == 0 else "normal")

    def hub(self, x, y, w=220, h=120, ports=True, label="REV Control Hub"):
        self.box(x, y, w, h, INK, INK, 10, 2)
        self.text(x+w/2, y+30, label, 15, "white", "middle", "bold")
        self.text(x+w/2, y+50, "runs OUR program", 12, ORANGE, "middle")
        # LED
        self.circle(x+w-18, y+18, 6, GREEN)
        # wifi symbol
        for r in (10, 18, 26):
            self.path(f"M{x+18-r*0.7},{y+30-r*0.7+8} A{r},{r} 0 0 1 {x+18+r*0.7},{y+30-r*0.7+8}", stroke=BLUE, sw=2.5)
        self.circle(x+18, y+40, 3, BLUE)
        if ports:
            pw = (w-40)/4
            for i in range(4):
                px = x+20+i*pw
                self.box(px+4, y+h-38, pw-8, 24, "#3a352e", ORANGE, 4, 2)
                self.text(px+pw/2, y+h-21, str(i), 14, "white", "middle", "bold")
            self.text(x+w/2, y+h-44, "MOTOR PORTS", 10, GRAY, "middle", "bold")
        return (x+20, y+h-26, (w-40)/4)  # port row geometry

    def motor(self, x, y, w=70, h=28, vertical=False, name=""):
        """Yellow Jacket motor: grey can with yellow label. Horizontal by default."""
        if not vertical:
            self.box(x, y, w, h, "#b8b8b8", INK, 6, 2)
            self.box(x+w*0.3, y+3, w*0.45, h-6, YELLOW, "none", 3, 0)
            self.box(x+w, y+h/2-5, 10, 10, "#3a352e", INK, 2, 1.5)  # shaft
        else:
            self.box(x, y, h, w, "#b8b8b8", INK, 6, 2)
            self.box(x+3, y+w*0.3, h-6, w*0.45, YELLOW, "none", 3, 0)
        if name:
            self.text(x+w/2, y+h+16, name, 12, INK, "middle")

    def wheel(self, x, y, w, h, slant, fill="#2b2b2b"):
        """Top-view mecanum wheel. slant '\\' or '/' for the roller stripes."""
        cid = self.uid()
        self.defs.append(f"<clipPath id='{cid}'><rect x='{x}' y='{y}' width='{w}' height='{h}' rx='6'/></clipPath>")
        self.box(x, y, w, h, fill, INK, 6, 2)
        step = 12
        g = []
        n = int((w+h)/step)+2
        for i in range(-n, n):
            off = i*step
            if slant == "\\":
                g.append(f"<line x1='{x+off}' y1='{y}' x2='{x+off+h}' y2='{y+h}' stroke='{YELLOW}' stroke-width='4'/>")
            else:
                g.append(f"<line x1='{x+off+h}' y1='{y}' x2='{x+off}' y2='{y+h}' stroke='{YELLOW}' stroke-width='4'/>")
        self.add(f"<g clip-path='url(#{cid})' opacity='0.9'>{''.join(g)}</g>")

    def strafer(self, cx, cy, s=1.0, wheel_arrows=None, labels=True, ports=True, show_hub=True):
        """Top view of Strafer. wheel_arrows: dict FL/FR/BL/BR -> +1 fwd, -1 back, 0 none."""
        wheel_arrows = wheel_arrows or {}
        g = lambda x: cx + x*s; h = lambda y: cy + y*s
        # rails
        self.box(g(-105), h(-120), 24*s, 240*s, "#d9d9d9", INK, 3, 2)
        self.box(g(81), h(-120), 24*s, 240*s, "#d9d9d9", INK, 3, 2)
        self.box(g(-81), h(-95), 162*s, 22*s, "#d9d9d9", INK, 3, 2)
        self.box(g(-81), h(15), 162*s, 22*s, "#d9d9d9", INK, 3, 2)
        # motors along rails
        for (mx, my) in [(-101, -78), (-101, 32), (85, -78), (85, 32)]:
            self.box(g(mx), h(my), 16*s, 48*s, "#a8a8a8", INK, 3, 1.5)
            self.box(g(mx+2), h(my+14), 12*s, 20*s, YELLOW, "none", 2, 0)
        if show_hub:
            self.box(g(-55), h(-65), 110*s, 72*s, INK, INK, 6, 1.5)
            self.text(g(0), h(-36), "Control", 12*s, "white", "middle", "bold")
            self.text(g(0), h(-20), "Hub", 12*s, "white", "middle", "bold")
            self.circle(g(42), h(-55), 3*s, GREEN)
        # wheels: FL top-left etc.  slants form an X
        W, H = 30*s, 64*s
        pos = {"FL": (g(-150), h(-125), "\\"), "FR": (g(120), h(-125), "/"),
               "BL": (g(-150), h(60), "/"),  "BR": (g(120), h(60), "\\")}
        for k, (wx, wy, sl) in pos.items():
            self.wheel(wx, wy, W, H, sl)
            # axle
            ax = wx+W if k in ("FL", "BL") else wx
            ax2 = g(-105) if k in ("FL", "BL") else g(105)
            self.line(ax, wy+H/2, ax2, wy+H/2, INK, 4*s)
            d = wheel_arrows.get(k, 0)
            if d:
                col = GREEN if d > 0 else RED
                mid = wx+W/2
                if d > 0: self.arrow(mid, wy+H-6, mid, wy+6, col, 6*s, 14*s)
                else:     self.arrow(mid, wy+6, mid, wy+H-6, col, 6*s, 14*s)
        if labels:
            lab = {"FL": ("front_left", 0), "FR": ("front_right", 1), "BL": ("back_left", 2), "BR": ("back_right", 3)}
            for k, (wx, wy, _) in pos.items():
                name, port = lab[k]
                ty = wy - 10 if k in ("FL", "FR") else wy + H + 20
                self.text(wx+W/2, ty, name, 13*s, INK, "middle", "bold")
                if ports:
                    self.text(wx+W/2, ty + (16*s if k in ("BL", "BR") else -16*s), f"port {port}", 12*s, ORANGE, "middle", "bold")
        return pos

# =========================================================================
# 1. Big picture
# =========================================================================
S = SVG(960, 400, "How your thumb moves the robot")
# Gamepad
S.gamepad(120, 200, 0.75, labels=False)
S.badge(120, 300, 1)
S.text(120, 330, "You move a stick", 14, INK, "middle", "bold")
# USB cable to phone
S.line(200, 200, 260, 200, INK, 4)
S.text(230, 188, "USB", 11, GRAY, "middle")
# Driver Station phone
S.phone(262, 120, 90, 160, ("Driver", "Station", "", "INIT  START"))
S.badge(307, 300, 2)
S.lines(307, 330, ["Driver Station", "reads the sticks"], 14, INK, "middle", weight="bold")
# Wi-Fi waves
for i, r in enumerate((14, 26, 38)):
    S.path(f"M{430-r*0.7},{200-r*0.7} A{r},{r} 0 0 1 {430+r*0.7},{200-r*0.7}", stroke=BLUE, sw=3)
S.circle(430, 210, 4, BLUE)
S.text(430, 165-40, "Wi-Fi", 14, BLUE, "middle", "bold")
S.badge(430, 300, 3)
S.lines(430, 330, ["Numbers fly", "through the air"], 14, INK, "middle", weight="bold")
# Control Hub
px, py, pw = S.hub(500, 140, 220, 120)
S.badge(610, 300, 4)
S.lines(610, 330, ["Control Hub runs", "StraferTeleOp"], 14, INK, "middle", weight="bold")
# Cables to motors
colors = [ORANGE, BLUE, GREEN, PURPLE]
names = ["front_left", "front_right", "back_left", "back_right"]
for i in range(4):
    cx = px + i*pw + pw/2
    my = 120 + i*52
    S.path(f"M{cx},{py+2} L{cx},{py+18} Q{cx},{py+30} {cx+12},{py+30} L{780},{py+30} Q{800},{py+30} {800},{py+30-(py+30-my)} L{800},{my}",
           stroke=colors[i], sw=3)
# draw motors on the right
for i in range(4):
    my = 100 + i*52
    S.motor(800, my, 60, 26)
    S.text(870, my+18, names[i], 12, colors[i], "start", "bold")
    S.text(870, my+18, "", 12)
S.badge(830, 340, 5)
S.text(830, 370, "Motors spin the wheels", 14, INK, "middle", "bold")
S.save("01-big-picture.svg")

# =========================================================================
# 2. Gamepad controls
# =========================================================================
S = SVG(900, 470, "The gamepad: what each stick does")
S.gamepad(450, 240, 1.6, sticks=((0, -1), (1, 0)))
# left stick callouts
S.box(40, 90, 250, 150, "white", ORANGE, 12, 3)
S.text(165, 118, "LEFT stick", 18, ORANGE, "middle", "bold")
S.lines(55, 148, ["Push UP  ->  drive forward", "Push DOWN  ->  drive backward", "Push LEFT  ->  slide (strafe) left", "Push RIGHT  ->  slide (strafe) right"], 14)
S.arrow(290, 165, 372, 240, ORANGE, 3, 10)
# right stick callouts
S.box(610, 90, 250, 120, "white", BLUE, 12, 3)
S.text(735, 118, "RIGHT stick", 18, BLUE, "middle", "bold")
S.lines(625, 148, ["Push LEFT  ->  spin left", "Push RIGHT  ->  spin right", "Up / down does nothing"], 14)
S.arrow(610, 165, 520, 240, BLUE, 3, 10)
# bottom note
S.box(120, 385, 660, 60, LIGHT, "none", 10, 0)
S.lines(450, 410, ["You can do all three at once! Push the left stick forward AND right",
                   "while turning with the right stick. The program mixes them together."], 14, INK, "middle")
S.save("02-gamepad.svg")

# =========================================================================
# 3. Stick to numbers
# =========================================================================
S = SVG(900, 440, "A joystick is a number machine")
def stick_field(S, cx, cy, ox, oy, caption, vals):
    S.box(cx-70, cy-70, 140, 140, "white", INK, 10, 2)
    S.line(cx-70, cy, cx+70, cy, GRAY, 1.5, "4 4")
    S.line(cx, cy-70, cx, cy+70, GRAY, 1.5, "4 4")
    S.circle(cx, cy, 22, "#3a352e")
    S.circle(cx+ox*45, cy+oy*45, 16, "#6b6459", INK, 2)
    S.circle(cx+ox*45, cy+oy*45, 6, "#8a8378")
    S.text(cx, cy+100, caption, 15, INK, "middle", "bold")
    for i, v in enumerate(vals):
        S.text(cx, cy+122+i*18, v, 13, ORANGE if i == 0 else BLUE, "middle", "bold")
stick_field(S, 150, 180, 0, 0, "Stick in the middle", ["left_stick_x = 0.0", "left_stick_y = 0.0"])
stick_field(S, 400, 180, 0, -1, "Pushed all the way UP", ["left_stick_x = 0.0", "left_stick_y = -1.0"])
stick_field(S, 650, 180, 0.6, 0, "Pushed a bit RIGHT", ["left_stick_x = 0.6", "left_stick_y = 0.0"])
S.box(30, 355, 840, 70, "#FFF1E6", ORANGE, 10, 2)
S.lines(450, 380, ["Surprise! Pushing UP gives a NEGATIVE number. That's just how gamepads work.",
                   "So the program flips it:   drive = -gamepad1.left_stick_y   (now UP means +1 = forward)"], 14, INK, "middle")
S.save("03-stick-numbers.svg")

# =========================================================================
# 4. Control Hub wiring
# =========================================================================
S = SVG(900, 520, "Control Hub: which plug goes to which wheel")
px, py, pw = S.hub(330, 90, 240, 130)
# battery
S.box(590, 100, 70, 40, "#2b2b2b", INK, 6, 2)
S.text(625, 125, "12 V", 13, YELLOW, "middle", "bold")
S.box(575, 112, 15, 16, YELLOW, INK, 2, 1.5)
S.text(625, 160, "battery", 12, GRAY, "middle")
colors = [ORANGE, BLUE, GREEN, PURPLE]
targets = [("front_left", 80, 300), ("front_right", 300, 300), ("back_left", 520, 300), ("back_right", 740, 300)]
for i, (name, tx, ty) in enumerate(targets):
    cx = px + i*pw + pw/2
    S.path(f"M{cx},{py+2} L{cx},{py+30} L{tx+35},{ty-30} L{tx+35},{ty}", stroke=colors[i], sw=3.5)
    S.motor(tx, ty, 70, 28)
    S.text(tx+35, ty+48, f"port {i}", 14, colors[i], "middle", "bold")
    S.text(tx+35, ty+68, f'"{name}"', 14, INK, "middle", "bold")
    S.wheel(tx+15, ty+85, 40, 60, "\\" if i in (0, 3) else "/")
S.box(40, 460, 820, 44, LIGHT, "none", 10, 0)
S.lines(450, 478, ['The name in quotes must be typed EXACTLY the same in the Driver Station config and in the code.',
                   'Port 0 -> "front_left"    Port 1 -> "front_right"    Port 2 -> "back_left"    Port 3 -> "back_right"'], 13, INK, "middle")
S.save("04-control-hub-wiring.svg")

# =========================================================================
# 5. Robot top view
# =========================================================================
S = SVG(760, 700, "Top view: naming the four wheels")
S.strafer(380, 330, 1.25)
# FRONT arrow
S.arrow(380, 130, 380, 80, ORANGE, 8, 20)
S.text(380, 70, "FRONT", 22, ORANGE, "middle", "bold")
S.text(150, 560, "LEFT side", 16, INK, "middle", "bold")
S.text(610, 560, "RIGHT side", 16, INK, "middle", "bold")
S.box(400, 585, 330, 90, "white", INK, 10, 2, "6 4")
S.lines(565, 612, ["Stand BEHIND the robot,", "looking at the front.", "Your left = robot's left."], 13, INK, "middle")
S.box(30, 585, 330, 90, "white", INK, 10, 2, "6 4")
S.lines(195, 612, ["The yellow roller stripes", "make an X when you look", "down from above."], 13, INK, "middle")
S.save("05-robot-top-view.svg")

# =========================================================================
# 6. Mecanum moves
# =========================================================================
S = SVG(960, 470, "Mecanum magic: three moves, one set of wheels")
moves = [
    ("DRIVE forward", "left stick UP", {"FL": 1, "FR": 1, "BL": 1, "BR": 1}, (0, -1), "up"),
    ("STRAFE right", "left stick RIGHT", {"FL": 1, "FR": -1, "BL": -1, "BR": 1}, (1, 0), "right"),
    ("TURN right", "right stick RIGHT", {"FL": 1, "FR": -1, "BL": 1, "BR": -1}, (0, 0), "spin"),
]
for i, (title, stick, arrows, _, motion) in enumerate(moves):
    cx = 165 + i*315
    S.text(cx, 85, title, 20, INK, "middle", "bold")
    S.text(cx, 106, stick, 14, ORANGE, "middle", "bold")
    S.strafer(cx, 270, 0.72, wheel_arrows=arrows, labels=False, show_hub=False)
    # robot motion arrow
    if motion == "up":
        S.arrow(cx, 262, cx, 210, INK, 7, 18)
    elif motion == "right":
        S.arrow(cx-25, 240, cx+30, 240, INK, 7, 18)
    else:
        S.path(f"M{cx-30},{225} A34,34 0 1 1 {cx+30},{225}", stroke=INK, sw=6)
        S.arrow(cx+28, 222, cx+34, 238, INK, 6, 16)
S.box(30, 410, 900, 46, LIGHT, "none", 10, 0)
S.circle(70, 433, 8, GREEN); S.text(85, 438, "= wheel spins forward", 14)
S.circle(300, 433, 8, RED); S.text(315, 438, "= wheel spins backward", 14)
S.text(560, 438, "Angled rollers turn mixed spins into sideways motion!", 14, INK)
S.save("06-mecanum-moves.svg")

# =========================================================================
# 7. Program loop
# =========================================================================
S = SVG(960, 360, "What the program does, over and over")
steps = [("INIT", "Find the 4 motors\nby their names", GRAY),
         ("START", "Driver presses\nSTART", GRAY),
         ("1. Read", "left stick,\nright stick", ORANGE),
         ("2. Math", "mix into 4 wheel\npowers", BLUE),
         ("3. Power", "send powers\nto the motors", GREEN),
         ("4. Show", "numbers on the\nDriver Station", PURPLE)]
x = 30
boxes = []
for i, (t, d, col) in enumerate(steps):
    w = 135
    S.box(x, 100, w, 110, "white", col, 12, 3)
    S.text(x+w/2, 130, t, 17, col, "middle", "bold")
    for j, ln in enumerate(d.split("\n")):
        S.text(x+w/2, 160+j*18, ln, 13, INK, "middle")
    boxes.append((x, w))
    if i < len(steps)-1:
        S.arrow(x+w+2, 155, x+w+18, 155, INK, 3, 9)
    x += w + 20
# loop back arrow from 4 to 1
x4 = boxes[5][0] + boxes[5][1]/2; x1 = boxes[2][0] + boxes[2][1]/2
S.path(f"M{x4},212 L{x4},250 L{x1},250 L{x1},214", stroke=ORANGE, sw=4)
S.arrow(x1, 224, x1, 212, ORANGE, 4, 12)
S.text((x1+x4)/2, 272, "repeat about 50 times every second, until the driver presses STOP", 14, ORANGE, "middle", "bold")
S.box(30, 295, 900, 46, LIGHT, "none", 10, 0)
S.text(480, 323, "This is called a LOOP. Because it repeats so fast, the robot reacts the instant you move the stick.", 14, INK, "middle")
S.save("07-program-loop.svg")

# =========================================================================
# 8. The math recipe + normalizing
# =========================================================================
S = SVG(960, 460, "The recipe: mixing three numbers into four wheel powers")
S.text(30, 90, "Three ingredients from the sticks:", 16, INK, "start", "bold")
S.pill(30, 100, "drive", ORANGE); S.pill(120, 100, "strafe", BLUE); S.pill(215, 100, "turn", GREEN)
S.text(320, 119, "each one is a number from -1.0 to +1.0", 14, GRAY)
rows = [("front_left", "+", "+"), ("front_right", "-", "-"), ("back_left", "-", "+"), ("back_right", "+", "-")]
y = 160
for name, s1, s2 in rows:
    S.box(30, y-22, 520, 36, "white", EDGE, 8, 1.5)
    S.text(40, y+4, name, 15, INK, "start", "bold")
    S.text(150, y+4, "=", 15, INK)
    S.pill(170, y-14, "drive", ORANGE, 13)
    S.text(240, y+4, s1, 18, INK, "middle", "bold")
    S.pill(255, y-14, "strafe", BLUE, 13)
    S.text(335, y+4, s2, 18, INK, "middle", "bold")
    S.pill(350, y-14, "turn", GREEN, 13)
    y += 46
S.text(30, 355, "Try it: left stick straight up  ->  drive = 1, strafe = 0, turn = 0  ->  every wheel gets +1. Forward!", 14, INK)
S.text(30, 378, "Try it: left stick right  ->  drive = 0, strafe = 1, turn = 0  ->  FL +1, FR -1, BL -1, BR +1. Slide right!", 14, INK)
# normalize chart
S.text(620, 90, "Too much? Shrink them all together.", 16, INK, "start", "bold")
S.text(620, 110, "A motor can't go past 1.0 (100%).", 13, GRAY)
base = 300
vals = [1.4, 0.6, 1.0, 0.2]
labels = ["FL", "FR", "BL", "BR"]
S.line(620, base, 930, base, INK, 2)
S.line(620, base-140, 930, base-140, RED, 1.5, "5 4")
S.text(935, base-136, "1.0", 12, RED)
for i, v in enumerate(vals):
    bx = 630 + i*75
    S.box(bx, base-v*100, 24, v*100, GRAY, "none", 3, 0)
    S.box(bx+30, base-(v/1.4)*100, 24, (v/1.4)*100, ORANGE, "none", 3, 0)
    S.text(bx+27, base+18, labels[i], 13, INK, "middle", "bold")
S.text(775, 345, "grey = before, orange = after dividing everything by 1.4", 12, GRAY, "middle")
S.text(775, 365, "The shape stays the same, so the robot still moves the right way.", 12, GRAY, "middle")
S.box(30, 400, 900, 46, LIGHT, "none", 10, 0)
S.text(480, 428, "That's the whole drive program: read, mix, shrink if needed, send. Everything else is comments!", 14, INK, "middle")
S.save("08-math-recipe.svg")

# =========================================================================
# 9. Direction test
# =========================================================================
S = SVG(960, 400, "Testing motor directions (do this after any rebuild)")
# robot on blocks, side view
S.box(80, 250, 260, 40, "#d9d9d9", INK, 4, 2)   # frame
for wx in (100, 280):
    S.circle(wx, 275, 34, "#2b2b2b", INK, 2)
    S.circle(wx, 275, 12, YELLOW)
S.box(150, 290, 80, 40, "#c19a6b", INK, 4, 2)   # block
S.text(190, 315, "block", 12, "white", "middle", "bold")
S.line(40, 330, 380, 330, INK, 3)
S.text(210, 355, "Wheels in the air = safe to test", 14, INK, "middle", "bold")
S.arrow(60, 275, 60, 235, GREEN, 4, 12); S.text(60, 225, "spin?", 12, GREEN, "middle", "bold")
steps = ["1. Put the robot on a block so no wheel touches the floor.",
         "2. INIT and START the Strafer TeleOp.",
         "3. Push the LEFT stick straight forward and hold it.",
         "4. Look at each wheel. Would it roll the robot FORWARD?",
         "5. Wrong way? Swap FORWARD <-> REVERSE for THAT motor only",
         "     (in StraferTeleOp.java), then rebuild and test again.",
         "6. All four correct? Put it on the floor and drive!"]
S.lines(420, 100, steps, 15, INK, lh=28)
S.box(420, 300, 500, 70, "#FFF1E6", ORANGE, 10, 2)
S.lines(670, 325, ["Our robot needed: front_left FORWARD, back_left REVERSE,", "front_right FORWARD, back_right FORWARD."], 14, INK, "middle", weight="bold")
S.save("09-direction-test.svg")
print("done")
