package com.example.moa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "guest_visitor_sessions",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["unique_link", "visitor_id"]),
    ],
)
data class GuestVisitorSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "unique_link", nullable = false)
    var uniqueLink: String = "",

    @Column(name = "visitor_id", nullable = false)
    var visitorId: String = "",

    @Column(name = "guest_name", nullable = false)
    var guestName: String = "",

    @Column(name = "client_ip")
    var clientIp: String? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
