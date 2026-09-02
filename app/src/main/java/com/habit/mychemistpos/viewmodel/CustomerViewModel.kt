package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.model.Customer
import com.habit.mychemistpos.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val customers: List<Customer> = emptyList(),
    val showAddEditSheet: Boolean = false,
    val editingCustomer: Customer? = null,
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.getAllCustomers().collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        customers = list.sortedBy { it.name }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load customers"
                )
            }
        }
    }

    fun openAddSheet() {
        _uiState.value = _uiState.value.copy(editingCustomer = null, showAddEditSheet = true)
    }

    fun openEditSheet(customer: Customer) {
        _uiState.value = _uiState.value.copy(editingCustomer = customer, showAddEditSheet = true)
    }

    fun closeSheet() {
        _uiState.value = _uiState.value.copy(showAddEditSheet = false, editingCustomer = null)
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id.isEmpty()) {
                repository.addCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer.id)
        }
    }
}
