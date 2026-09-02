package com.habit.mychemistpos.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a numeric amount as Kenyan Shillings.
 *
 * Examples:
 *   formatCurrency(1500.0)   → "KES 1,500.00"
 *   formatCurrency(0.0)      → "KES 0.00"
 *   formatCurrency(99999.5)  → "KES 99,999.50"
 */
fun formatCurrency(amount: Double): String {
    val locale = Locale.Builder().setLanguage("en").setRegion("KE").build()
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "KES ${formatter.format(amount)}"
}

/**
 * Formats a numeric amount as a compact currency string (no decimals).
 *
 * Examples:
 *   formatCurrencyCompact(1500.0) → "KES 1,500"
 */
fun formatCurrencyCompact(amount: Double): String {
    val locale = Locale.Builder().setLanguage("en").setRegion("KE").build()
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }
    return "KES ${formatter.format(amount)}"
}
