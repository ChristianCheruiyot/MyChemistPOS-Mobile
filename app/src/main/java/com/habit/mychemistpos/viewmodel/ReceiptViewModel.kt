package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.model.Sale
import com.habit.mychemistpos.data.repository.AuthRepository
import com.habit.mychemistpos.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val sale: Sale? = null,
    val businessName: String = "",
)

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val saleId: String = checkNotNull(savedStateHandle["saleId"])
    
    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    init {
        loadReceipt()
    }

    private fun loadReceipt() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                val saleResult = salesRepository.getSaleById(saleId)
                
                if (saleResult != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sale = saleResult,
                        businessName = user?.businessName ?: "MyChemist POS"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Receipt not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load receipt"
                )
            }
        }
    }
}
