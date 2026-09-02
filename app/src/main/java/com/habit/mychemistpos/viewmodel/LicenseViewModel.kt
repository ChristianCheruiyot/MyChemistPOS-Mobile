package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.repository.LicenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LicenseUiState(
    val isLicensed: Boolean = false,
    val isCheckingLicense: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val licenseKey: String = "",
    val deviceId: String = ""
)

@HiltViewModel
class LicenseViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LicenseUiState())
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            licenseRepository.isLicensed.collect { isLicensed ->
                _uiState.value = _uiState.value.copy(
                    isLicensed = isLicensed,
                    licenseKey = licenseRepository.getLicenseKey(),
                    deviceId = licenseRepository.getDeviceId()
                )
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingLicense = true)
            licenseRepository.checkLicenseValiditySilent()
            _uiState.value = _uiState.value.copy(isCheckingLicense = false)
        }
    }

    fun activateLicense(key: String) {
        if (key.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "License key cannot be empty")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            val result = licenseRepository.activateLicense(key)
            result.fold(
                onSuccess = { msg ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = msg,
                        licenseKey = licenseRepository.getLicenseKey()
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to activate license"
                    )
                }
            )
        }
    }

    fun deactivateLicense() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            licenseRepository.deactivateLicense()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                licenseKey = "",
                successMessage = null,
                error = null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
