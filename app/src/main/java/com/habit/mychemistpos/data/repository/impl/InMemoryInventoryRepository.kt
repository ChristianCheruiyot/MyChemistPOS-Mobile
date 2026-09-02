package com.habit.mychemistpos.data.repository.impl

import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.data.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryInventoryRepository @Inject constructor() : InventoryRepository {

    private val _items = MutableStateFlow(seedInventory())

    override fun getAllItems(): Flow<List<InventoryItem>> = _items

    override fun searchItems(query: String): Flow<List<InventoryItem>> =
        _items.map { list ->
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.batchNumber.contains(query, ignoreCase = true)
            }
        }

    override fun getItemsByCategory(category: String): Flow<List<InventoryItem>> =
        _items.map { list -> list.filter { it.category == category } }

    override fun getLowStockItems(): Flow<List<InventoryItem>> =
        _items.map { list -> list.filter { it.isLowStock } }

    override fun getExpiringSoonItems(withinDays: Int): Flow<List<InventoryItem>> =
        _items.map { list ->
            val cutoff = LocalDate.now().plusDays(withinDays.toLong())
            list.filter { item ->
                item.expiryDate != null &&
                !item.expiryDate.isBefore(LocalDate.now()) &&
                item.expiryDate.isBefore(cutoff)
            }
        }

    override fun getExpiredItems(): Flow<List<InventoryItem>> =
        _items.map { list -> list.filter { it.isExpired } }

    override suspend fun getItemById(id: String): InventoryItem? =
        _items.value.find { it.id == id }

    override suspend fun addItem(item: InventoryItem): Result<String> {
        val id = UUID.randomUUID().toString()
        val newItem = item.copy(id = id, createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
        _items.value = _items.value + newItem
        return Result.success(id)
    }

    override suspend fun updateItem(item: InventoryItem): Result<Unit> {
        _items.value = _items.value.map {
            if (it.id == item.id) item.copy(updatedAt = LocalDateTime.now()) else it
        }
        return Result.success(Unit)
    }

    override suspend fun deleteItem(id: String): Result<Unit> {
        _items.value = _items.value.filter { it.id != id }
        return Result.success(Unit)
    }

    override suspend fun updateQuantity(id: String, newQuantity: Int): Result<Unit> {
        _items.value = _items.value.map {
            if (it.id == id) it.copy(quantity = newQuantity, updatedAt = LocalDateTime.now()) else it
        }
        return Result.success(Unit)
    }

    override suspend fun deductStock(items: Map<String, Int>): Result<Unit> {
        _items.value = _items.value.map { item ->
            val deduction = items[item.id]
            if (deduction != null) {
                item.copy(
                    quantity = (item.quantity - deduction).coerceAtLeast(0),
                    updatedAt = LocalDateTime.now()
                )
            } else item
        }
        return Result.success(Unit)
    }
}

// ─── Seed Data ──────────────────────────────────────────────────────────────────
private fun seedInventory(): List<InventoryItem> {
    val now = LocalDateTime.now()
    return listOf(
        InventoryItem("inv_001", "Paracetamol 500mg", "Analgesics", "BN-2025-001", 150.0, 250.0, 120, "tablets", LocalDate.now().plusMonths(18), 20, "sup_001", false, "preset:pills", now, now),
        InventoryItem("inv_002", "Amoxicillin 250mg", "Antibiotics", "BN-2025-002", 320.0, 500.0, 45, "capsules", LocalDate.now().plusMonths(12), 15, "sup_001", true, "preset:pills", now, now),
        InventoryItem("inv_003", "Cetrizine 10mg", "Antihistamines", "BN-2025-003", 80.0, 150.0, 8, "tablets", LocalDate.now().plusDays(15), 10, "sup_002", false, "preset:pills", now, now),
        InventoryItem("inv_004", "Ibuprofen 400mg", "Analgesics", "BN-2025-004", 200.0, 350.0, 90, "tablets", LocalDate.now().plusMonths(24), 25, "sup_001", false, "preset:pills", now, now),
        InventoryItem("inv_005", "Metformin 500mg", "Diabetes", "BN-2025-005", 250.0, 420.0, 5, "tablets", LocalDate.now().plusMonths(6), 10, "sup_003", true, "preset:pills", now, now),
        InventoryItem("inv_006", "Omeprazole 20mg", "Gastrointestinal", "BN-2025-006", 180.0, 300.0, 60, "capsules", LocalDate.now().plusMonths(9), 15, "sup_002", false, "preset:pills", now, now),
        InventoryItem("inv_007", "Salbutamol Inhaler", "Respiratory", "BN-2025-007", 800.0, 1200.0, 12, "pcs", LocalDate.now().plusMonths(15), 5, "sup_003", true, "preset:spray", now, now),
        InventoryItem("inv_008", "Vitamin C 1000mg", "Vitamins", "BN-2025-008", 350.0, 550.0, 200, "tablets", LocalDate.now().plusMonths(20), 30, "sup_002", false, "preset:pills", now, now),
        InventoryItem("inv_009", "Clotrimazole Cream", "Antifungals", "BN-2025-009", 120.0, 220.0, 3, "tubes", LocalDate.now().minusDays(5), 8, "sup_001", false, "preset:cream", now, now),
        InventoryItem("inv_010", "Bandage Roll 10cm", "Surgical Supplies", "BN-2025-010", 50.0, 100.0, 40, "pcs", null, 10, "sup_003", false, "preset:surgical", now, now),
        InventoryItem("inv_011", "Loratadine 10mg", "Antihistamines", "BN-2025-011", 90.0, 180.0, 55, "tablets", LocalDate.now().plusDays(25), 10, "sup_002", false, "preset:pills", now, now),
        InventoryItem("inv_012", "Doxycycline 100mg", "Antibiotics", "BN-2025-012", 280.0, 450.0, 35, "capsules", LocalDate.now().plusMonths(8), 10, "sup_001", true, "preset:pills", now, now),
    )
}
