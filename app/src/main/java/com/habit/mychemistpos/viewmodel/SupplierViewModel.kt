package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.model.Supplier
import com.habit.mychemistpos.data.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupplierUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val suppliers: List<Supplier> = emptyList(),
    val showAddEditSheet: Boolean = false,
    val editingSupplier: Supplier? = null,
)

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val repository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierUiState())
    val uiState: StateFlow<SupplierUiState> = _uiState.asStateFlow()

    init {
        loadSuppliers()
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.getAllSuppliers().collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        suppliers = list.sortedBy { it.name }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load suppliers"
                )
            }
        }
    }

    fun openAddSheet() {
        _uiState.value = _uiState.value.copy(editingSupplier = null, showAddEditSheet = true)
    }

    fun openEditSheet(supplier: Supplier) {
        _uiState.value = _uiState.value.copy(editingSupplier = supplier, showAddEditSheet = true)
    }

    fun closeSheet() {
        _uiState.value = _uiState.value.copy(showAddEditSheet = false, editingSupplier = null)
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            if (supplier.id.isEmpty()) {
                repository.addSupplier(supplier)
            } else {
                repository.updateSupplier(supplier)
            }
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier.id)
        }
    }
}
