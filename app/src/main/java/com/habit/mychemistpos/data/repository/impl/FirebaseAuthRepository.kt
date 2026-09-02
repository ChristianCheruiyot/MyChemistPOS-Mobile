package com.habit.mychemistpos.data.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.habit.mychemistpos.data.model.User
import com.habit.mychemistpos.data.model.UserRole
import com.habit.mychemistpos.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(
        auth.currentUser?.let { firebaseUser ->
            User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Pharmacist",
                email = firebaseUser.email ?: "",
                businessName = "MyChemist POS",
                role = UserRole.OWNER
            )
        }
    )
    private val _isInitialized = MutableStateFlow(true)

    override val currentUser: Flow<User?> = combine(_currentUser, _isInitialized) { user, _ ->
        user
    }.filter { _isInitialized.value }

    override val isLoggedIn: Flow<Boolean> = currentUser.map { it != null }

    init {
        // Listen to Firebase Auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Map FirebaseUser to our domain User model
                val user = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Pharmacist",
                    email = firebaseUser.email ?: "",
                    businessName = "MyChemist POS", // Placeholder until Firestore is integrated
                    role = UserRole.OWNER
                )
                _currentUser.value = user
            } else {
                _currentUser.value = null
            }
            _isInitialized.value = true
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed: User is null")
            
            val user = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Pharmacist",
                email = firebaseUser.email ?: "",
                businessName = "MyChemist POS",
                role = UserRole.OWNER
            )
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google Login failed: User is null")
            
            val user = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Pharmacist",
                email = firebaseUser.email ?: "",
                businessName = "MyChemist POS",
                role = UserRole.OWNER
            )
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    override suspend fun getCurrentUser(): User? = _currentUser.value

    override suspend fun updateProfile(user: User): Result<Unit> {
        // TODO: Update user profile in Firestore when integrated.
        _currentUser.value = user
        return Result.success(Unit)
    }
}
