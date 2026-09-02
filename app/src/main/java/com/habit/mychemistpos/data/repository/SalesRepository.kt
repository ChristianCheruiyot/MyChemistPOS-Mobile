package com.habit.mychemistpos.data.repository

import com.habit.mychemistpos.data.model.Sale
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SalesRepository {
    fun getAllSales(): Flow<List<Sale>>
    fun getTodaySales(): Flow<List<Sale>>
    fun getSalesByDateRange(start: LocalDate, end: LocalDate): Flow<List<Sale>>
    fun getRecentSales(limit: Int = 10): Flow<List<Sale>>

    suspend fun getSaleById(id: String): Sale?
    suspend fun addSale(sale: Sale): Result<String>
    // Note: sales cannot be deleted per business rules
    suspend fun getSalesCountToday(): Int
    suspend fun getTotalRevenueToday(): Double
}
