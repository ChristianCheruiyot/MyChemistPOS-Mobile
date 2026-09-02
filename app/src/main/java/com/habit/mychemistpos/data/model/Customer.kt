package com.habit.mychemistpos.data.model

import java.time.LocalDateTime
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val phone: String = "",
    val creditBalance: Double = 0.0,
    val prescriptionHistory: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
