package com.habit.mychemistpos.data.repository.impl

import com.habit.mychemistpos.data.model.PaymentMethod
import com.habit.mychemistpos.data.model.Sale
import com.habit.mychemistpos.data.model.SaleLineItem
import com.habit.mychemistpos.data.repository.SalesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemorySalesRepository @Inject constructor() : SalesRepository {

    private val _sales = MutableStateFlow(seedSales())

    override fun getAllSales(): Flow<List<Sale>> =
        _sales.map { it.sortedByDescending { s -> s.createdAt } }

    override fun getTodaySales(): Flow<List<Sale>> =
        _sales.map { list ->
            list.filter { it.createdAt.toLocalDate() == LocalDate.now() }
                .sortedByDescending { it.createdAt }
        }

    override fun getSalesByDateRange(start: LocalDate, end: LocalDate): Flow<List<Sale>> =
        _sales.map { list ->
            list.filter {
                val date = it.createdAt.toLocalDate()
                !date.isBefore(start) && !date.isAfter(end)
            }.sortedByDescending { it.createdAt }
        }

    override fun getRecentSales(limit: Int): Flow<List<Sale>> =
        _sales.map { it.sortedByDescending { s -> s.createdAt }.take(limit) }

    override suspend fun getSaleById(id: String): Sale? =
        _sales.value.find { it.id == id }

    override suspend fun addSale(sale: Sale): Result<String> {
        val id = UUID.randomUUID().toString()
        val newSale = sale.copy(id = id, createdAt = LocalDateTime.now())
        _sales.value = _sales.value + newSale
        return Result.success(id)
    }

    override suspend fun getSalesCountToday(): Int =
        _sales.value.count { it.createdAt.toLocalDate() == LocalDate.now() }

    override suspend fun getTotalRevenueToday(): Double =
        _sales.value
            .filter { it.createdAt.toLocalDate() == LocalDate.now() }
            .sumOf { it.netTotal }
}

private fun seedSales(): List<Sale> {
    val today = LocalDate.now()
    return listOf(
        Sale(
            id = "sale_001",
            items = listOf(
                SaleLineItem("inv_001", "Paracetamol 500mg", 2, 250.0),
                SaleLineItem("inv_008", "Vitamin C 1000mg", 1, 550.0),
            ),
            total = 1050.0, discount = 50.0, amountPaid = 1000.0,
            paymentMethod = PaymentMethod.CASH, servedBy = "Dr. Amina Kariuki",
            createdAt = today.atTime(9, 15)
        ),
        Sale(
            id = "sale_002",
            items = listOf(
                SaleLineItem("inv_002", "Amoxicillin 250mg", 1, 500.0, true),
                SaleLineItem("inv_006", "Omeprazole 20mg", 2, 300.0),
            ),
            total = 1100.0, discount = 0.0, amountPaid = 1100.0,
            paymentMethod = PaymentMethod.CASH, servedBy = "Dr. Amina Kariuki",
            prescriptionRef = "RX-2026-0042",
            createdAt = today.atTime(10, 30)
        ),
        Sale(
            id = "sale_003",
            items = listOf(
                SaleLineItem("inv_004", "Ibuprofen 400mg", 3, 350.0),
            ),
            total = 1050.0, discount = 0.0, amountPaid = 1050.0,
            paymentMethod = PaymentMethod.CASH, servedBy = "Dr. Amina Kariuki",
            createdAt = today.atTime(11, 45)
        ),
        Sale(
            id = "sale_004",
            items = listOf(
                SaleLineItem("inv_005", "Metformin 500mg", 2, 420.0, true),
                SaleLineItem("inv_001", "Paracetamol 500mg", 1, 250.0),
            ),
            total = 1090.0, discount = 90.0, amountPaid = 500.0,
            paymentMethod = PaymentMethod.CREDIT, servedBy = "Dr. Amina Kariuki",
            customerId = "cust_001",
            createdAt = today.minusDays(1).atTime(14, 20)
        ),
    )
}
