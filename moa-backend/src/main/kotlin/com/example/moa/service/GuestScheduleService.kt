package com.example.moa.service

import com.example.moa.entity.CalendarEvent
import com.example.moa.entity.GuestSchedule
import com.example.moa.entity.GuestTimeSlot
import com.example.moa.entity.GuestVisitorSession
import com.example.moa.repository.GuestScheduleRepository
import com.example.moa.repository.GuestTimeSlotRepository
import com.example.moa.repository.GuestVisitorSessionRepository
import com.example.moa.repository.UserRepository
import com.example.moa.repository.CalendarEventRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class GuestScheduleResponse(
    val id: Long,
    val uniqueLink: String,
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val status: String,
    val confirmedStart: String? = null,
    val confirmedEnd: String? = null,
    val webLink: String
)

data class GuestTimeSlotDto(val start: String, val end: String)

@Service
class GuestScheduleService(
    private val guestScheduleRepository: GuestScheduleRepository,
    private val guestTimeSlotRepository: GuestTimeSlotRepository,
    private val guestVisitorSessionRepository: GuestVisitorSessionRepository,
    private val userRepository: UserRepository,
    private val calendarEventRepository: CalendarEventRepository,
    @Value("\${server.public-url:http://localhost:8080}") private val publicUrl: String
) {
    private fun buildWebLink(uniqueLink: String): String {
        val base = publicUrl.trimEnd('/')
        return "$base/guest.html?link=$uniqueLink"
    }

    private fun toResponse(schedule: GuestSchedule) = GuestScheduleResponse(
        id = schedule.id,
        uniqueLink = schedule.uniqueLink,
        title = schedule.title,
        description = schedule.description,
        startDate = schedule.startDate.toString(),
        endDate = schedule.endDate.toString(),
        status = schedule.status,
        confirmedStart = schedule.confirmedStart?.toString(),
        confirmedEnd = schedule.confirmedEnd?.toString(),
        webLink = buildWebLink(schedule.uniqueLink)
    )
    // 1. 링크 공유용 일정 만들기 (주최자 전용 - 로그인 필요)
    @Transactional
    fun createGuestSchedule(userId: Long, title: String, description: String?, startDate: LocalDate, endDate: LocalDate): GuestScheduleResponse {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 고유 링크 생성 (UUID 활용)
        val uniqueLink = UUID.randomUUID().toString().substring(0, 8)
        
        val schedule = guestScheduleRepository.save(
            GuestSchedule(
                uniqueLink = uniqueLink,
                createdBy = user,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate
            )
        )
        
        return toResponse(schedule)
    }
    
    // 2. 고유 링크로 일정 정보 가져오기 (비회원 가능)
    @Transactional(readOnly = true)
    fun getScheduleByLink(uniqueLink: String): GuestScheduleResponse {
        val schedule = guestScheduleRepository.findByUniqueLink(uniqueLink) 
            ?: throw IllegalArgumentException("유효하지 않은 링크입니다.")
            
        return toResponse(schedule)
    }
    
    // 3. 이름과 함께 내 가능 시간 입력하기 (비회원 가능)
    private fun saveVisitorSession(
        uniqueLink: String,
        visitorId: String?,
        guestName: String,
        clientIp: String?,
    ) {
        val vid = visitorId?.trim()?.takeIf { it.isNotEmpty() }
            ?: clientIp?.trim()?.takeIf { it.isNotEmpty() }?.let { "ip-$it" }
            ?: return
        val existing = guestVisitorSessionRepository.findByUniqueLinkAndVisitorId(uniqueLink, vid)
        if (existing != null) {
            existing.guestName = guestName
            existing.clientIp = clientIp ?: existing.clientIp
            existing.updatedAt = LocalDateTime.now()
            guestVisitorSessionRepository.save(existing)
        } else {
            guestVisitorSessionRepository.save(
                GuestVisitorSession(
                    uniqueLink = uniqueLink,
                    visitorId = vid,
                    guestName = guestName,
                    clientIp = clientIp,
                ),
            )
        }
    }

    private fun resolveViewerGuestName(
        uniqueLink: String,
        visitorId: String?,
        clientIp: String?,
        participantNames: Set<String>,
    ): Pair<String?, Boolean> {
        val vid = visitorId?.trim()?.takeIf { it.isNotEmpty() }
        var name: String? = null
        if (vid != null) {
            name = guestVisitorSessionRepository.findByUniqueLinkAndVisitorId(uniqueLink, vid)?.guestName
        }
        if (name.isNullOrBlank() && !clientIp.isNullOrBlank()) {
            name = guestVisitorSessionRepository
                .findFirstByUniqueLinkAndClientIpOrderByUpdatedAtDesc(uniqueLink, clientIp)
                ?.guestName
        }
        val hasSubmitted = !name.isNullOrBlank() && participantNames.contains(name)
        return name to hasSubmitted
    }

    @Transactional
    fun addGuestTimeSlots(
        uniqueLink: String,
        guestName: String,
        slots: List<GuestTimeSlotDto>,
        visitorId: String? = null,
        clientIp: String? = null,
    ): Map<String, Any> {
        val schedule = guestScheduleRepository.findByUniqueLink(uniqueLink) 
            ?: throw IllegalArgumentException("유효하지 않은 링크입니다.")
            
        // 동일한 이름으로 등록한 기존 시간이 있다면 덮어쓰기 (초기화 후 재등록)
        guestTimeSlotRepository.deleteAllByGuestScheduleAndGuestName(schedule, guestName)
        
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        
        slots.forEach { slot ->
            guestTimeSlotRepository.save(
                GuestTimeSlot(
                    guestSchedule = schedule,
                    guestName = guestName,
                    slotStart = LocalDateTime.parse(slot.start, formatter),
                    slotEnd = LocalDateTime.parse(slot.end, formatter)
                )
            )
        }

        saveVisitorSession(uniqueLink, visitorId, guestName, clientIp)
        
        return mapOf("message" to "${guestName}님의 가능 시간이 성공적으로 등록되었습니다.")
    }
    
    // 4. 익명 일정 겹치는 시간 히트맵 분석 (비회원 가능)
    @Transactional(readOnly = true)
    fun analyzeGuestSchedule(
        uniqueLink: String,
        visitorId: String? = null,
        clientIp: String? = null,
    ): Map<String, Any> {
        val schedule = guestScheduleRepository.findByUniqueLink(uniqueLink) 
            ?: throw IllegalArgumentException("유효하지 않은 링크입니다.")
            
        val allSlots = guestTimeSlotRepository.findAllByGuestSchedule(schedule)
        
        val heatmap = mutableMapOf<String, MutableMap<String, Int>>()
        val heatmapMembers = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
        val userAvailability = mutableMapOf<LocalDateTime, MutableList<String>>()
        val participantSlots = mutableMapOf<String, MutableList<Map<String, String>>>()
        
        allSlots.forEach { slot ->
            participantSlots.putIfAbsent(slot.guestName, mutableListOf())
            participantSlots[slot.guestName]!!.add(
                mapOf("start" to slot.slotStart.toString(), "end" to slot.slotEnd.toString())
            )
            var current = slot.slotStart
            while (current.isBefore(slot.slotEnd)) {
                val dateStr = current.toLocalDate().toString()
                val timeStr = String.format("%02d:00", current.hour)
                
                heatmap.putIfAbsent(dateStr, mutableMapOf())
                heatmap[dateStr]!![timeStr] = heatmap[dateStr]!!.getOrDefault(timeStr, 0) + 1

                heatmapMembers.putIfAbsent(dateStr, mutableMapOf())
                heatmapMembers[dateStr]!!.putIfAbsent(timeStr, mutableListOf())
                if (!heatmapMembers[dateStr]!![timeStr]!!.contains(slot.guestName)) {
                    heatmapMembers[dateStr]!![timeStr]!!.add(slot.guestName)
                }
                
                userAvailability.putIfAbsent(current, mutableListOf())
                userAvailability[current]!!.add(slot.guestName)
                
                current = current.plusHours(1)
            }
        }
        
        val totalParticipants = allSlots.map { it.guestName }.distinct().size

        val participants = participantSlots.entries.map { (name, slots) ->
            mapOf(
                "name" to name,
                "slotCount" to slots.size,
                "slots" to slots
            )
        }.sortedByDescending { (it["slotCount"] as Int) }
        
        val recommendations = userAvailability.entries
            .sortedWith(
                compareByDescending<Map.Entry<LocalDateTime, MutableList<String>>> { it.value.size }
                    .thenBy { it.key }
            )
            .take(3)
            .mapIndexed { index, entry ->
                RecommendationDto(
                    rank = index + 1,
                    start = entry.key.toString(),
                    end = entry.key.plusHours(1).toString(),
                    availableCount = entry.value.size,
                    availableMembers = entry.value.distinct()
                )
            }

        val participantNames = participants.mapNotNull { it["name"] as? String }.toSet()
        val (viewerName, hasSubmitted) = resolveViewerGuestName(
            uniqueLink,
            visitorId,
            clientIp,
            participantNames,
        )
            
        return mapOf<String, Any>(
            "scheduleId" to schedule.id,
            "title" to schedule.title,
            "uniqueLink" to schedule.uniqueLink,
            "status" to schedule.status,
            "confirmedStart" to (schedule.confirmedStart?.toString() ?: ""),
            "confirmedEnd" to (schedule.confirmedEnd?.toString() ?: ""),
            "totalParticipants" to totalParticipants,
            "recommendations" to recommendations,
            "heatmap" to heatmap,
            "heatmapMembers" to heatmapMembers,
            "participants" to participants,
            "viewer" to mapOf<String, Any>(
                "guestName" to (viewerName ?: ""),
                "hasSubmitted" to hasSubmitted,
            ),
        )
    }

    @Transactional
    fun confirmGuestSchedule(userId: Long, uniqueLink: String, start: String, end: String): Map<String, Any> {
        val schedule = guestScheduleRepository.findByUniqueLink(uniqueLink)
            ?: throw IllegalArgumentException("유효하지 않은 링크입니다.")

        if (schedule.createdBy?.id != userId) {
            throw IllegalArgumentException("일정 확정 권한이 없습니다.")
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        schedule.confirmedStart = LocalDateTime.parse(start, formatter)
        schedule.confirmedEnd = LocalDateTime.parse(end, formatter)
        schedule.status = "CONFIRMED"

        calendarEventRepository.save(
            CalendarEvent(
                user = schedule.createdBy,
                title = schedule.title,
                eventStart = schedule.confirmedStart!!,
                eventEnd = schedule.confirmedEnd!!,
                color = "#2179FE",
                source = "MANUAL"
            )
        )

        return mapOf(
            "message" to "일정이 확정되었습니다!",
            "status" to "CONFIRMED",
            "calendarEventCreated" to true
        )
    }

    @Transactional(readOnly = true)
    fun getMyGuestSchedules(userId: Long): List<GuestScheduleResponse> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        return guestScheduleRepository.findAllByCreatedBy(user)
            .sortedByDescending { it.createdAt }
            .map { toResponse(it) }
    }

    @Transactional
    fun completeGuestSchedule(userId: Long, uniqueLink: String): Map<String, Any> {
        val schedule = guestScheduleRepository.findByUniqueLink(uniqueLink)
            ?: throw IllegalArgumentException("유효하지 않은 링크입니다.")

        if (schedule.createdBy?.id != userId) {
            throw IllegalArgumentException("완료 처리 권한이 없습니다.")
        }

        if (schedule.status != "CONFIRMED") {
            throw IllegalArgumentException("확정된 일정만 완료 처리할 수 있습니다.")
        }

        schedule.status = "DONE"
        return mapOf(
            "message" to "일정이 완료 처리되었습니다.",
            "status" to "DONE"
        )
    }
}
