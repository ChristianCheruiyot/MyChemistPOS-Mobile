package com.habit.mychemistpos.data.repository

import com.habit.mychemistpos.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllItems(): Flow<List<InventoryItem>>
    fun searchItems(query: String): Flow<List<InventoryItem>>
    fun getItemsByCategory(category: String): Flow<List<InventoryItem>>
    fun getLowStockItems(): Flow<List<InventoryItem>>
    fun getExpiringSoonItems(withinDays: Int = 30): Flow<List<InventoryItem>>
    fun getExpiredItems(): Flow<List<InventoryItem>>

    suspend fun getItemById(id: String): InventoryItem?
    suspend fun addItem(item: InventoryItem): Result<String>
    suspend fun updateItem(item: InventoryItem): Result<Unit>
    suspend fun deleteItem(id: String): Result<Unit>
    suspend fun updateQuantity(id: String, newQuantity: Int): Result<Unit>

    /** Bulk reduce quantities after a sale */
    suspend fun deductStock(items: Map<String, Int>): Result<Unit>
}
