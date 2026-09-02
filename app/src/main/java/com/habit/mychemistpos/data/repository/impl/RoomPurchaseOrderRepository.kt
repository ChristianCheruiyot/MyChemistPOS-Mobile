package com.habit.mychemistpos.data.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.habit.mychemistpos.data.local.dao.PurchaseOrderDao
import com.habit.mychemistpos.data.model.OrderStatus
import com.habit.mychemistpos.data.model.PurchaseOrder
import com.habit.mychemistpos.data.repository.PurchaseOrderRepository
import com.habit.mychemistpos.util.toMap
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPurchaseOrderRepository @Inject constructor(
    private val dao: PurchaseOrderDao,
    private val firestore: FirebaseFirestore,
    private val tenantProvider: TenantProvider
) : PurchaseOrderRepository {

    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    override fun getAllOrders(): Flow<List<PurchaseOrder>> = dao.getAll()

    override fun getOrdersBySupplierId(supplierId: String): Flow<List<PurchaseOrder>> =
        dao.getBySupplierId(supplierId)

    override suspend fun getOrderById(id: String): PurchaseOrder? = dao.getById(id)

    override suspend fun addOrder(order: PurchaseOrder): Result<String> {
        return try {
            val id = if (order.id.isBlank()) UUID.randomUUID().toString() else order.id
            val copiedOrder = order.copy(id = id, createdAt = LocalDateTime.now())
            dao.insert(copiedOrder)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("purchase_orders").document(id)
                    .set(copiedOrder.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrder(order: PurchaseOrder): Result<Unit> {
        return try {
            dao.update(order)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("purchase_orders").document(order.id)
                    .set(order.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(id: String, status: OrderStatus): Result<Unit> {
        return try {
            val order = dao.getById(id) ?: return Result.failure(Exception("Order not found"))
            val updated = order.copy(
                status = status,
                deliveredAt = if (status == OrderStatus.DELIVERED) LocalDateTime.now() else order.deliveredAt
            )
            dao.update(updated)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("purchase_orders").document(id)
                    .set(updated.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteOrder(id: String): Result<Unit> {
        return try {
            dao.deleteById(id)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("purchase_orders").document(id)
                    .delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
