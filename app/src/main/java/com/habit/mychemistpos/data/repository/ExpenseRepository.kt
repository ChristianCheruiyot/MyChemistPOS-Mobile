package com.habit.mychemistpos.data.repository

import com.habit.mychemistpos.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByDateRange(start: LocalDate, end: LocalDate): Flow<List<Expense>>
    fun getExpensesByCategory(category: String): Flow<List<Expense>>

    suspend fun getExpenseById(id: String): Expense?
    suspend fun addExpense(expense: Expense): Result<String>
    suspend fun updateExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(id: String): Result<Unit>
    suspend fun getTotalExpensesByDateRange(start: LocalDate, end: LocalDate): Double
}
