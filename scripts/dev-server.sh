#!/usr/bin/env bash
# MOA 로컬 개발: 백엔드 기동 (+ 실기기만 adb reverse)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND="$ROOT/moa-backend"
LOG="/tmp/moa-backend.log"
PID_FILE="/tmp/moa-backend.pid"
NGROK_LOG="/tmp/moa-ngrok.log"
NGROK_PID_FILE="/tmp/moa-ngrok.pid"

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

start_ngrok() {
  local public_url=""
  if curl -sf http://127.0.0.1:4040/api/tunnels >/dev/null 2>&1; then
    public_url=$(curl -s http://127.0.0.1:4040/api/tunnels \
      | python3 -c "import sys,json; ts=json.load(sys.stdin).get('tunnels',[]); print(ts[0]['public_url'] if ts else '')" 2>/dev/null || true)
  fi
  if [ -n "$public_url" ]; then
    echo "[OK] ngrok already running: $public_url"
    return
  fi
  if ! command -v ngrok >/dev/null 2>&1; then
    echo "[WARN] ngrok not installed — 단기일정 공유 링크는 Mac에서 ngrok http 8080 실행 필요"
    return
  fi
  echo "[..] Starting ngrok (log: $NGROK_LOG) ..."
  nohup ngrok http 8080 >"$NGROK_LOG" 2>&1 &
  echo $! >"$NGROK_PID_FILE"
  for i in $(seq 1 15); do
    if curl -sf http://127.0.0.1:4040/api/tunnels >/dev/null 2>&1; then
      public_url=$(curl -s http://127.0.0.1:4040/api/tunnels \
        | python3 -c "import sys,json; ts=json.load(sys.stdin).get('tunnels',[]); print(ts[0]['public_url'] if ts else '')" 2>/dev/null || true)
      if [ -n "$public_url" ]; then
        echo "[OK] ngrok ready: $public_url"
        echo "     guest 예: ${public_url}/guest.html?link=YOUR_LINK"
        return
      fi
    fi
    sleep 1
  done
  echo "[FAIL] ngrok did not start. tail -10 $NGROK_LOG"
  tail -10 "$NGROK_LOG"
}

setup_adb_reverse() {
  local devices
  devices=$(adb devices | awk 'NR>1 && $2=="device" {print $1}')
  if [ -z "$devices" ]; then
    echo "[INFO] No adb device. Emulator: start AVD, then run app from Android Studio."
    return
  fi
  while IFS= read -r dev; do
    [ -z "$dev" ] && continue
    if [[ "$dev" == emulator-* ]]; then
      echo "[OK] Emulator $dev — SERVER_URL=http://10.0.2.2:8080/ (adb reverse 불필요)"
      continue
    fi
    adb -s "$dev" reverse tcp:8080 tcp:8080
    echo "[OK] adb reverse on $dev (실기기)"
  done <<< "$devices"
}

start_backend
start_ngrok
setup_adb_reverse
echo ""
echo "에뮬레이터: local.properties SERVER_URL=http://10.0.2.2:8080/"
echo "실기기 ADB: SERVER_URL=http://127.0.0.1:8080/ + adb reverse tcp:8080 tcp:8080"
echo "단기일정 공유: ngrok 켜진 상태에서만 WEB_SHARE_URL 링크가 열림 (Mac 재부팅/종료 시 ./scripts/dev-server.sh 다시 실행)"
echo "Health: $(curl -s http://127.0.0.1:8080/api/health)"
