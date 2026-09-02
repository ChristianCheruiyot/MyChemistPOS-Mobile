package com.habit.mychemistpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habit.mychemistpos.data.model.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE createdAt >= :startIso AND createdAt < :endIso ORDER BY createdAt DESC")
    fun getByDateRange(startIso: String, endIso: String): Flow<List<Sale>>

    @Query("SELECT * FROM sales ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: Sale)

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :startIso AND createdAt < :endIso")
    suspend fun countByDateRange(startIso: String, endIso: String): Int

    @Query("SELECT COALESCE(SUM(amountPaid), 0.0) FROM sales WHERE createdAt >= :startIso AND createdAt < :endIso")
    suspend fun totalRevenueByDateRange(startIso: String, endIso: String): Double
}
