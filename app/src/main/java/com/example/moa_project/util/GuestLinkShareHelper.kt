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

    fun buildConfirmedShareText(
        scheduleTitle: String,
        confirmedStart: String?,
        confirmedEnd: String?,
    ): String {
        val dateStr = confirmedStart?.split("T")?.firstOrNull()?.let { d ->
            val parts = d.split("-")
            if (parts.size == 3) "${parts[1].toInt()}월 ${parts[2].toInt()}일" else d
        } ?: "-"
        val startTime = confirmedStart?.split("T")?.getOrNull(1)?.take(5) ?: ""
        val endTime = confirmedEnd?.split("T")?.getOrNull(1)?.take(5) ?: ""
        val timeStr = if (startTime.isNotBlank() && endTime.isNotBlank()) {
            "$startTime ~ $endTime"
        } else {
            startTime.ifBlank { endTime }.ifBlank { "-" }
        }
        return "$scheduleTitle 일정이 $dateStr ${timeStr}에 확정되었습니다"
    }

    fun copyConfirmedShareText(
        context: Context,
        scheduleTitle: String,
        confirmedStart: String?,
        confirmedEnd: String?,
        webLink: String? = null,
    ) {
        val shareText = buildConfirmedShareTextWithLink(
            scheduleTitle = scheduleTitle,
            confirmedStart = confirmedStart,
            confirmedEnd = confirmedEnd,
            webLink = webLink,
        )
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Moa confirmed schedule", shareText))
        Toast.makeText(context, "확정 일정 문구를 복사했어요.", Toast.LENGTH_SHORT).show()
    }

    fun shareConfirmedSchedule(
        context: Context,
        scheduleTitle: String,
        confirmedStart: String?,
        confirmedEnd: String?,
        webLink: String? = null,
    ) {
        val shareText = buildConfirmedShareTextWithLink(
            scheduleTitle = scheduleTitle,
            confirmedStart = confirmedStart,
            confirmedEnd = confirmedEnd,
            webLink = webLink,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$scheduleTitle 일정 확정")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "공유하기"))
    }

    private fun buildConfirmedShareTextWithLink(
        scheduleTitle: String,
        confirmedStart: String?,
        confirmedEnd: String?,
        webLink: String?,
    ): String = buildString {
        append(buildConfirmedShareText(scheduleTitle, confirmedStart, confirmedEnd))
        if (!webLink.isNullOrBlank()) {
            append("\n")
            append(webLink)
        }
    }
}
