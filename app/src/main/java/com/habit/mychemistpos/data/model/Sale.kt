package com.habit.mychemistpos.data.model

import java.time.LocalDateTime
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable as KSerializable

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey val id: String = "",
    val items: List<SaleLineItem> = emptyList(),
    val total: Double = 0.0,
    val discount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val servedBy: String = "",
    val customerId: String? = null,
    val prescriptionRef: String? = null,
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    /** Net total after discount */
    val netTotal: Double
        get() = total - discount

    /** Change to give back */
    val change: Double
        get() = if (amountPaid > netTotal) amountPaid - netTotal else 0.0

    /** Whether this is a credit sale */
    val isCredit: Boolean
        get() = paymentMethod == PaymentMethod.CREDIT

    /** Outstanding balance for credit sales */
    val outstandingBalance: Double
        get() = if (isCredit) netTotal - amountPaid else 0.0
}

@KSerializable
data class SaleLineItem(
    val itemId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val requiresPrescription: Boolean = false,
) {
    val subtotal: Double
        get() = unitPrice * quantity
}

enum class PaymentMethod {
    CASH,
    MPESA,
    CREDIT;

    val displayName: String
        get() = when (this) {
            CASH -> "Cash"
            MPESA -> "M-Pesa"
            CREDIT -> "Credit"
        }
}
