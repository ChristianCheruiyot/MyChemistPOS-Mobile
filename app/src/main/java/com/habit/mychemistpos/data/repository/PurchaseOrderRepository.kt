package com.habit.mychemistpos.data.repository

import com.habit.mychemistpos.data.model.PurchaseOrder
import kotlinx.coroutines.flow.Flow

interface PurchaseOrderRepository {
    fun getAllOrders(): Flow<List<PurchaseOrder>>
    fun getOrdersBySupplierId(supplierId: String): Flow<List<PurchaseOrder>>

    suspend fun getOrderById(id: String): PurchaseOrder?
    suspend fun addOrder(order: PurchaseOrder): Result<String>
    suspend fun updateOrder(order: PurchaseOrder): Result<Unit>
    suspend fun updateOrderStatus(id: String, status: com.habit.mychemistpos.data.model.OrderStatus): Result<Unit>
    suspend fun deleteOrder(id: String): Result<Unit>
}
