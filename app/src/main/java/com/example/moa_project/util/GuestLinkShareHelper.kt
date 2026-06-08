package com.example.moa_project.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

object GuestLinkShareHelper {

    fun copyWebLink(context: Context, webLink: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Moa schedule link", webLink))
        Toast.makeText(context, "링크를 복사했어요.", Toast.LENGTH_SHORT).show()
    }

    fun openShareChooser(
        context: Context,
        scheduleTitle: String,
        scheduleDescription: String?,
        startDate: String,
        endDate: String,
        webLink: String,
    ) {
        val shareText = buildString {
            append(scheduleTitle)
            append("\n")
            append(startDate)
            append(" ~ ")
            append(endDate)
            if (!scheduleDescription.isNullOrBlank()) {
                append("\n")
                append(scheduleDescription)
            }
            append("\n")
            append(webLink)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, scheduleTitle)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "공유하기"))
    }

    fun share(
        context: Context,
        scheduleTitle: String,
        scheduleDescription: String?,
        startDate: String,
        endDate: String,
        uniqueLink: String,
        webLink: String,
    ) {
        copyWebLink(context, webLink)
        openShareChooser(context, scheduleTitle, scheduleDescription, startDate, endDate, webLink)
    }
}
