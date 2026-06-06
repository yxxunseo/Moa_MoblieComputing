package com.example.moa_project.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.moa_project.BuildConfig
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link

/**
 * 단기 일정을 카카오톡 Feed 템플릿으로 공유한다.
 *
 * - [Link]에는 webUrl/mobileWebUrl만 사용 (androidExecutionParams 금지 → guest.html 직접 이동)
 * - WEB_SHARE_URL 도메인을 카카오 개발자 콘솔 [제품 링크] > Web 도메인에 등록해야 카드·버튼 클릭이 동작함
 */
object KakaoShareHelper {
    private const val TAG = "KakaoShareHelper"

    fun shareGuestSchedule(
        context: Context,
        scheduleTitle: String,
        scheduleDescription: String?,
        startDate: String,
        endDate: String,
        uniqueLink: String,
        webLink: String,
    ) {
        if (!GuestLinkHelper.isExternalReachable(webLink)) {
            Toast.makeText(
                context,
                "공개 URL(WEB_SHARE_URL) 설정 후 카카오톡 공유가 가능해요.",
                Toast.LENGTH_LONG,
            ).show()
            return
        }

        val shareBase = BuildConfig.WEB_SHARE_URL.trim().trimEnd('/')
        val imageUrl = "$shareBase/kakao-guest-share.png"
        val description = buildString {
            append(scheduleTitle)
            append("\n")
            append(startDate)
            append(" ~ ")
            append(endDate)
            if (!scheduleDescription.isNullOrBlank()) {
                append("\n")
                append(scheduleDescription)
            }
            append("\n가능한 시간을 등록해주세요")
        }

        // androidExecutionParams를 넣으면 모바일에서 앱/설정(apps.kakao.com)으로 빠짐 → 웹만 사용
        val link = Link(
            webUrl = webLink,
            mobileWebUrl = webLink,
        )

        val feed = FeedTemplate(
            content = Content(
                title = "모임에 초대되었어요!",
                description = description,
                imageUrl = imageUrl,
                link = link,
            ),
            buttons = listOf(
                Button(
                    title = "일정 등록하러 가기",
                    link = link,
                ),
            ),
        )

        if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
            ShareClient.instance.shareDefault(context, feed) { sharingResult, error ->
                if (error != null) {
                    Log.e(TAG, "카카오 Feed 공유 실패: ${error.message}", error)
                    Toast.makeText(
                        context,
                        "Feed 공유 실패. 카카오 콘솔에 WEB_SHARE_URL 도메인 등록을 확인해 주세요.",
                        Toast.LENGTH_LONG,
                    ).show()
                    shareViaWeb(context, feed)
                    return@shareDefault
                }
                sharingResult?.intent?.let { context.startActivity(it) }
            }
        } else {
            shareViaWeb(context, feed)
        }
    }

    private fun shareViaWeb(context: Context, feed: FeedTemplate) {
        val shareUri = WebSharerClient.instance.makeDefaultUrl(feed)
        try {
            KakaoCustomTabsClient.openWithDefault(context, shareUri)
        } catch (e: UnsupportedOperationException) {
            Log.w(TAG, "CustomTabs 미지원, 브라우저로 fallback", e)
            context.startActivity(Intent(Intent.ACTION_VIEW, shareUri))
        } catch (e: Exception) {
            Log.e(TAG, "웹 공유 fallback 실패", e)
            Toast.makeText(context, "카카오 공유를 열 수 없어요.", Toast.LENGTH_SHORT).show()
        }
    }
}
