#!/bin/bash
# Install the latest TeamCode APK on the Control Hub over Wi-Fi and copy the robot's
# configuration and recent logs into the project, so they can be read later offline.
#
# 1. Join the Control Hub's Wi-Fi network on this Mac.
# 2. From the project folder run:   bash tools/robot-sync.sh
#    Add --logs-only to skip the install and just collect configs and logs
#    (useful after running a test).
# 3. Switch back to your normal Wi-Fi.
#
# Results land in robot-sync/ (configs, logs, a summary). That folder is not committed.

set -u
cd "$(dirname "$0")/.." || exit 1

HUB="192.168.43.1:5555"
APK="TeamCode/build/outputs/apk/debug/TeamCode-debug.apk"
PKG="com.qualcomm.ftcrobotcontroller"
OUT="robot-sync"
STAMP="$(date +%Y%m%d-%H%M%S)"
INSTALL=1
[ "${1:-}" = "--logs-only" ] && INSTALL=0

mkdir -p "$OUT/configs" "$OUT/logs"
SUMMARY="$OUT/summary-$STAMP.txt"
log() { echo "$@" | tee -a "$SUMMARY"; }

log "robot-sync $STAMP"

# ---- connect ----------------------------------------------------------------
if ! ping -c 1 -t 3 192.168.43.1 >/dev/null 2>&1; then
  log "Can't reach the Control Hub at 192.168.43.1."
  log "Join the Control Hub's Wi-Fi network first (name is on the hub label and the Driver Hub)."
  exit 1
fi
adb connect "$HUB" >/dev/null 2>&1
for i in 1 2 3 4 5 6; do
  adb -s "$HUB" get-state 2>/dev/null | grep -q device && break
  sleep 2; adb connect "$HUB" >/dev/null 2>&1
done
if ! adb -s "$HUB" get-state 2>/dev/null | grep -q device; then
  log "adb could not connect to $HUB."
  exit 1
fi
log "Connected to Control Hub ($HUB)."

# ---- install ----------------------------------------------------------------
if [ "$INSTALL" = 1 ]; then
  if [ ! -f "$APK" ]; then
    log "No APK at $APK. Build it first in Android Studio (Build > Make Project) or with ./gradlew :TeamCode:assembleDebug"
    exit 1
  fi
  log "Installing APK built $(stat -f '%Sm' "$APK") ..."
  result="$(adb -s "$HUB" install -r "$APK" 2>&1 | tail -1)"
  log "  $result"
  if [ "$result" = "Success" ]; then
    adb -s "$HUB" shell am force-stop "$PKG"
    adb -s "$HUB" shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
    log "  Robot Controller app restarted."
  else
    log "  Install failed. Try running the script again."
  fi
fi

# ---- collect configs and logs ----------------------------------------------
for f in $(adb -s "$HUB" shell ls /sdcard/FIRST/ 2>/dev/null | tr -d '\r' | grep -i '\.xml$'); do
  adb -s "$HUB" pull "/sdcard/FIRST/$f" "$OUT/configs/$f" >/dev/null 2>&1 && log "Saved config: $OUT/configs/$f"
done
log "Blocks programs on hub:  $(adb -s "$HUB" shell ls /sdcard/FIRST/blocks/ 2>/dev/null | tr -d '\r' | grep '\.blk$' | tr '\n' ' ')"
log "OnBot Java files on hub: $(adb -s "$HUB" shell find /sdcard/FIRST/java/src -name '*.java' 2>/dev/null | tr -d '\r' | sed 's|.*/||' | tr '\n' ' ')"
adb -s "$HUB" logcat -d -v time 2>/dev/null | grep -E "RobotCore|Robocol|OpMode|HwTest|LauncherTune|BallPathTest|Apex|System\\\$Error|Exception" \
  > "$OUT/logs/logcat-$STAMP.txt"
log "Saved log:    $OUT/logs/logcat-$STAMP.txt ($(wc -l < "$OUT/logs/logcat-$STAMP.txt" | tr -d ' ') lines)"

adb disconnect "$HUB" >/dev/null 2>&1
log ""
log "Done. Switch the Mac back to your normal Wi-Fi."
