package com.habit.mychemistpos.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val businessName: String = "",
    val role: UserRole = UserRole.OWNER,
)

enum class UserRole {
    OWNER,
    PHARMACIST,
    CASHIER
}
