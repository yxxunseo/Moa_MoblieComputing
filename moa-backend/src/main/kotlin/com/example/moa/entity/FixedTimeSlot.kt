package com.example.moa.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "fixed_time_slots")
data class FixedTimeSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    /** 1=월 … 7=일 */
    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: Int = 1,

    @Column(name = "start_hour", nullable = false)
    var startHour: Int = 9,

    @Column(name = "end_hour", nullable = false)
    var endHour: Int = 18,

    @Column(nullable = false)
    var title: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
