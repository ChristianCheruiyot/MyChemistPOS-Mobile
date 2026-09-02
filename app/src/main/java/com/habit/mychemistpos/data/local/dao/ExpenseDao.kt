package com.habit.mychemistpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habit.mychemistpos.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE createdAt >= :startIso AND createdAt < :endIso ORDER BY createdAt DESC")
    fun getByDateRange(startIso: String, endIso: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY createdAt DESC")
    fun getByCategory(category: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE createdAt >= :startIso AND createdAt < :endIso")
    suspend fun totalByDateRange(startIso: String, endIso: String): Double
}
