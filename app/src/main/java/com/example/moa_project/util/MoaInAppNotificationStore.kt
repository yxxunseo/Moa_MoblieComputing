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
    private const val KEY_RECEIVED_AT = "received_at"
    private const val MAX_ITEMS = 50
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun recordReceivedAt(
        context: Context,
        key: String,
        at: LocalDateTime = LocalDateTime.now(),
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val map = JSONObject(prefs.getString(KEY_RECEIVED_AT, "{}") ?: "{}")
        if (!map.has(key)) {
            map.put(key, at.format(fmt))
            prefs.edit().putString(KEY_RECEIVED_AT, map.toString()).apply()
        }
    }

    fun getReceivedAt(context: Context, key: String): LocalDateTime? {
        val map = JSONObject(
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_RECEIVED_AT, "{}") ?: "{}",
        )
        if (!map.has(key)) return null
        val stored = map.getString(key)
        return runCatching { LocalDateTime.parse(stored, fmt) }.getOrNull()
    }

    fun getOrRecordReceivedAt(context: Context, key: String): LocalDateTime {
        return getReceivedAt(context, key) ?: run {
            val now = LocalDateTime.now()
            recordReceivedAt(context, key, now)
            now
        }
    }

    fun add(
        context: Context,
        type: MoaNotificationType,
        title: String,
        body: String,
        receivedAt: LocalDateTime = LocalDateTime.now(),
        id: String? = null,
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY_ITEMS, "[]"))
        val resolvedId = id ?: "local-${System.currentTimeMillis()}"
        val item = JSONObject().apply {
            put("id", resolvedId)
            put("type", type.name)
            put("title", title)
            put("body", body)
            put("timestamp", receivedAt.format(fmt))
            put("read", false)
        }
        val next = JSONArray()
        for (i in 0 until arr.length()) {
            val existing = arr.getJSONObject(i)
            if (existing.getString("id") == resolvedId) continue
            next.put(existing)
        }
        next.put(0, item)
        while (next.length() > MAX_ITEMS) {
            next.remove(next.length() - 1)
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
            val o = arr.getJSONObject(i)
            val type = runCatching {
                MoaNotificationType.valueOf(o.getString("type"))
            }.getOrDefault(MoaNotificationType.INFO)
            if (type != MoaNotificationType.CONFIRMED) continue
            if (!o.optBoolean("read", false)) count++
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
