package com.habit.mychemistpos.data.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.habit.mychemistpos.data.local.dao.ExpenseDao
import com.habit.mychemistpos.data.model.Expense
import com.habit.mychemistpos.data.repository.ExpenseRepository
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
class RoomExpenseRepository @Inject constructor(
    private val dao: ExpenseDao,
    private val firestore: FirebaseFirestore,
    private val tenantProvider: TenantProvider
) : ExpenseRepository {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    override fun getAllExpenses(): Flow<List<Expense>> = dao.getAll()

    override fun getExpensesByDateRange(start: LocalDate, end: LocalDate): Flow<List<Expense>> {
        val startIso = LocalDateTime.of(start, LocalTime.MIN).format(formatter)
        val endIso = LocalDateTime.of(end.plusDays(1), LocalTime.MIN).format(formatter)
        return dao.getByDateRange(startIso, endIso)
    }

    override fun getExpensesByCategory(category: String): Flow<List<Expense>> =
        dao.getByCategory(category)

    override suspend fun getExpenseById(id: String): Expense? = dao.getById(id)

    override suspend fun addExpense(expense: Expense): Result<String> {
        return try {
            val id = if (expense.id.isBlank()) UUID.randomUUID().toString() else expense.id
            val copiedExpense = expense.copy(id = id, createdAt = LocalDateTime.now())
            dao.insert(copiedExpense)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("expenses").document(id)
                    .set(copiedExpense.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            dao.update(expense)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("expenses").document(expense.id)
                    .set(expense.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(id: String): Result<Unit> {
        return try {
            dao.deleteById(id)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("expenses").document(id)
                    .delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalExpensesByDateRange(start: LocalDate, end: LocalDate): Double {
        val startIso = LocalDateTime.of(start, LocalTime.MIN).format(formatter)
        val endIso = LocalDateTime.of(end.plusDays(1), LocalTime.MIN).format(formatter)
        return dao.totalByDateRange(startIso, endIso)
    }
}
