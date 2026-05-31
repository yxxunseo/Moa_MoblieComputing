package com.example.moa.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "guest_schedules")
data class GuestSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "unique_link", unique = true, nullable = false)
    var uniqueLink: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: User? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate = LocalDate.now(),

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
@Table(name = "guest_time_slots")
data class GuestTimeSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_schedule_id", nullable = false)
    var guestSchedule: GuestSchedule? = null,

    // 비회원도 접근 가능하도록 유저 객체 대신 이름(String)만 저장
    @Column(name = "guest_name", nullable = false)
    var guestName: String = "",

    @Column(name = "slot_start", nullable = false)
    var slotStart: LocalDateTime = LocalDateTime.now(),

    @Column(name = "slot_end", nullable = false)
    var slotEnd: LocalDateTime = LocalDateTime.now()
)
