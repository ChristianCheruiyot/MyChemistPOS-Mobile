package com.habit.mychemistpos.data.repository.impl

import com.habit.mychemistpos.data.model.User
import com.habit.mychemistpos.data.model.UserRole
import com.habit.mychemistpos.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryAuthRepository @Inject constructor() : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser
    override val isLoggedIn: Flow<Boolean> = _currentUser.map { it != null }

    // Demo credentials
    private val demoUser = User(
        id = "user_001",
        name = "Dr. Amina Kariuki",
        email = "amina@example.com",
        businessName = "Dawa Pharmacy",
        role = UserRole.OWNER
    )
    private val demoPassword = "password123"

    override suspend fun login(email: String, password: String): Result<User> {
        // Simulate network delay
        kotlinx.coroutines.delay(500)

        _currentUser.value = demoUser
        return Result.success(demoUser)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun getCurrentUser(): User? = _currentUser.value

    override suspend fun updateProfile(user: User): Result<Unit> {
        _currentUser.value = user
        return Result.success(Unit)
    }
}
