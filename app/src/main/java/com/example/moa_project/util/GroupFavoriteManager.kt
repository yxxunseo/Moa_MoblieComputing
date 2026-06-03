package com.example.moa_project.util

import android.content.Context
import com.example.moa_project.network.TokenManager

/**
 * 관심 모임 목록을 사용자별로 분리해서 저장.
 * SharedPreferences 키에 userId를 포함해 계정 간 데이터 혼용을 방지.
 */
object GroupFavoriteManager {
    private const val PREFS = "moa_group_favorites"

    /** 현재 로그인한 userId 기반 키. 로그아웃 상태면 "anonymous" 사용. */
    private fun idsKey(): String {
        val uid = TokenManager.getUserId()
        return if (uid >= 0L) "ids_$uid" else "ids_anonymous"
    }

    fun isFavorite(context: Context, groupId: Long): Boolean =
        prefs(context).getStringSet(idsKey(), emptySet())
            ?.contains(groupId.toString()) == true

    fun toggleFavorite(context: Context, groupId: Long): Boolean {
        val key = idsKey()
        val set = prefs(context).getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        val id = groupId.toString()
        val nowFavorite = if (set.contains(id)) {
            set.remove(id); false
        } else {
            set.add(id); true
        }
        prefs(context).edit().putStringSet(key, set).apply()
        return nowFavorite
    }

    fun favoriteIds(context: Context): Set<Long> =
        prefs(context).getStringSet(idsKey(), emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet()
            ?: emptySet()

    fun favoriteCount(context: Context): Int = favoriteIds(context).size

    /** 로그아웃/계정 전환 시 전체 즐겨찾기 데이터를 삭제 */
    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
