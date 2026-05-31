package com.example.moa.service

import com.example.moa.entity.CalendarEvent
import com.example.moa.repository.CalendarEventRepository
import com.example.moa.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

data class CalendarEventDto(
    val id: Long,
    val title: String,
    val start: String,
    val end: String,
    val color: String,
    val source: String,
    val groupName: String?
)

fun CalendarEvent.toDto() = CalendarEventDto(
    id = id,
    title = title,
    start = eventStart.toString(),
    end = eventEnd.toString(),
    color = color,
    source = source,
    groupName = group?.name
)

@Service
class CalendarService(
    private val calendarEventRepository: CalendarEventRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getMonthlyEvents(userId: Long, year: Int, month: Int): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 특정 연/월의 일정만 조회
        val events = calendarEventRepository.findAllByUserAndYearMonth(user, year, month)
        
        return mapOf(
            "month" to String.format("%04d-%02d", year, month),
            "events" to events.map { it.toDto() }
        )
    }

    @Transactional
    fun addManualEvent(userId: Long, title: String, start: String, end: String, color: String): CalendarEventDto {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        val event = calendarEventRepository.save(
            CalendarEvent(
                user = user,
                title = title,
                eventStart = LocalDateTime.parse(start), // 형식: yyyy-MM-ddTHH:mm:ss
                eventEnd = LocalDateTime.parse(end),
                color = color,
                source = "MANUAL"
            )
        )
        return event.toDto()
    }
    
    @Transactional
    fun deleteEvent(userId: Long, eventId: Long) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val event = calendarEventRepository.findById(eventId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        
        if (event.user?.id != user.id) {
            throw IllegalArgumentException("삭제 권한이 없습니다.")
        }
        
        calendarEventRepository.delete(event)
    }

    @Transactional
    fun updateEvent(
        userId: Long,
        eventId: Long,
        title: String,
        start: String,
        end: String,
        color: String
    ): CalendarEventDto {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val event = calendarEventRepository.findById(eventId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }

        if (event.user?.id != user.id) {
            throw IllegalArgumentException("수정 권한이 없습니다.")
        }
        if (event.source != "MANUAL") {
            throw IllegalArgumentException("수동 등록 일정만 수정할 수 있습니다.")
        }

        event.title = title
        event.eventStart = LocalDateTime.parse(start)
        event.eventEnd = LocalDateTime.parse(end)
        event.color = color
        return calendarEventRepository.save(event).toDto()
    }
}
