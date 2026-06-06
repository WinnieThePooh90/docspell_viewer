#!/usr/bin/env bash
# Docspell-Launcher-Icons aus dem offiziellen Artwork-Repository.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RES="$ROOT/app/src/main/res"
ART="https://raw.githubusercontent.com/eikek/docspell/master/artwork"
TMP="${TMPDIR:-/tmp}/docspell-icons"

mkdir -p "$TMP"
curl -fsSL "$ART/logo-48.png" -o "$TMP/logo-48.png"
curl -fsSL "$ART/logo-96.png" -o "$TMP/logo-96.png"
curl -fsSL "$ART/logo-400.png" -o "$TMP/logo-400.png"

rm -rf "$RES/mipmap-anydpi-v26"
rm -f "$RES"/drawable-*/ic_launcher_foreground.png

for dpi in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  cp "$TMP/logo-48.png" "$RES/mipmap-$dpi/ic_launcher.png"
  cp "$TMP/logo-48.png" "$RES/mipmap-$dpi/ic_launcher_round.png"
done
cp "$TMP/logo-96.png" "$RES/mipmap-hdpi/ic_launcher.png"
cp "$TMP/logo-96.png" "$RES/mipmap-hdpi/ic_launcher_round.png"
cp "$TMP/logo-96.png" "$RES/mipmap-xhdpi/ic_launcher.png"
cp "$TMP/logo-96.png" "$RES/mipmap-xhdpi/ic_launcher_round.png"
cp "$TMP/logo-400.png" "$RES/mipmap-xxhdpi/ic_launcher.png"
cp "$TMP/logo-400.png" "$RES/mipmap-xxhdpi/ic_launcher_round.png"
cp "$TMP/logo-400.png" "$RES/mipmap-xxxhdpi/ic_launcher.png"
cp "$TMP/logo-400.png" "$RES/mipmap-xxxhdpi/ic_launcher_round.png"

echo "Docspell-Icons nach $RES/mipmap-* installiert."
