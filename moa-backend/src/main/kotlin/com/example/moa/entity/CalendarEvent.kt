package com.example.moa.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "calendar_events")
data class CalendarEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: MeetingGroup? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    var schedule: Schedule? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(name = "event_start", nullable = false)
    var eventStart: LocalDateTime = LocalDateTime.now(),

    @Column(name = "event_end", nullable = false)
    var eventEnd: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var color: String = "#2179FE",

    // MANUAL / GROUP / GOOGLE
    @Column(nullable = false)
    var source: String = "MANUAL",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
