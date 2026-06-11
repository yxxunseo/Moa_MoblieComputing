#!/usr/bin/env bash
# MOA 로컬 개발: 백엔드 기동 (+ 실기기만 adb reverse)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND="$ROOT/moa-backend"
JAR="$BACKEND/build/libs/moa-backend-0.0.1-SNAPSHOT.jar"
LOG="/tmp/moa-backend.log"
PID_FILE="/tmp/moa-backend-java.pid"
NGROK_LOG="/tmp/moa-ngrok.log"
NGROK_PID_FILE="/tmp/moa-ngrok.pid"

stop_backend() {
  if [ -f "$PID_FILE" ]; then
    kill "$(cat "$PID_FILE")" 2>/dev/null || true
    rm -f "$PID_FILE"
  fi
  lsof -ti:8080 2>/dev/null | xargs kill -9 2>/dev/null || true
}

start_backend() {
  if curl -sf http://127.0.0.1:8080/api/health >/dev/null 2>&1; then
    echo "[OK] Backend already running on :8080"
    return
  fi
  stop_backend
  echo "[..] Building backend jar (if needed) ..."
  (cd "$BACKEND" && ./gradlew bootJar -q)
  if [ ! -f "$JAR" ]; then
    echo "[FAIL] Jar not found: $JAR"
    exit 1
  fi
  echo "[..] Starting backend jar (log: $LOG) ..."
  nohup java -jar "$JAR" >>"$LOG" 2>&1 </dev/null &
  local pid=$!
  echo "$pid" >"$PID_FILE"
  disown "$pid" 2>/dev/null || true
  for i in $(seq 1 40); do
    if curl -sf http://127.0.0.1:8080/api/health >/dev/null 2>&1; then
      echo "[OK] Backend ready (pid=$(cat "$PID_FILE"))"
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

print_ngrok_url() {
  curl -s http://127.0.0.1:4040/api/tunnels 2>/dev/null \
    | python3 -c "import sys,json; ts=json.load(sys.stdin).get('tunnels',[]); print(ts[0]['public_url'] if ts else '')" 2>/dev/null || true
}

start_backend
start_ngrok
setup_adb_reverse

MAC_IP=$(ipconfig getifaddr en0 2>/dev/null || ipconfig getifaddr en1 2>/dev/null || echo "unknown")
NGROK_URL=$(print_ngrok_url)

echo ""
echo "에뮬레이터: local.properties SERVER_URL=http://10.0.2.2:8080/"
echo "실기기 LAN (같은 Wi-Fi): SERVER_URL=http://${MAC_IP}:8080/"
echo "실기기 ADB: SERVER_URL=http://127.0.0.1:8080/ + adb reverse tcp:8080 tcp:8080"
if [ -n "$NGROK_URL" ]; then
  echo "실기기 ngrok (Wi-Fi/LTE): SERVER_URL=${NGROK_URL}/"
fi
echo "단기일정 공유: ngrok 켜진 상태에서만 WEB_SHARE_URL 링크가 열림"
echo "Health: $(curl -s http://127.0.0.1:8080/api/health)"
