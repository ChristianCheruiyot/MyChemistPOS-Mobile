package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.model.Expense
import com.habit.mychemistpos.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val expenses: List<Expense> = emptyList(),
    val showAddEditSheet: Boolean = false,
    val editingExpense: Expense? = null,
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.getAllExpenses().collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        expenses = list.sortedByDescending { it.createdAt }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load expenses"
                )
            }
        }
    }

    fun openAddSheet() {
        _uiState.value = _uiState.value.copy(editingExpense = null, showAddEditSheet = true)
    }

    fun openEditSheet(expense: Expense) {
        _uiState.value = _uiState.value.copy(editingExpense = expense, showAddEditSheet = true)
    }

    fun closeSheet() {
        _uiState.value = _uiState.value.copy(showAddEditSheet = false, editingExpense = null)
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            if (expense.id.isEmpty()) {
                repository.addExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense.id)
        }
    }
}
