package com.example.moa_project.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

object GroupInviteShareHelper {

    fun copyInviteLink(
        context: Context,
        inviteCode: String,
        inviterName: String? = null,
    ) {
        val webLink = GroupInviteLinkHelper.resolveJoinWebLink(inviteCode, inviterName)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (!webLink.isNullOrBlank()) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Moa Invite Link", webLink))
            Toast.makeText(context, "초대 링크를 복사했어요.", Toast.LENGTH_SHORT).show()
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("Moa Invite Code", inviteCode))
            Toast.makeText(context, "초대 코드를 복사했어요.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openShareChooser(
        context: Context,
        groupName: String,
        groupDescription: String?,
        inviteCode: String,
        inviterName: String? = null,
    ) {
        val shareText = GroupInviteLinkHelper.buildInviteMessage(
            groupName = groupName,
            groupDescription = groupDescription,
            inviteCode = inviteCode,
            inviterName = inviterName,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$groupName 모임 초대")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "공유하기"))
    }

    fun share(
        context: Context,
        groupName: String,
        groupDescription: String?,
        inviteCode: String,
        inviterName: String? = null,
    ) {
        copyInviteLink(context, inviteCode, inviterName)
        openShareChooser(context, groupName, groupDescription, inviteCode, inviterName)
    }
}
