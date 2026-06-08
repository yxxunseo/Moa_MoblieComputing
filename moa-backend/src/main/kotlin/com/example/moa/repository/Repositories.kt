package com.example.moa.repository

import com.example.moa.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.YearMonth

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun findByLoginId(loginId: String): User?
    fun existsByLoginId(loginId: String): Boolean
    fun findByProviderAndProviderId(provider: String, providerId: String): User?
}

@Repository
interface GroupRepository : JpaRepository<MeetingGroup, Long> {
    fun findByInviteCode(inviteCode: String): MeetingGroup?
}

@Repository
interface GroupMemberRepository : JpaRepository<GroupMember, Long> {
    fun findByGroupAndUser(group: MeetingGroup, user: User): GroupMember?
    fun findAllByUser(user: User): List<GroupMember>
    fun findAllByGroup(group: MeetingGroup): List<GroupMember>
    fun existsByGroupAndUser(group: MeetingGroup, user: User): Boolean
    fun countByGroup(group: MeetingGroup): Long
    fun deleteAllByGroup(group: MeetingGroup)
}

@Repository
interface ScheduleRepository : JpaRepository<Schedule, Long> {
    fun findAllByGroup(group: MeetingGroup): List<Schedule>
    fun deleteAllByGroup(group: MeetingGroup)
}

@Repository
interface TimeSlotRepository : JpaRepository<TimeSlot, Long> {
    fun findAllBySchedule(schedule: Schedule): List<TimeSlot>
    fun findAllByScheduleAndUser(schedule: Schedule, user: User): List<TimeSlot>
    fun deleteAllByScheduleAndUser(schedule: Schedule, user: User)
    fun deleteAllBySchedule(schedule: Schedule)

    /** 일정에 시간을 입력한 고유 사용자 수. 기존엔 전체 슬롯을 메모리에 로드해 distinct 했음. */
    @Query("SELECT COUNT(DISTINCT t.user.id) FROM TimeSlot t WHERE t.schedule = :schedule")
    fun countDistinctRespondedUsers(schedule: Schedule): Long
}

@Repository
interface CalendarEventRepository : JpaRepository<CalendarEvent, Long> {
    fun findAllByUser(user: User): List<CalendarEvent>

    @Query("SELECT e FROM CalendarEvent e WHERE e.user = :user AND YEAR(e.eventStart) = :year AND MONTH(e.eventStart) = :month")
    fun findAllByUserAndYearMonth(user: User, year: Int, month: Int): List<CalendarEvent>
}

@Repository
interface GuestScheduleRepository : JpaRepository<GuestSchedule, Long> {
    fun findByUniqueLink(uniqueLink: String): GuestSchedule?
    fun findAllByCreatedBy(user: User): List<GuestSchedule>
}

@Repository
interface GuestTimeSlotRepository : JpaRepository<GuestTimeSlot, Long> {
    fun findAllByGuestSchedule(guestSchedule: GuestSchedule): List<GuestTimeSlot>
    fun deleteAllByGuestScheduleAndGuestName(guestSchedule: GuestSchedule, guestName: String)
}

@Repository
interface GuestVisitorSessionRepository : JpaRepository<com.example.moa.entity.GuestVisitorSession, Long> {
    fun findByUniqueLinkAndVisitorId(uniqueLink: String, visitorId: String): com.example.moa.entity.GuestVisitorSession?
    fun findFirstByUniqueLinkAndClientIpOrderByUpdatedAtDesc(
        uniqueLink: String,
        clientIp: String,
    ): com.example.moa.entity.GuestVisitorSession?
}

@Repository
interface ScheduleReactionRepository : JpaRepository<ScheduleReaction, Long> {
    fun findAllBySchedule(schedule: Schedule): List<ScheduleReaction>
    fun findByScheduleAndUser(schedule: Schedule, user: User): ScheduleReaction?
    fun deleteAllBySchedule(schedule: Schedule)
}

@Repository
interface FixedTimeSlotRepository : JpaRepository<FixedTimeSlot, Long> {
    fun findAllByUser(user: User): List<FixedTimeSlot>
}
