package com.example.moa.service

import com.example.moa.entity.FixedTimeSlot
import com.example.moa.repository.FixedTimeSlotRepository
import com.example.moa.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class FixedTimeSlotDto(
    val id: Long,
    val dayOfWeek: Int,
    val startHour: Int,
    val endHour: Int,
    val title: String
)

fun FixedTimeSlot.toDto() = FixedTimeSlotDto(
    id = id,
    dayOfWeek = dayOfWeek,
    startHour = startHour,
    endHour = endHour,
    title = title
)

@Service
class FixedTimeSlotService(
    private val fixedTimeSlotRepository: FixedTimeSlotRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getMyFixedSlots(userId: Long): List<FixedTimeSlotDto> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        return fixedTimeSlotRepository.findAllByUser(user).map { it.toDto() }
    }

    @Transactional
    fun addFixedSlot(
        userId: Long,
        dayOfWeek: Int,
        startHour: Int,
        endHour: Int,
        title: String
    ): FixedTimeSlotDto {
        require(dayOfWeek in 1..7) { "요일은 1(월)~7(일) 사이여야 합니다." }
        require(startHour in 0..23 && endHour in 1..24 && endHour > startHour) { "시간 범위가 올바르지 않습니다." }

        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val saved = fixedTimeSlotRepository.save(
            FixedTimeSlot(
                user = user,
                dayOfWeek = dayOfWeek,
                startHour = startHour,
                endHour = endHour,
                title = title.ifBlank { "고정 일정" }
            )
        )
        return saved.toDto()
    }

    @Transactional
    fun deleteFixedSlot(userId: Long, slotId: Long) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val slot = fixedTimeSlotRepository.findById(slotId).orElseThrow { IllegalArgumentException("고정 일정을 찾을 수 없습니다.") }
        if (slot.user?.id != user.id) throw IllegalArgumentException("삭제 권한이 없습니다.")
        fixedTimeSlotRepository.delete(slot)
    }
}
