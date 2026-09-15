#!/usr/bin/env python3
"""Render a docs/*.md file (with SVG figures) to PDF via headless Chrome.

Usage:  python3 docs/build_pdf.py docs/robot-v1-design.md [more.md ...]
Output: docs/pdf/<name>.pdf
Reuses the markdown converter in TeamCode/docs/build_html.py.
"""
import html
import os
import re
import subprocess
import sys
import tempfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(REPO, "TeamCode", "docs"))
import build_html as bh  # noqa: E402

CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

PRINT_CSS = """
@page { size: Letter; margin: 0.6in 0.65in 0.7in 0.65in; }
body { background: #fff; font-size: 11pt; line-height: 1.45; }
main { max-width: none; margin: 0; padding: 0; box-shadow: none; background: #fff; }
h1 { font-size: 22pt; margin-top: 0; }
h2 { font-size: 15pt; break-after: avoid; page-break-after: avoid; margin-top: 1.4em; }
h3 { font-size: 12pt; break-after: avoid; page-break-after: avoid; }
table { font-size: 9.5pt; break-inside: auto; }
tr, td, th { break-inside: avoid; page-break-inside: avoid; }
pre { font-size: 9pt; break-inside: avoid; page-break-inside: avoid; }
figure { break-inside: avoid; page-break-inside: avoid; margin: 1em 0; }
figure svg { box-shadow: none; border: 1px solid #E5E0D8; }
blockquote { break-inside: avoid; }
hr { display: none; }
a { color: inherit; text-decoration: none; }
.footer { position: fixed; bottom: 0; right: 0; font-size: 8pt; color: #9CA3AF; }
"""


def build(md_path):
    md_path = os.path.abspath(md_path)
    md_dir = os.path.dirname(md_path)
    # build_html resolves images relative to HERE/..; point HERE at <md_dir>/img so "img/x.svg" resolves
    bh.HERE = os.path.join(md_dir, "img")
    with open(md_path, encoding="utf-8") as f:
        md = f.read()
    title = re.search(r"^#\s+(.*)", md, re.M).group(1)
    body = bh.convert(md)
    doc = (
        "<!doctype html><html lang='en'><head><meta charset='utf-8'>"
        f"<title>{html.escape(title)}</title><style>{bh.CSS}{PRINT_CSS}</style></head>"
        f"<body><main>{body}</main></body></html>"
    )
    out_dir = os.path.join(REPO, "docs", "pdf")
    os.makedirs(out_dir, exist_ok=True)
    pdf = os.path.join(out_dir, os.path.splitext(os.path.basename(md_path))[0] + ".pdf")
    with tempfile.NamedTemporaryFile("w", suffix=".html", delete=False, encoding="utf-8") as tmp:
        tmp.write(doc)
        tmp_path = tmp.name
    subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--no-pdf-header-footer",
                    f"--print-to-pdf={pdf}", f"file://{tmp_path}"],
                   check=True, capture_output=True)
    os.unlink(tmp_path)
    print("wrote", os.path.relpath(pdf, REPO), f"({os.path.getsize(pdf)//1024} KB)")
    return pdf


if __name__ == "__main__":
    for p in sys.argv[1:] or ["docs/robot-v1-design.md"]:
        build(p)
