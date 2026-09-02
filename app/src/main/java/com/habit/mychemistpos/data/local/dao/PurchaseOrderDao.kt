package com.habit.mychemistpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habit.mychemistpos.data.model.PurchaseOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseOrderDao {

    @Query("SELECT * FROM purchase_orders ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM purchase_orders WHERE supplierId = :supplierId ORDER BY createdAt DESC")
    fun getBySupplierId(supplierId: String): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM purchase_orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PurchaseOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: PurchaseOrder)

    @Update
    suspend fun update(order: PurchaseOrder)

    @Query("DELETE FROM purchase_orders WHERE id = :id")
    suspend fun deleteById(id: String)
}
