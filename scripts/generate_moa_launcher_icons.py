#!/usr/bin/env python3
"""Generate Moa launcher icons from a high-res master PNG."""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
DEFAULT_MASTER = ROOT / "scripts" / "assets" / "ic_launcher_master.png"

# Adaptive icon layer size (108dp)
ADAPTIVE_SIZES = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}

# 캐릭터가 프레임에 꽉 차지 않도록 (Android adaptive safe zone ~66%)
CHARACTER_SCALE = 0.72

# Legacy launcher icon (48dp)
LAUNCHER_SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def remove_light_background(img: Image.Image, threshold: int = 238) -> Image.Image:
    """흰색·밝은 회색 그라데이션 배경 제거."""
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

    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if r >= threshold and g >= threshold and b >= threshold:
                pixels[x, y] = (r, g, b, 0)
                continue
            dist = abs(r - bg_r) + abs(g - bg_g) + abs(b - bg_b)
            if dist < 42 and max(r, g, b) > 160:
                pixels[x, y] = (r, g, b, 0)
    return img


def fit_character_on_canvas(fg: Image.Image, canvas_size: int, scale: float = CHARACTER_SCALE) -> Image.Image:
    """캐릭터를 중앙에 작게 배치해 여백 확보."""
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


def composite_on_white(fg: Image.Image, size: int) -> Image.Image:
    padded = fit_character_on_canvas(fg, size, CHARACTER_SCALE)
    canvas = Image.new("RGBA", (size, size), (255, 255, 255, 255))
    canvas.alpha_composite(padded)
    return canvas.convert("RGB")


def save_webp(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if img.mode != "RGBA":
        img.save(path, format="WEBP", quality=92, method=6)
    else:
        img.save(path, format="WEBP", quality=92, method=6, lossless=False)


def save_png(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, format="PNG", optimize=True)


def export_in_app_character(fg_raw: Image.Image, path: Path, max_px: int = 512) -> None:
    """앱 내 MoaMascot / 기본 프로필용 캐릭터 (투명 배경)."""
    fg = fg_raw.copy()
    bbox = fg.getbbox()
    if bbox:
        fg = fg.crop(bbox)
    ratio = min(max_px / fg.width, max_px / fg.height, 1.0)
    if ratio < 1.0:
        fg = fg.resize(
            (max(1, int(fg.width * ratio)), max(1, int(fg.height * ratio))),
            Image.Resampling.LANCZOS,
        )
    save_png(fg, path)


def main(master_path: Path) -> None:
    if not master_path.is_file():
        print(f"Master not found: {master_path}", file=sys.stderr)
        sys.exit(1)

    master = Image.open(master_path).convert("RGBA")
    w, h = master.size
    side = min(w, h)
    left = (w - side) // 2
    top = (h - side) // 2
    master = master.crop((left, top, left + side, top + side))
    if side < 1024:
        master = master.resize((1024, 1024), Image.Resampling.LANCZOS)
    fg_raw = remove_light_background(master)
    fg_master = fit_character_on_canvas(fg_raw, 1024, CHARACTER_SCALE)

    # Adaptive foreground (transparent, 여백 포함)
    for density, px in ADAPTIVE_SIZES.items():
        fg = fit_character_on_canvas(fg_raw, px, CHARACTER_SCALE)
        out = RES / f"drawable-{density}" / "ic_launcher_foreground.png"
        save_png(fg, out)
        print(f"wrote {out.relative_to(ROOT)}")

    # Legacy + round mipmaps (white background composite)
    for density, px in LAUNCHER_SIZES.items():
        icon = composite_on_white(fg_master, px)
        for name in ("ic_launcher", "ic_launcher_round"):
            out = RES / f"mipmap-{density}" / f"{name}.webp"
            save_webp(icon, out)
            print(f"wrote {out.relative_to(ROOT)}")

    # Play Store / high-res marketing
    play = composite_on_white(fg_master, 512)
    play_path = ROOT / "app" / "src" / "main" / "ic_launcher-playstore.png"
    save_png(play, play_path)
    print(f"wrote {play_path.relative_to(ROOT)}")

    assets_dir = ROOT / "scripts" / "assets"
    assets_dir.mkdir(parents=True, exist_ok=True)
    src_copy = assets_dir / "ic_launcher_source.png"
    save_png(master.convert("RGB"), src_copy)
    print(f"wrote {src_copy.relative_to(ROOT)}")

    # ic_character.png(앱 내부 마스코트)는 런처 아이콘과 별도 — 이 스크립트에서 덮어쓰지 않음


if __name__ == "__main__":
    master = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_MASTER
    main(master)
