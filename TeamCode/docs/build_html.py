#!/usr/bin/env python3
"""Builds docs/StraferTeleOp.html from TeamCode/README.md with every SVG embedded inline.

The result is one self-contained file that opens in any browser, no internet or GitHub needed.
Run:  python3 TeamCode/docs/build_html.py
"""
import html
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
README = os.path.join(HERE, "..", "README.md")
OUT = os.path.join(HERE, "StraferTeleOp.html")

CSS = """
:root{--ink:#1A1611;--orange:#FF8A3D;--yellow:#F5C518;--paper:#FFFDF7;--card:#F3EFE7;--edge:#E5E0D8;--muted:#6b6459}
*{box-sizing:border-box}
body{margin:0;background:#f6f2ea;color:var(--ink);font:17px/1.6 -apple-system,BlinkMacSystemFont,"Segoe UI",Arial,sans-serif}
main{max-width:900px;margin:0 auto;padding:32px 24px 80px;background:var(--paper);box-shadow:0 0 0 1px var(--edge)}
h1{font-size:2.2em;line-height:1.15;margin:.2em 0 .4em;border-bottom:6px solid var(--orange);padding-bottom:.3em}
h2{font-size:1.55em;margin:2.2em 0 .6em;padding-left:.55em;border-left:8px solid var(--orange)}
h3{font-size:1.2em;margin:1.6em 0 .4em;color:var(--muted)}
p{margin:.7em 0}
hr{border:0;border-top:2px dashed var(--edge);margin:2.5em 0}
a{color:#b35a17}
code{font-family:"SF Mono",Menlo,Consolas,monospace;font-size:.9em;background:var(--card);padding:.1em .35em;border-radius:5px}
pre{background:var(--ink);color:#fff;padding:16px 18px;border-radius:12px;overflow-x:auto;line-height:1.45}
pre code{background:none;color:inherit;padding:0;font-size:.92em}
blockquote{margin:1.2em 0;padding:12px 18px;background:var(--card);border-left:6px solid var(--yellow);border-radius:8px}
blockquote p{margin:.3em 0}
table{border-collapse:collapse;width:100%;margin:1em 0;font-size:.95em}
th,td{border:1px solid var(--edge);padding:8px 12px;text-align:left;vertical-align:top}
th{background:var(--card)}
tr:nth-child(even) td{background:#faf7f0}
ul,ol{padding-left:1.5em}
li{margin:.3em 0}
figure{margin:1.4em 0;text-align:center}
figure svg{width:100%;height:auto;max-width:100%;border-radius:18px;box-shadow:0 4px 18px rgba(26,22,17,.12)}
figcaption{font-size:.9em;color:var(--muted);margin-top:.4em}
em.foot{display:block;color:var(--muted);font-size:.95em}
@media print{body{background:#fff}main{box-shadow:none}h2{break-after:avoid}figure{break-inside:avoid}}
"""


def inline(text):
    """Inline markdown: links (whose text may contain code), code spans, bold, italics."""
    out = []
    for seg in re.split(r"(\[[^\]]+\]\([^)]+\))", text):
        m = re.match(r"^\[([^\]]+)\]\(([^)]+)\)$", seg)
        if m:
            href = m.group(2)
            if not re.match(r"^(https?:|#|mailto:)", href):
                href = "../" + href  # README links are relative to TeamCode/, this file lives in TeamCode/docs/
            out.append(f'<a href="{html.escape(href, quote=True)}">{_spans(m.group(1))}</a>')
        else:
            out.append(_spans(seg))
    return "".join(out)


def _spans(text):
    out = []
    for part in re.split(r"(`[^`]+`)", text):
        if part.startswith("`") and part.endswith("`") and len(part) > 1:
            out.append(f"<code>{html.escape(part[1:-1])}</code>")
            continue
        p = html.escape(part, quote=False)
        p = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", p)
        p = re.sub(r"(?<![\w*])\*(?!\s)(.+?)(?<!\s)\*(?![\w*])", r"<em>\1</em>", p)
        out.append(p)
    return "".join(out)


def svg_inline(path, alt):
    full = os.path.normpath(os.path.join(HERE, "..", path))
    with open(full, encoding="utf-8") as f:
        svg = f.read()
    # drop fixed pixel width/height so CSS can scale it; keep the viewBox
    svg = re.sub(r"\s(width|height)='\d+'", "", svg, count=2)
    # make clipPath ids unique so nine inlined SVGs don't collide
    prefix = re.sub(r"[^a-z0-9]", "", os.path.basename(path).lower())
    svg = re.sub(r"id='(id\d+)'", lambda m: f"id='{prefix}-{m.group(1)}'", svg)
    svg = re.sub(r"url\(#(id\d+)\)", lambda m: f"url(#{prefix}-{m.group(1)})", svg)
    svg = svg.replace("<svg ", f"<svg role='img' aria-label='{html.escape(alt)}' ", 1)
    return f"<figure>{svg}<figcaption>{html.escape(alt)}</figcaption></figure>"


