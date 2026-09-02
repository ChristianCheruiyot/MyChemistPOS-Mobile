package com.habit.mychemistpos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habit.mychemistpos.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAll(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE category = :category ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE quantity <= reorderLevel ORDER BY quantity ASC")
    fun getLowStock(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem)

    @Update
    suspend fun update(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE inventory_items SET quantity = :newQuantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateQuantity(id: String, newQuantity: Int, updatedAt: String)
}
