package com.example.moa.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "schedules")
data class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    var group: MeetingGroup? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate = LocalDate.now(),

    // WAITING / ADJUSTING / CONFIRMED / DONE
    @Column(nullable = false)
    var status: String = "WAITING",

    @Column(name = "confirmed_start")
    var confirmedStart: LocalDateTime? = null,

    @Column(name = "confirmed_end")
    var confirmedEnd: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "time_slots")
data class TimeSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    var schedule: Schedule? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Column(name = "slot_start", nullable = false)
    var slotStart: LocalDateTime = LocalDateTime.now(),

    @Column(name = "slot_end", nullable = false)
    var slotEnd: LocalDateTime = LocalDateTime.now()
)
