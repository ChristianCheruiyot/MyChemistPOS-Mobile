package com.habit.mychemistpos.data.repository.impl

import com.habit.mychemistpos.data.model.OrderStatus
import com.habit.mychemistpos.data.model.PurchaseOrder
import com.habit.mychemistpos.data.model.PurchaseOrderItem
import com.habit.mychemistpos.data.repository.PurchaseOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryPurchaseOrderRepository @Inject constructor() : PurchaseOrderRepository {

    private val _orders = MutableStateFlow(seedOrders())

    override fun getAllOrders(): Flow<List<PurchaseOrder>> =
        _orders.map { it.sortedByDescending { o -> o.createdAt } }

    override fun getOrdersBySupplierId(supplierId: String): Flow<List<PurchaseOrder>> =
        _orders.map { list -> list.filter { it.supplierId == supplierId } }

    override suspend fun getOrderById(id: String): PurchaseOrder? =
        _orders.value.find { it.id == id }

    override suspend fun addOrder(order: PurchaseOrder): Result<String> {
        val id = UUID.randomUUID().toString()
        _orders.value = _orders.value + order.copy(id = id, createdAt = LocalDateTime.now())
        return Result.success(id)
    }

    override suspend fun updateOrder(order: PurchaseOrder): Result<Unit> {
        _orders.value = _orders.value.map { if (it.id == order.id) order else it }
        return Result.success(Unit)
    }

    override suspend fun updateOrderStatus(id: String, status: OrderStatus): Result<Unit> {
        _orders.value = _orders.value.map {
            if (it.id == id) it.copy(
                status = status,
                deliveredAt = if (status == OrderStatus.DELIVERED) LocalDateTime.now() else it.deliveredAt
            ) else it
        }
        return Result.success(Unit)
    }

    override suspend fun deleteOrder(id: String): Result<Unit> {
        _orders.value = _orders.value.filter { it.id != id }
        return Result.success(Unit)
    }
}

private fun seedOrders(): List<PurchaseOrder> {
    val now = LocalDateTime.now()
    return listOf(
        PurchaseOrder(
            id = "po_001", supplierId = "sup_001", supplierName = "PharmaCare Distributors",
            items = listOf(
                PurchaseOrderItem("inv_001", "Paracetamol 500mg", 100, 150.0),
                PurchaseOrderItem("inv_002", "Amoxicillin 250mg", 50, 320.0),
            ),
            totalCost = 31000.0, status = OrderStatus.DELIVERED,
            deliveredAt = now.minusDays(3), createdAt = now.minusDays(5)
        ),
        PurchaseOrder(
            id = "po_002", supplierId = "sup_002", supplierName = "MedSupply Kenya Ltd",
            items = listOf(
                PurchaseOrderItem("inv_003", "Cetrizine 10mg", 60, 80.0),
                PurchaseOrderItem("inv_008", "Vitamin C 1000mg", 100, 350.0),
            ),
            totalCost = 39800.0, status = OrderStatus.PENDING,
            createdAt = now.minusDays(1)
        ),
    )
}
