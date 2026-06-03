package com.example.moa_project.util

import com.example.moa_project.network.GuestParticipantDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object HeatmapMembersResolver {

    fun resolveAt(
        date: String,
        hour: String,
        heatmapMembers: Map<String, Map<String, List<String>>>?,
        participants: List<GuestParticipantDto>?,
    ): List<String> {
        membersFromMap(date, hour, heatmapMembers)?.takeIf { it.isNotEmpty() }?.let { return it }
        return membersFromParticipants(date, hour, participants)
    }

    fun rebuildFromParticipants(
        participants: List<GuestParticipantDto>?,
    ): Map<String, Map<String, List<String>>> {
        val result = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
        participants.orEmpty().forEach { participant ->
            participant.slots.orEmpty().forEach { slot ->
                val start = parseDateTime(slot.start) ?: return@forEach
                val end = parseDateTime(slot.end) ?: return@forEach
                var current = start
                while (current.isBefore(end)) {
                    val dateStr = current.toLocalDate().toString()
                    val timeStr = normalizeHour("${current.hour}:00")
                    val day = result.getOrPut(dateStr) { mutableMapOf() }
                    val list = day.getOrPut(timeStr) { mutableListOf() }
                    if (!list.contains(participant.name)) {
                        list.add(participant.name)
                    }
                    current = current.plusHours(1)
                }
            }
        }
        return result
    }

    private fun membersFromMap(
        date: String,
        hour: String,
        heatmapMembers: Map<String, Map<String, List<String>>>?,
    ): List<String>? {
        val dayMap = heatmapMembers?.get(date) ?: return null
        for (key in listOf(hour, normalizeHour(hour))) {
            dayMap[key]?.let { return coerceNames(it) }
        }
        dayMap.forEach { (key, names) ->
            if (normalizeHour(key) == normalizeHour(hour)) {
                return coerceNames(names)
            }
        }
        return null
    }

    private fun membersFromParticipants(
        date: String,
        hour: String,
        participants: List<GuestParticipantDto>?,
    ): List<String> {
        val targetHour = normalizeHour(hour)
        val names = linkedSetOf<String>()
        participants.orEmpty().forEach { participant ->
            participant.slots.orEmpty().forEach { slot ->
                val start = parseDateTime(slot.start) ?: return@forEach
                val end = parseDateTime(slot.end) ?: return@forEach
                var current = start
                while (current.isBefore(end)) {
                    if (current.toLocalDate().toString() == date &&
                        normalizeHour("${current.hour}:00") == targetHour
                    ) {
                        names.add(participant.name)
                    }
                    current = current.plusHours(1)
                }
            }
        }
        return names.toList()
    }

    private fun coerceNames(raw: List<String>): List<String> =
        raw.mapNotNull { it?.toString()?.trim()?.takeIf { name -> name.isNotEmpty() } }

    private fun normalizeHour(hour: String): String {
        val hourPart = hour.substringBefore(":").trim()
        val n = hourPart.toIntOrNull() ?: return hour
        return "%02d:00".format(n)
    }

    private fun parseDateTime(text: String): LocalDateTime? {
        val trimmed = text.trim()
        return runCatching { LocalDateTime.parse(trimmed) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(trimmed.substringBefore('.').substringBefore('Z'))
            }.getOrNull()
    }
}
