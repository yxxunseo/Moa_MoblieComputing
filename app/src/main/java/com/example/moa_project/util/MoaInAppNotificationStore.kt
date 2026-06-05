package com.example.moa_project.util

import android.content.Context
import com.example.moa_project.ui.notifications.MoaNotification
import com.example.moa_project.ui.notifications.MoaNotificationType
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** 앱 내 알림함에 표시할 알림을 로컬에 저장 */
object MoaInAppNotificationStore {
    private const val PREFS = "moa_in_app_notifications"
    private const val KEY_ITEMS = "items"
    private const val MAX_ITEMS = 50
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun add(
        context: Context,
        type: MoaNotificationType,
        title: String,
        body: String,
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY_ITEMS, "[]"))
        val item = JSONObject().apply {
            put("id", "local-${System.currentTimeMillis()}")
            put("type", type.name)
            put("title", title)
            put("body", body)
            put("timestamp", LocalDateTime.now().format(fmt))
            put("read", false)
        }
        val next = JSONArray().put(item)
        for (i in 0 until minOf(arr.length(), MAX_ITEMS - 1)) {
            next.put(arr.getJSONObject(i))
        }
        prefs.edit().putString(KEY_ITEMS, next.toString()).apply()
    }

    fun loadAll(context: Context): List<MoaNotification> {
        val arr = JSONArray(
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_ITEMS, "[]")
        )
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    MoaNotification(
                        id = o.getString("id"),
                        type = runCatching {
                            MoaNotificationType.valueOf(o.getString("type"))
                        }.getOrDefault(MoaNotificationType.INFO),
                        title = o.getString("title"),
                        body = o.getString("body"),
                        timestamp = runCatching {
                            LocalDateTime.parse(o.getString("timestamp"), fmt)
                        }.getOrDefault(LocalDateTime.now()),
                    )
                )
            }
        }
    }

    fun unreadCount(context: Context): Int {
        val arr = JSONArray(
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_ITEMS, "[]")
        )
        var count = 0
        for (i in 0 until arr.length()) {
            if (!arr.getJSONObject(i).optBoolean("read", false)) count++
        }
        return count
    }

    fun markAllRead(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY_ITEMS, "[]"))
        for (i in 0 until arr.length()) {
            arr.getJSONObject(i).put("read", true)
        }
        prefs.edit().putString(KEY_ITEMS, arr.toString()).apply()
    }
}
