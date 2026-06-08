#!/usr/bin/env bash
# 카카오톡 인앱 브라우저용 공개 URL (ngrok 무료 "방문 확인" 흰 화면 회피)
# 사용: ./scripts/dev-tunnel-cloudflare.sh
# 나온 https://xxxx.trycloudflare.com 을 local.properties WEB_SHARE_URL 에 넣고 앱 Rebuild
set -euo pipefail

if ! command -v cloudflared >/dev/null 2>&1; then
  echo "[FAIL] cloudflared 없음. 설치: brew install cloudflared"
  exit 1
fi

if ! curl -sf http://127.0.0.1:8080/api/health >/dev/null 2>&1; then
  echo "[WARN] 백엔드(8080)가 안 떠 있어요. 먼저 moa-backend 에서 ./gradlew bootRun"
  exit 1
fi

echo "[..] Cloudflare Tunnel 시작 (카카오 링크용 — ngrok 대신 이 URL 쓰세요)"
echo "     종료: Ctrl+C"
cloudflared tunnel --url http://127.0.0.1:8080
