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
}

@Repository
interface ScheduleRepository : JpaRepository<Schedule, Long> {
    fun findAllByGroup(group: MeetingGroup): List<Schedule>
}

@Repository
interface TimeSlotRepository : JpaRepository<TimeSlot, Long> {
    fun findAllBySchedule(schedule: Schedule): List<TimeSlot>
    fun findAllByScheduleAndUser(schedule: Schedule, user: User): List<TimeSlot>
    fun deleteAllByScheduleAndUser(schedule: Schedule, user: User)
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
interface ScheduleReactionRepository : JpaRepository<ScheduleReaction, Long> {
    fun findAllBySchedule(schedule: Schedule): List<ScheduleReaction>
    fun findByScheduleAndUser(schedule: Schedule, user: User): ScheduleReaction?
}

@Repository
interface FixedTimeSlotRepository : JpaRepository<FixedTimeSlot, Long> {
    fun findAllByUser(user: User): List<FixedTimeSlot>
}
