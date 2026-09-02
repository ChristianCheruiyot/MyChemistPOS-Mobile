package com.habit.mychemistpos.data.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.habit.mychemistpos.data.local.dao.SaleDao
import com.habit.mychemistpos.data.model.Sale
import com.habit.mychemistpos.data.repository.SalesRepository
import com.habit.mychemistpos.util.toMap
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSalesRepository @Inject constructor(
    private val dao: SaleDao,
    private val firestore: FirebaseFirestore,
    private val tenantProvider: TenantProvider
) : SalesRepository {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    override fun getAllSales(): Flow<List<Sale>> = dao.getAll()

    override fun getTodaySales(): Flow<List<Sale>> {
        val start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(formatter)
        val end = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN).format(formatter)
        return dao.getByDateRange(start, end)
    }

    override fun getSalesByDateRange(start: LocalDate, end: LocalDate): Flow<List<Sale>> {
        val startIso = LocalDateTime.of(start, LocalTime.MIN).format(formatter)
        val endIso = LocalDateTime.of(end.plusDays(1), LocalTime.MIN).format(formatter)
        return dao.getByDateRange(startIso, endIso)
    }

    override fun getRecentSales(limit: Int): Flow<List<Sale>> = dao.getRecent(limit)

    override suspend fun getSaleById(id: String): Sale? = dao.getById(id)

    override suspend fun addSale(sale: Sale): Result<String> {
        return try {
            val id = if (sale.id.isBlank()) UUID.randomUUID().toString() else sale.id
            val copiedSale = sale.copy(id = id, createdAt = LocalDateTime.now())
            dao.insert(copiedSale)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("transactions").document(id)
                    .set(copiedSale.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSalesCountToday(): Int {
        val start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(formatter)
        val end = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN).format(formatter)
        return dao.countByDateRange(start, end)
    }

    override suspend fun getTotalRevenueToday(): Double {
        val start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(formatter)
        val end = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN).format(formatter)
        return dao.totalRevenueByDateRange(start, end)
    }
}
