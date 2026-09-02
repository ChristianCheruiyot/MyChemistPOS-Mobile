package com.habit.mychemistpos.data.model

import java.time.LocalDateTime
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable as KSerializable

@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey val id: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val items: List<PurchaseOrderItem> = emptyList(),
    val totalCost: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val deliveredAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    val computedTotalCost: Double
        get() = items.sumOf { it.totalCost }
}

@KSerializable
data class PurchaseOrderItem(
    val itemId: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val unitCost: Double = 0.0,
) {
    val totalCost: Double
        get() = unitCost * quantity
}

enum class OrderStatus {
    PENDING,
    DELIVERED,
    CANCELLED;

    val displayName: String
        get() = when (this) {
            PENDING -> "Pending"
            DELIVERED -> "Delivered"
            CANCELLED -> "Cancelled"
        }
}
