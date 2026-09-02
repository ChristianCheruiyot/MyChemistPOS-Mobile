package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.local.ChemistDatabase

import com.habit.mychemistpos.data.model.User
import com.habit.mychemistpos.data.repository.AuthRepository
import com.habit.mychemistpos.data.repository.impl.FirestoreSyncManager
import com.habit.mychemistpos.data.repository.impl.TenantProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val isCheckingAuth: Boolean = true,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: FirestoreSyncManager,
    private val database: ChemistDatabase,
    private val tenantProvider: TenantProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = user != null,
                    user = user,
                    isCheckingAuth = false
                )
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password

        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email address is required")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Password is required")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    viewModelScope.launch(Dispatchers.IO) {
                        syncManager.syncAllFromCloud()
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isLoggedIn = true, user = user
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = e.message ?: "Login failed"
                    )
                }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val repo = authRepository as? com.habit.mychemistpos.data.repository.impl.FirebaseAuthRepository
            
            if (repo != null) {
                val result = repo.loginWithGoogle(idToken)
                result.fold(
                    onSuccess = { user ->
                        viewModelScope.launch(Dispatchers.IO) {
                            syncManager.syncAllFromCloud()
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, isLoggedIn = true, user = user
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, error = e.message ?: "Google Login failed"
                        )
                    }
                )
            } else {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false, error = "Google Sign In not supported with current repository"
                )
            }
        }
    }

    fun setErrorMessage(message: String?) {
        _uiState.value = _uiState.value.copy(error = message)
    }



    fun logout() {
        viewModelScope.launch {
            tenantProvider.clearCache()
            syncManager.clearSyncTimestamps()
            authRepository.logout()
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
            _uiState.value = LoginUiState()
        }
    }
}
