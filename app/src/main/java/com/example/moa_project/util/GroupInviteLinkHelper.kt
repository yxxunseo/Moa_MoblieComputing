package com.example.moa_project.util

import com.example.moa_project.BuildConfig
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object GroupInviteLinkHelper {

    fun resolveJoinWebLink(
        inviteCode: String,
        inviterName: String? = null,
    ): String? {
        val appShareBase = BuildConfig.WEB_SHARE_URL.trim().trimEnd('/')
        if (appShareBase.isNotBlank() && GuestLinkHelper.isExternalReachable(appShareBase)) {
            return buildJoinUrl(appShareBase, inviteCode, inviterName)
        }

        val serverBase = BuildConfig.SERVER_URL.trim().trimEnd('/')
        if (serverBase.isNotBlank() && GuestLinkHelper.isExternalReachable(serverBase)) {
            return buildJoinUrl(serverBase, inviteCode, inviterName)
        }

        return null
    }

    fun isExternalShareReady(): Boolean {
        val appShareBase = BuildConfig.WEB_SHARE_URL.trim().trimEnd('/')
        if (appShareBase.isNotBlank() && GuestLinkHelper.isExternalReachable(appShareBase)) {
            return true
        }
        val serverBase = BuildConfig.SERVER_URL.trim().trimEnd('/')
        return serverBase.isNotBlank() && GuestLinkHelper.isExternalReachable(serverBase)
    }

    fun buildInviteMessage(
        groupName: String,
        groupDescription: String?,
        inviteCode: String,
        inviterName: String? = null,
        webLink: String? = resolveJoinWebLink(inviteCode, inviterName),
    ): String = buildString {
        append("[MOA] ")
        append(groupName)
        append(" 모임에 초대해요!")
        append("\n\n")
        append("초대 코드: ")
        append(inviteCode)
        if (!groupDescription.isNullOrBlank()) {
            append("\n")
            append(groupDescription)
        }
        append("\n\n")
        append("MOA 앱 → 모임 → 코드로 입장에서 초대 코드를 입력해 주세요.")
        if (!webLink.isNullOrBlank()) {
            append("\n")
            append(webLink)
        }
    }

    private fun buildJoinUrl(
        base: String,
        inviteCode: String,
        inviterName: String? = null,
    ): String {
        val encoded = URLEncoder.encode(inviteCode.trim(), StandardCharsets.UTF_8.toString())
        val fromParam = inviterName?.trim()?.take(20)?.takeIf { it.isNotBlank() }?.let {
            "&from=" + URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
        }.orEmpty()
        return "$base/join.html?code=$encoded$fromParam"
    }
}