def table(rows):
    def cells(row):
        return [c.strip() for c in row.strip().strip("|").split("|")]
    head = cells(rows[0])
    aligns = []
    for a in cells(rows[1]):
        a = a.strip()
        aligns.append("center" if a.startswith(":") and a.endswith(":") else "right" if a.endswith(":") else "left")
    out = ["<table><thead><tr>"]
    for i, h in enumerate(head):
        out.append(f'<th style="text-align:{aligns[i] if i < len(aligns) else "left"}">{inline(h)}</th>')
    out.append("</tr></thead><tbody>")
    for r in rows[2:]:
        out.append("<tr>")
        for i, c in enumerate(cells(r)):
            out.append(f'<td style="text-align:{aligns[i] if i < len(aligns) else "left"}">{inline(c)}</td>')
        out.append("</tr>")
    out.append("</tbody></table>")
    return "".join(out)


def convert(md):
    lines = md.splitlines()
    out, i, para = [], 0, []

    def flush():
        if para:
            text = " ".join(s.strip() for s in para)
            out.append(f"<p>{inline(text)}</p>")
            para.clear()

    while i < len(lines):
        ln = lines[i]
        s = ln.strip()
        if s.startswith("```"):
            flush()
            lang = s[3:].strip()
            i += 1
            buf = []
            while i < len(lines) and not lines[i].strip().startswith("```"):
                buf.append(lines[i]); i += 1
            cls = f' class="lang-{lang}"' if lang else ""
            out.append(f"<pre><code{cls}>{html.escape(chr(10).join(buf))}</code></pre>")
            i += 1; continue
        if not s:
            flush(); i += 1; continue
        if s == "---":
            flush(); out.append("<hr>"); i += 1; continue
        m = re.match(r"^(#{1,6})\s+(.*)", s)
        if m:
            flush(); lvl = len(m.group(1))
            out.append(f"<h{lvl}>{inline(m.group(2))}</h{lvl}>"); i += 1; continue
        m = re.match(r"^!\[([^\]]*)\]\(([^)]+)\)$", s)
        if m:
            flush(); out.append(svg_inline(m.group(2), m.group(1))); i += 1; continue
        if s.startswith(">"):
            flush(); buf = []
            while i < len(lines) and lines[i].strip().startswith(">"):
                buf.append(re.sub(r"^\s*>\s?", "", lines[i])); i += 1
            out.append(f"<blockquote>{convert(chr(10).join(buf))}</blockquote>"); continue
        if s.startswith("|"):
            flush(); buf = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                buf.append(lines[i]); i += 1
            out.append(table(buf)); continue
        m = re.match(r"^(\d+)\.\s+(.*)", s)
        if m or re.match(r"^[-*]\s+", s):
            flush()
            ordered = bool(m)
            tag = "ol" if ordered else "ul"
            start = f' start="{m.group(1)}"' if ordered and m.group(1) != "1" else ""
            items = []
            while i < len(lines):
                t = lines[i].strip()
                mm = re.match(r"^(\d+)\.\s+(.*)", t) if ordered else re.match(r"^[-*]\s+(.*)", t)
                if not mm: break
                items.append(mm.group(2) if ordered else mm.group(1)); i += 1
            out.append(f"<{tag}{start}>" + "".join(f"<li>{inline(x)}</li>" for x in items) + f"</{tag}>")
            continue
        if s.startswith("*") and s.endswith("*") and not s.startswith("**"):
            flush(); out.append(f'<em class="foot">{inline(s[1:-1])}</em>'); i += 1; continue
        para.append(ln); i += 1
    flush()
    return "\n".join(out)


def main():
    with open(README, encoding="utf-8") as f:
        md = f.read()
    body = convert(md)
    # links that point at repo files should open GitHub-relative paths; leave as-is but make them harmless offline
    title = re.search(r"^#\s+(.*)", md, re.M).group(1)
    doc = (
        "<!doctype html>\n<html lang='en'><head><meta charset='utf-8'>"
        "<meta name='viewport' content='width=device-width,initial-scale=1'>"
        f"<title>{html.escape(title)}</title><style>{CSS}</style></head>"
        f"<body><main>{body}</main></body></html>"
    )
    with open(OUT, "w", encoding="utf-8") as f:
        f.write(doc)
    print(f"wrote {os.path.relpath(OUT)} ({len(doc)//1024} KB)")


if __name__ == "__main__":
    main()
