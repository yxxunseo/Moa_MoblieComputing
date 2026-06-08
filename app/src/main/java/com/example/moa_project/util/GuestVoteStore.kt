package com.example.moa_project.util

import android.content.Context
import com.example.moa_project.network.GuestParticipantDto
import com.example.moa_project.network.TimeSlotDto
import com.example.moa_project.ui.schedule.TimeSlot
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** 단기 링크 일정에서 게스트 이름·이전 투표 복원 */
object GuestVoteStore {
    private const val PREFS = "moa_guest_votes"
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    fun saveGuestName(context: Context, link: String, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(nameKey(link), name.trim())
            .apply()
    }

    fun getGuestName(context: Context, link: String): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(nameKey(link), null)
            ?.takeIf { it.isNotBlank() }

    fun parseSlots(slots: List<TimeSlotDto>?): List<TimeSlot> =
        slots.orEmpty().mapNotNull { dto ->
            runCatching {
                val start = LocalDateTime.parse(dto.start, formatter)
                TimeSlot(date = start.toLocalDate(), hour = start.hour)
            }.getOrNull()
        }

    fun findParticipantSlots(
        participants: List<GuestParticipantDto>?,
        guestName: String,
    ): List<TimeSlot> {
        val name = guestName.trim()
        if (name.isBlank()) return emptyList()
        return participants
            ?.firstOrNull { it.name.trim() == name }
            ?.let { parseSlots(it.slots) }
            .orEmpty()
    }

    private fun nameKey(link: String) = "guest_name_$link"
}
