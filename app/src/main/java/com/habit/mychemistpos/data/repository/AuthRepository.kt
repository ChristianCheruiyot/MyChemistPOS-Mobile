package com.habit.mychemistpos.data.repository

import com.habit.mychemistpos.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Handles authentication and session management.
 * In-memory for now, will be swapped to Firebase Auth later.
 */
interface AuthRepository {
    val currentUser: Flow<User?>
    val isLoggedIn: Flow<Boolean>

    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    suspend fun updateProfile(user: User): Result<Unit>
}
