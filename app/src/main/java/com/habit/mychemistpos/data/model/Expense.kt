package com.habit.mychemistpos.data.model

import java.time.LocalDateTime
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = "",
    val category: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** Common pharmacy expense categories */
val ExpenseCategories = listOf(
    "Rent",
    "Utilities",
    "Salaries",
    "Transport",
    "Supplies",
    "Maintenance",
    "Licensing",
    "Marketing",
    "Miscellaneous"
)
