package com.habit.mychemistpos.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ─── Formatters ─────────────────────────────────────────────────────────────────
private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
private val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

/** "14 May 2026" */
fun LocalDate.formatDate(): String = this.format(dateFormatter)

/** "14/05/2026" */
fun LocalDate.formatShortDate(): String = this.format(shortDateFormatter)

/** "14 May 2026, 08:04 AM" */
fun LocalDateTime.formatDateTime(): String = this.format(dateTimeFormatter)

/** "08:04 AM" */
fun LocalDateTime.formatTime(): String = this.format(timeFormatter)

/** "May 2026" */
fun LocalDate.formatMonthYear(): String = this.format(monthYearFormatter)

/**
 * Returns a human-readable relative string for expiry dates.
 *
 * Examples:
 *   "Expired 3 days ago"
 *   "Expires today"
 *   "Expires in 5 days"
 *   "Expires in 2 months"
 */
fun LocalDate.expiryLabel(): String {
    val today = LocalDate.now()
    val daysUntil = ChronoUnit.DAYS.between(today, this)

    return when {
        daysUntil < 0  -> "Expired ${-daysUntil} day${if (-daysUntil != 1L) "s" else ""} ago"
        daysUntil == 0L -> "Expires today"
        daysUntil <= 30 -> "Expires in $daysUntil day${if (daysUntil != 1L) "s" else ""}"
        else -> {
            val months = ChronoUnit.MONTHS.between(today, this)
            "Expires in $months month${if (months != 1L) "s" else ""}"
        }
    }
}

/**
 * Checks if a date is within a given warning window from today.
 */
fun LocalDate.isExpiringSoon(warningDays: Int = 30): Boolean {
    val today = LocalDate.now()
    return !this.isBefore(today) && ChronoUnit.DAYS.between(today, this) <= warningDays
}

/**
 * Checks if a date has already passed.
 */
fun LocalDate.isExpired(): Boolean = this.isBefore(LocalDate.now())

/**
 * Returns "Today", "Yesterday", or the formatted date.
 */
fun LocalDate.relativeLabel(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> this.formatDate()
    }
}
