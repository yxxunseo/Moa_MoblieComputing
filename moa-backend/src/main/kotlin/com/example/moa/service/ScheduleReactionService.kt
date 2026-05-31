package com.example.moa.service

import com.example.moa.entity.Schedule
import com.example.moa.entity.ScheduleReaction
import com.example.moa.entity.User
import com.example.moa.repository.GroupMemberRepository
import com.example.moa.repository.ScheduleReactionRepository
import com.example.moa.repository.ScheduleRepository
import com.example.moa.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ReactionDto(
    val emoji: String,
    val nickname: String,
    val userId: Long
)

@Service
class ScheduleReactionService(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleReactionRepository: ScheduleReactionRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getReactions(userId: Long, scheduleId: Long): List<ReactionDto> {
        val schedule = findAccessibleSchedule(userId, scheduleId)
        return scheduleReactionRepository.findAllBySchedule(schedule).map {
            ReactionDto(
                emoji = it.emoji,
                nickname = it.user?.nickname ?: "알 수 없음",
                userId = it.user?.id ?: 0L
            )
        }
    }

    @Transactional
    fun upsertReaction(userId: Long, scheduleId: Long, emoji: String): ReactionDto {
        val schedule = findAccessibleSchedule(userId, scheduleId)
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val existing = scheduleReactionRepository.findByScheduleAndUser(schedule, user)
        val saved = if (existing != null) {
            existing.emoji = emoji
            scheduleReactionRepository.save(existing)
        } else {
            scheduleReactionRepository.save(
                ScheduleReaction(schedule = schedule, user = user, emoji = emoji)
            )
        }

        return ReactionDto(emoji = saved.emoji, nickname = user.nickname, userId = user.id)
    }

    @Transactional
    fun deleteReaction(userId: Long, scheduleId: Long) {
        val schedule = findAccessibleSchedule(userId, scheduleId)
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        scheduleReactionRepository.findByScheduleAndUser(schedule, user)?.let {
            scheduleReactionRepository.delete(it)
        }
    }

    private fun findAccessibleSchedule(userId: Long, scheduleId: Long): Schedule {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val group = schedule.group ?: throw IllegalArgumentException("그룹 정보가 없습니다.")
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("접근 권한이 없습니다.")
        }
        return schedule
    }
}
