package com.example.moa.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "schedule_reactions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["schedule_id", "user_id"])]
)
data class ScheduleReaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    var schedule: Schedule? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Column(nullable = false, length = 16)
    var emoji: String = ""
)
