#!/usr/bin/env bash
# MOA 로컬 개발: 백엔드 기동 + 실기기 adb reverse
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND="$ROOT/moa-backend"
LOG="/tmp/moa-backend.log"
PID_FILE="/tmp/moa-backend.pid"

start_backend() {
  if curl -sf http://127.0.0.1:8080/api/health >/dev/null 2>&1; then
    echo "[OK] Backend already running on :8080"
    return
  fi
  echo "[..] Starting backend (log: $LOG) ..."
  cd "$BACKEND"
  nohup ./gradlew bootRun >"$LOG" 2>&1 &
  echo $! >"$PID_FILE"
  for i in $(seq 1 40); do
    if curl -sf http://127.0.0.1:8080/api/health >/dev/null 2>&1; then
      echo "[OK] Backend ready"
      return
    fi
    sleep 1
  done
  echo "[FAIL] Backend did not start. tail -30 $LOG"
  tail -30 "$LOG"
  exit 1
}

setup_adb_reverse() {
  local devices
  devices=$(adb devices | awk 'NR>1 && $2=="device" {print $1}')
  if [ -z "$devices" ]; then
    echo "[WARN] No authorized adb device. Connect phone and allow USB debugging."
    return
  fi
  while IFS= read -r dev; do
    [ -z "$dev" ] && continue
    adb -s "$dev" reverse tcp:8080 tcp:8080
    echo "[OK] adb reverse on $dev"
  done <<< "$devices"
}

start_backend
setup_adb_reverse
echo ""
echo "App SERVER_URL (local.properties): http://127.0.0.1:8080/"
echo "Emulator auto-maps to 10.0.2.2 in the app."
echo "Health: $(curl -s http://127.0.0.1:8080/api/health)"
