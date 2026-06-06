#!/usr/bin/env python3
"""Generate Moa launcher icons and in-app mascot assets from source PNGs."""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
ASSETS = ROOT / "scripts" / "assets"

BASIC = ASSETS / "ic_mascot_basic.png"
SPARKLE = ASSETS / "ic_mascot_sparkle.png"
HEART = ASSETS / "ic_mascot_heart.png"

# Adaptive icon layer size (108dp)
ADAPTIVE_SIZES = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}

CHARACTER_SCALE = 0.78
IN_APP_MAX_PX = 1024
IN_APP_PADDING_RATIO = 0.12

LAUNCHER_SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

# MoaAccentBlueBg
LAUNCHER_BG = (234, 241, 255, 255)


def remove_solid_background(img: Image.Image, threshold: int = 40) -> Image.Image:
    """흰색·검정 등 단색 배경 제거."""
    img = img.convert("RGBA")
    pixels = img.load()
    w, h = img.size
    corners = [
        pixels[0, 0][:3],
        pixels[w - 1, 0][:3],
        pixels[0, h - 1][:3],
        pixels[w - 1, h - 1][:3],
    ]
    bg_r = sum(c[0] for c in corners) // 4
    bg_g = sum(c[1] for c in corners) // 4
    bg_b = sum(c[2] for c in corners) // 4
    is_dark_bg = max(bg_r, bg_g, bg_b) < 48

    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if is_dark_bg:
                if max(r, g, b) <= threshold:
                    pixels[x, y] = (r, g, b, 0)
                    continue
            else:
                if r >= 238 and g >= 238 and b >= 238:
                    pixels[x, y] = (r, g, b, 0)
                    continue
            dist = abs(r - bg_r) + abs(g - bg_g) + abs(b - bg_b)
            if dist < 42:
                if is_dark_bg and max(r, g, b) <= threshold + 20:
                    pixels[x, y] = (r, g, b, 0)
                elif not is_dark_bg and max(r, g, b) > 160:
                    pixels[x, y] = (r, g, b, 0)
    return img


def square_crop(img: Image.Image, target: int = 1024) -> Image.Image:
    w, h = img.size
    side = min(w, h)
    left = (w - side) // 2
    top = (h - side) // 2
    cropped = img.crop((left, top, left + side, top + side))
    if side != target:
        cropped = cropped.resize((target, target), Image.Resampling.LANCZOS)
    return cropped


def fit_character_on_canvas(fg: Image.Image, canvas_size: int, scale: float = CHARACTER_SCALE) -> Image.Image:
    fg = fg.copy()
    bbox = fg.getbbox()
    if bbox:
        fg = fg.crop(bbox)
    target = max(1, int(canvas_size * scale))
    ratio = min(target / fg.width, target / fg.height)
    new_w = max(1, int(fg.width * ratio))
    new_h = max(1, int(fg.height * ratio))
    fg = fg.resize((new_w, new_h), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    x = (canvas_size - new_w) // 2
    y = (canvas_size - new_h) // 2
    canvas.alpha_composite(fg, (x, y))
    return canvas


def composite_on_background(fg: Image.Image, size: int, bg_color: tuple[int, int, int, int]) -> Image.Image:
    padded = fit_character_on_canvas(fg, size, CHARACTER_SCALE)
    canvas = Image.new("RGBA", (size, size), bg_color)
    canvas.alpha_composite(padded)
    return canvas.convert("RGB")


def save_webp(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, format="WEBP", quality=92, method=6, lossless=False)


def save_png(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, format="PNG", optimize=True)


def export_in_app_character(
    fg_raw: Image.Image,
    path: Path,
    max_px: int = IN_APP_MAX_PX,
    padding_ratio: float = IN_APP_PADDING_RATIO,
) -> None:
    fg = fg_raw.copy()
    bbox = fg.getbbox()
    if bbox:
        fg = fg.crop(bbox)

    pad = max(12, int(max(fg.width, fg.height) * padding_ratio))
    canvas = Image.new("RGBA", (fg.width + pad * 2, fg.height + pad * 2), (0, 0, 0, 0))
    canvas.alpha_composite(fg, (pad, pad))
    fg = canvas

    scale = min(max_px / fg.width, max_px / fg.height)
    new_w = max(1, int(fg.width * scale))
    new_h = max(1, int(fg.height * scale))
    if (new_w, new_h) != fg.size:
        fg = fg.resize((new_w, new_h), Image.Resampling.LANCZOS)
    save_png(fg, path)


def load_foreground(source: Path) -> Image.Image:
    master = square_crop(Image.open(source).convert("RGBA"))
    return remove_solid_background(master)


def generate_launcher_icons(fg_raw: Image.Image) -> None:
    fg_master = fit_character_on_canvas(fg_raw, 1024, CHARACTER_SCALE)

    for density, px in ADAPTIVE_SIZES.items():
        fg = fit_character_on_canvas(fg_raw, px, CHARACTER_SCALE)
        out = RES / f"drawable-{density}" / "ic_launcher_foreground.png"
        save_png(fg, out)
        print(f"wrote {out.relative_to(ROOT)}")

    for density, px in LAUNCHER_SIZES.items():
        icon = composite_on_background(fg_master, px, LAUNCHER_BG)
        for name in ("ic_launcher", "ic_launcher_round"):
            out = RES / f"mipmap-{density}" / f"{name}.webp"
            save_webp(icon, out)
            print(f"wrote {out.relative_to(ROOT)}")

    play = composite_on_background(fg_master, 512, LAUNCHER_BG)
    play_path = ROOT / "app" / "src" / "main" / "ic_launcher-playstore.png"
    save_png(play, play_path)
    print(f"wrote {play_path.relative_to(ROOT)}")


def main() -> None:
    for path in (BASIC, SPARKLE, HEART):
        if not path.is_file():
            print(f"Missing asset: {path}", file=sys.stderr)
            sys.exit(1)

    fg_basic = load_foreground(BASIC)
    fg_sparkle = load_foreground(SPARKLE)
    fg_heart = load_foreground(HEART)

    print("== launcher (sparkle) ==")
    generate_launcher_icons(fg_sparkle)

    print("== in-app mascots ==")
    export_in_app_character(fg_basic, RES / "drawable" / "ic_character.png")
    print(f"wrote {RES / 'drawable' / 'ic_character.png'}")

    export_in_app_character(fg_sparkle, RES / "drawable" / "ic_character_sparkle.png")
    print(f"wrote {RES / 'drawable' / 'ic_character_sparkle.png'}")

    export_in_app_character(fg_heart, RES / "drawable" / "ic_character_heart.png")
    print(f"wrote {RES / 'drawable' / 'ic_character_heart.png'}")

    ASSETS.mkdir(parents=True, exist_ok=True)
    save_png(square_crop(Image.open(SPARKLE).convert("RGBA")), ASSETS / "ic_launcher_master.png")
    print(f"wrote {ASSETS / 'ic_launcher_master.png'}")


if __name__ == "__main__":
    main()
