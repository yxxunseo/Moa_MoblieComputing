package com.example.moa.controller

import com.example.moa.service.GroupService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
class JoinPageController(
    private val groupService: GroupService,
) {

    @GetMapping("/join.html", "/join", produces = [MediaType.TEXT_HTML_VALUE])
    fun joinPage(
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) from: String?,
    ): String {
        val safeCode = code?.trim()?.take(64)?.replace("<", "")?.replace(">", "") ?: ""
        val preview = if (safeCode.isNotBlank()) {
            runCatching { groupService.getInvitePreview(safeCode) }.getOrNull()
        } else {
            null
        }

        val inviterName = escapeHtml(
            from?.trim()?.take(20)?.takeIf { it.isNotBlank() }
                ?: preview?.get("inviterName")
                ?: "친구",
        )
        val groupName = escapeHtml(preview?.get("groupName") ?: "모임")
        val encodedCode = if (safeCode.isNotBlank()) {
            URLEncoder.encode(safeCode, StandardCharsets.UTF_8)
        } else {
            ""
        }
        val appLink = if (safeCode.isNotBlank()) "moa://join?code=$encodedCode" else ""
        val ogDescription = "${inviterName}님의 ${groupName} 모임에 초대되었어요."

        return """
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover" />
  <title>MOA 모임 초대</title>
  <meta property="og:type" content="website" />
  <meta property="og:title" content="${inviterName}님의 모임 초대" />
  <meta property="og:description" content="$ogDescription" />
  <meta property="og:image" content="/kakao-guest-share.png" id="og-image" />
  <meta property="og:url" content="" id="og-url" />
  <style>
    :root {
      --bg-top: #e8efff;
      --bg-bottom: #f4f6fc;
      --main: #2179fe;
      --text: #101b33;
      --sub: #6b7289;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Pretendard", "Segoe UI", sans-serif;
      background: linear-gradient(180deg, var(--bg-top) 0%, var(--bg-bottom) 55%, #ffffff 100%);
      color: var(--text);
      min-height: 100vh;
      min-height: 100dvh;
      -webkit-font-smoothing: antialiased;
    }
    .page {
      max-width: 480px;
      margin: 0 auto;
      min-height: 100vh;
      min-height: 100dvh;
      display: flex;
      flex-direction: column;
      padding: 56px 24px calc(24px + env(safe-area-inset-bottom));
    }
    .hero {
      text-align: center;
      margin-top: 12px;
    }
    .title {
      margin: 0;
      font-size: 26px;
      font-weight: 800;
      line-height: 1.45;
      letter-spacing: -0.4px;
      color: var(--text);
    }
    .title .name {
      color: var(--main);
    }
    .sub {
      margin: 14px 0 0;
      font-size: 15px;
      line-height: 1.55;
      color: var(--sub);
      font-weight: 500;
    }
    .illus {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px 0 12px;
      min-height: 240px;
    }
    .illus img {
      width: min(72vw, 280px);
      height: auto;
      object-fit: contain;
    }
    .shadow {
      width: 120px;
      height: 18px;
      margin-top: 8px;
      border-radius: 50%;
      background: rgba(33, 121, 254, 0.12);
      filter: blur(2px);
    }
    .cta {
      margin-top: auto;
      padding-top: 8px;
    }
    .btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      min-height: 56px;
      border: none;
      border-radius: 16px;
      background: var(--main);
      color: #fff;
      font-size: 17px;
      font-weight: 800;
      text-decoration: none;
      letter-spacing: -0.2px;
      box-shadow: 0 10px 24px rgba(33, 121, 254, 0.28);
    }
    .btn:active { opacity: 0.92; }
    .code-hint {
      margin-top: 14px;
      text-align: center;
      font-size: 12px;
      color: #9aa3b8;
    }
  </style>
</head>
<body>
  <div class="page">
    <header class="hero">
      <h1 class="title">
        <span class="name">$inviterName</span>님의 모임에<br />
        초대되었어요!
      </h1>
      <p class="sub">모임에 참여해서 일정을 조율해 보세요.</p>
    </header>

    <div class="illus">
      <img src="/join-mascot.png" alt="MOA 캐릭터" />
      <div class="shadow"></div>
    </div>

    <div class="cta">
      ${if (appLink.isNotBlank()) """<a class="btn" href="$appLink">모임 참여하기</a>""" else """<span class="btn" style="opacity:0.5">초대 코드가 없습니다</span>"""}
      ${if (safeCode.isNotBlank()) """<p class="code-hint">초대 코드 · $safeCode</p>""" else ""}
    </div>
  </div>
  <script>
    (function () {
      var origin = window.location.origin;
      var ogImage = document.getElementById("og-image");
      var ogUrl = document.getElementById("og-url");
      if (ogImage) ogImage.setAttribute("content", origin + "/kakao-guest-share.png");
      if (ogUrl) ogUrl.setAttribute("content", window.location.href);
    })();
  </script>
</body>
</html>
        """.trimIndent()
    }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
