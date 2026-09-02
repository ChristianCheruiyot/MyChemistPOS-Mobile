package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.repository.ExpenseRepository
import com.habit.mychemistpos.data.repository.SalesRepository
import com.habit.mychemistpos.data.repository.impl.FirestoreSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ReportRange {
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_12_MONTHS
}

data class ReportStat(val label: String, val revenue: Double, val expenses: Double)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalTransactions: Int = 0,
    val reportStats: List<ReportStat> = emptyList(),
    val selectedRange: ReportRange = ReportRange.LAST_7_DAYS,
    val baseDate: LocalDate = LocalDate.now()
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val expenseRepository: ExpenseRepository,
    private val syncManager: FirestoreSyncManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing

    fun sync() {
        viewModelScope.launch {
            syncManager.syncAllFromCloud(force = false)
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            syncManager.syncAllFromCloud(force = true)
        }
    }

    private val _selectedRange = MutableStateFlow(ReportRange.LAST_7_DAYS)
    val selectedRange: StateFlow<ReportRange> = _selectedRange.asStateFlow()

    private val _baseDate = MutableStateFlow(LocalDate.now())
    val baseDate: StateFlow<LocalDate> = _baseDate.asStateFlow()

    init {
        loadData()
    }

    fun selectRange(range: ReportRange) {
        _selectedRange.value = range
        _baseDate.value = LocalDate.now() // Reset baseDate on range change
    }

    fun navigateWeek(direction: Int) {
        _baseDate.value = _baseDate.value.plusWeeks(direction.toLong())
        if (_baseDate.value.isAfter(LocalDate.now())) {
            _baseDate.value = LocalDate.now()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                salesRepository.getAllSales(),
                expenseRepository.getAllExpenses(),
                _selectedRange,
                _baseDate
            ) { sales, expenses, range, baseDate ->
                // Cutoff date for KPIs
                val cutoffDate = when (range) {
                    ReportRange.LAST_7_DAYS -> baseDate.minusDays(7)
                    ReportRange.LAST_30_DAYS -> baseDate.minusDays(30)
                    ReportRange.LAST_12_MONTHS -> baseDate.minusMonths(12)
                }

                val filteredSales = sales.filter { 
                    val date = it.createdAt.toLocalDate()
                    if (range == ReportRange.LAST_7_DAYS) {
                        !date.isBefore(baseDate.minusDays(6)) && !date.isAfter(baseDate)
                    } else {
                        !date.isBefore(cutoffDate) && !date.isAfter(baseDate)
                    }
                }
                
                val filteredExpenses = expenses.filter { 
                    val date = it.createdAt.toLocalDate()
                    if (range == ReportRange.LAST_7_DAYS) {
                        !date.isBefore(baseDate.minusDays(6)) && !date.isAfter(baseDate)
                    } else {
                        !date.isBefore(cutoffDate) && !date.isAfter(baseDate)
                    }
                }

                val totalRev = filteredSales.sumOf { it.amountPaid }
                val totalExp = filteredExpenses.sumOf { it.amount }

                // Group stats for chart
                val stats = when (range) {
                    ReportRange.LAST_7_DAYS -> {
                        (6L downTo 0L).map { daysAgo ->
                            val date = baseDate.minusDays(daysAgo)
                            val rev = sales.filter { it.createdAt.toLocalDate() == date }.sumOf { it.amountPaid }
                            val exp = expenses.filter { it.createdAt.toLocalDate() == date }.sumOf { it.amount }
                            ReportStat(date.format(DateTimeFormatter.ofPattern("EEE")), rev, exp)
                        }
                    }
                    ReportRange.LAST_30_DAYS -> {
                        (29L downTo 0L).map { daysAgo ->
                            val date = baseDate.minusDays(daysAgo)
                            val rev = sales.filter { it.createdAt.toLocalDate() == date }.sumOf { it.amountPaid }
                            val exp = expenses.filter { it.createdAt.toLocalDate() == date }.sumOf { it.amount }
                            ReportStat(date.dayOfMonth.toString(), rev, exp)
                        }
                    }
                    ReportRange.LAST_12_MONTHS -> {
                        (11L downTo 0L).map { monthsAgo ->
                            val monthDate = baseDate.minusMonths(monthsAgo)
                            val yearMonth = java.time.YearMonth.from(monthDate)
                            val rev = sales.filter { 
                                val saleDate = it.createdAt.toLocalDate()
                                saleDate.year == yearMonth.year && saleDate.month == yearMonth.month 
                            }.sumOf { it.amountPaid }
                            val exp = expenses.filter { 
                                val expDate = it.createdAt.toLocalDate()
                                expDate.year == yearMonth.year && expDate.month == yearMonth.month 
                            }.sumOf { it.amount }
                            ReportStat(monthDate.format(DateTimeFormatter.ofPattern("MMM")), rev, exp)
                        }
                    }
                }

                ReportsUiState(
                    isLoading = false,
                    totalRevenue = totalRev,
                    totalExpenses = totalExp,
                    netProfit = totalRev - totalExp,
                    totalTransactions = filteredSales.size,
                    reportStats = stats,
                    selectedRange = range,
                    baseDate = baseDate
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
