package com.example.moa_project.util

import android.content.Context

object GroupFavoriteManager {
    private const val PREFS = "moa_group_favorites"

    fun isFavorite(context: Context, groupId: Long): Boolean {
        return prefs(context).getStringSet("ids", emptySet())?.contains(groupId.toString()) == true
    }

    fun toggleFavorite(context: Context, groupId: Long): Boolean {
        val set = prefs(context).getStringSet("ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        val key = groupId.toString()
        val nowFavorite = if (set.contains(key)) {
            set.remove(key)
            false
        } else {
            set.add(key)
            true
        }
        prefs(context).edit().putStringSet("ids", set).apply()
        return nowFavorite
    }

    fun favoriteIds(context: Context): Set<Long> {
        return prefs(context).getStringSet("ids", emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun favoriteCount(context: Context): Int = favoriteIds(context).size

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
