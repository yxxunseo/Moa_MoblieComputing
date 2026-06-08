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
        val stored = map.optString(key)
        if (stored.isBlank()) return null
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
        targetRoute: String? = null,
    ) {
        runCatching {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val arr = readItemsArray(prefs)
            val resolvedId = id ?: "local-${System.currentTimeMillis()}"
            val item = toJson(
                MoaNotification(
                    id = resolvedId,
                    type = type,
                    title = title,
                    body = body,
                    timestamp = receivedAt,
                    targetRoute = targetRoute,
                ),
            )
            val next = JSONArray()
            for (i in 0 until arr.length()) {
                val existing = runCatching { arr.getJSONObject(i) }.getOrNull() ?: continue
                val normalized = normalizeJsonObject(existing, i)
                if (normalized.optString("id") == resolvedId) continue
                next.put(normalized)
            }
            next.put(0, item)
            while (next.length() > MAX_ITEMS) {
                next.remove(next.length() - 1)
            }
            prefs.edit().putString(KEY_ITEMS, next.toString()).apply()
        }
    }

    fun upsert(context: Context, notification: MoaNotification) {
        add(
            context = context,
            type = notification.type,
            title = notification.title,
            body = notification.body,
            receivedAt = notification.timestamp,
            id = notification.id,
            targetRoute = notification.targetRoute,
        )
    }

    fun loadAll(context: Context): List<MoaNotification> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = normalizeAndPersistIfNeeded(prefs, readItemsArray(prefs))
        return buildList {
            for (i in 0 until arr.length()) {
                val item = runCatching { parseNotification(arr.getJSONObject(i), i) }.getOrNull()
                if (item != null) add(item)
            }
        }
    }

    fun unreadCount(context: Context): Int {
        return runCatching {
            val arr = readItemsArray(
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE),
            )
            var count = 0
            for (i in 0 until arr.length()) {
                val o = runCatching { arr.getJSONObject(i) }.getOrNull() ?: continue
                if (!o.optBoolean("read", false)) count++
            }
            count
        }.getOrDefault(0)
    }

    fun markAllRead(context: Context) {
        runCatching {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val arr = readItemsArray(prefs)
            for (i in 0 until arr.length()) {
                runCatching { arr.getJSONObject(i).put("read", true) }
            }
            prefs.edit().putString(KEY_ITEMS, arr.toString()).apply()
        }
    }

    private fun readItemsArray(prefs: android.content.SharedPreferences): JSONArray {
        val raw = prefs.getString(KEY_ITEMS, "[]") ?: "[]"
        return runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
    }

    private fun parseNotification(o: JSONObject, index: Int): MoaNotification? {
        val normalized = normalizeJsonObject(o, index)
        val id = normalized.optString("id").takeIf { it.isNotBlank() } ?: return null
        val title = normalized.optString("title", "알림")
        val body = normalized.optString("body", "")
        val type = runCatching {
            MoaNotificationType.valueOf(normalized.optString("type", MoaNotificationType.INFO.name))
        }.getOrDefault(MoaNotificationType.INFO)
        val timestamp = runCatching {
            LocalDateTime.parse(normalized.optString("timestamp"), fmt)
        }.getOrDefault(LocalDateTime.now())
        val targetRoute = normalized.optString("targetRoute").takeIf { it.isNotBlank() }
        return MoaNotification(
            id = id,
            type = type,
            title = title,
            body = body,
            timestamp = timestamp,
            targetRoute = targetRoute,
        )
    }

    private fun normalizeJsonObject(o: JSONObject, index: Int): JSONObject {
        val legacyBody = o.optString("body").ifBlank { o.optString("message") }
        val legacyTitle = o.optString("title").ifBlank {
            when (runCatching { MoaNotificationType.valueOf(o.optString("type")) }.getOrNull()) {
                MoaNotificationType.CONFIRMED -> "일정이 확정됐어요"
                MoaNotificationType.WEEKLY_REMINDER -> "일정 등록 안내"
                MoaNotificationType.UPCOMING -> "캘린더에 일정이 추가됐어요"
                else -> legacyBody.take(40).ifBlank { "알림" }
            }
        }
        return JSONObject().apply {
            put(
                "id",
                o.optString("id").ifBlank {
                    "legacy-$index-${o.optString("timestamp", System.currentTimeMillis().toString())}"
                },
            )
            put("type", o.optString("type", MoaNotificationType.INFO.name))
            put("title", legacyTitle)
            put("body", legacyBody)
            put(
                "timestamp",
                o.optString("timestamp").ifBlank { LocalDateTime.now().format(fmt) },
            )
            put("read", o.optBoolean("read", false))
            o.optString("targetRoute").takeIf { it.isNotBlank() }?.let { put("targetRoute", it) }
        }
    }

    private fun toJson(notification: MoaNotification): JSONObject =
        JSONObject().apply {
            put("id", notification.id)
            put("type", notification.type.name)
            put("title", notification.title)
            put("body", notification.body)
            put("timestamp", notification.timestamp.format(fmt))
            put("read", false)
            notification.targetRoute?.let { put("targetRoute", it) }
        }

    private fun normalizeAndPersistIfNeeded(
        prefs: android.content.SharedPreferences,
        rawArr: JSONArray,
    ): JSONArray {
        if (rawArr.length() == 0) return rawArr
        val migrated = JSONArray()
        var changed = false
        for (i in 0 until rawArr.length()) {
            val o = runCatching { rawArr.getJSONObject(i) }.getOrNull()
            if (o == null) {
                changed = true
                continue
            }
            val normalized = normalizeJsonObject(o, i)
            if (!o.has("title") || o.optString("id").isBlank() || normalized.length() != o.length()) {
                changed = true
            }
            migrated.put(normalized)
        }
        if (changed) {
            prefs.edit().putString(KEY_ITEMS, migrated.toString()).apply()
        }
        return if (changed) migrated else rawArr
    }
}
