package com.habit.mychemistpos.data.model

import java.time.LocalDateTime
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
