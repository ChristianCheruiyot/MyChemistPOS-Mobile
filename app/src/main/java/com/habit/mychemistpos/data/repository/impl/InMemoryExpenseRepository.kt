package com.habit.mychemistpos.data.repository.impl

import com.habit.mychemistpos.data.model.Expense
import com.habit.mychemistpos.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryExpenseRepository @Inject constructor() : ExpenseRepository {

    private val _expenses = MutableStateFlow(seedExpenses())

    override fun getAllExpenses(): Flow<List<Expense>> =
        _expenses.map { it.sortedByDescending { e -> e.createdAt } }

    override fun getExpensesByDateRange(start: LocalDate, end: LocalDate): Flow<List<Expense>> =
        _expenses.map { list ->
            list.filter {
                val date = it.createdAt.toLocalDate()
                !date.isBefore(start) && !date.isAfter(end)
            }.sortedByDescending { it.createdAt }
        }

    override fun getExpensesByCategory(category: String): Flow<List<Expense>> =
        _expenses.map { list -> list.filter { it.category == category } }

    override suspend fun getExpenseById(id: String): Expense? =
        _expenses.value.find { it.id == id }

    override suspend fun addExpense(expense: Expense): Result<String> {
        val id = UUID.randomUUID().toString()
        _expenses.value = _expenses.value + expense.copy(id = id, createdAt = LocalDateTime.now())
        return Result.success(id)
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> {
        _expenses.value = _expenses.value.map { if (it.id == expense.id) expense else it }
        return Result.success(Unit)
    }

    override suspend fun deleteExpense(id: String): Result<Unit> {
        _expenses.value = _expenses.value.filter { it.id != id }
        return Result.success(Unit)
    }

    override suspend fun getTotalExpensesByDateRange(start: LocalDate, end: LocalDate): Double =
        _expenses.value.filter {
            val d = it.createdAt.toLocalDate(); !d.isBefore(start) && !d.isAfter(end)
        }.sumOf { it.amount }
}

private fun seedExpenses(): List<Expense> {
    val today = LocalDate.now()
    return listOf(
        Expense("exp_001", "Rent", "Monthly shop rent", 25000.0, today.atTime(8, 0)),
        Expense("exp_002", "Utilities", "Electricity bill", 3500.0, today.minusDays(2).atTime(10, 0)),
        Expense("exp_003", "Transport", "Stock delivery", 1500.0, today.minusDays(1).atTime(14, 0)),
        Expense("exp_004", "Supplies", "Receipt paper", 800.0, today.atTime(9, 30)),
    )
}
