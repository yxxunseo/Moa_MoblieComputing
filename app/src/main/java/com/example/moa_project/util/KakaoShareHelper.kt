package com.example.moa_project.util

import android.content.Context
import android.content.Intent
import android.net.Uri
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
 * 단기 일정 링크를 카카오톡 Feed 템플릿으로 공유한다.
 * WEB_SHARE_URL 도메인은 카카오 개발자 콘솔에 등록되어 있어야 한다.
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

        val link = Link(
            webUrl = webLink,
            mobileWebUrl = webLink,
            androidExecutionParams = mapOf("link" to uniqueLink),
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
                    Log.e(TAG, "카카오톡 공유 실패", error)
                    Toast.makeText(context, "카카오톡 공유에 실패했어요.", Toast.LENGTH_SHORT).show()
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
