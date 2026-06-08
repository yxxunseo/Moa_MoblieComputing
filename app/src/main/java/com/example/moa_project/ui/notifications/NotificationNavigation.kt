package com.example.moa_project.ui.notifications

fun MoaNotification.navigationRoute(): String? {
    val scheduleId = id.extractScheduleId()

    val canonical = when (type) {
        MoaNotificationType.CONFIRMED -> when {
            id.startsWith("guest-") -> targetRoute?.takeIf { it.startsWith("schedule_result/") }
            scheduleId != null -> "schedule_result_group/$scheduleId"
            else -> null
        }
        MoaNotificationType.WAITING,
        MoaNotificationType.ADJUSTING,
        MoaNotificationType.WEEKLY_REMINDER -> scheduleId?.let { "schedule_coordination_group/$it" }
        MoaNotificationType.UPCOMING -> scheduleId?.let { "schedule_result_group/$it" } ?: "calendar"
        MoaNotificationType.INFO -> null
    }

    if (canonical != null) return canonical

    return targetRoute?.takeIf { route ->
        route.isNotBlank() && route != "calendar"
    }
}

fun MoaNotification.isNavigable(): Boolean =
    navigationRoute() != null || id.startsWith("guest-")

private fun String.extractScheduleId(): String? = when {
    startsWith("sch-") -> removePrefix("sch-")
    startsWith("pending-") -> removePrefix("pending-")
    startsWith("weekly-") -> removePrefix("weekly-")
    startsWith("weekly_") -> removePrefix("weekly_")
    else -> null
}?.takeIf { it.all(Char::isDigit) }
