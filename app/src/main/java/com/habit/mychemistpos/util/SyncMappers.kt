package com.habit.mychemistpos.util

import com.google.firebase.Timestamp
import com.habit.mychemistpos.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant

// ── Robust LocalDateTime / LocalDate Conversions ─────────────────────────────

fun parseStringToLocalDateTime(str: String): LocalDateTime? {
    if (str.isBlank()) return null
    
    // 1. Try parsing as ZonedDateTime (handles offsets like +03:00 or Z)
    try {
        val zdt = java.time.ZonedDateTime.parse(str)
        return zdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    } catch (e: Exception) {}

    // 2. Try parsing as OffsetDateTime
    try {
        val odt = java.time.OffsetDateTime.parse(str)
        return odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    } catch (e: Exception) {}

    // 3. Try parsing as Instant
    try {
        val instant = Instant.parse(str)
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    } catch (e: Exception) {}

    // 4. Try parsing as standard LocalDateTime (no offset, e.g. 2026-05-30T11:00:44)
    try {
        return LocalDateTime.parse(str)
    } catch (e: Exception) {}

    // 5. Try parsing as standard LocalDate (no time, e.g. 2026-05-30)
    try {
        return LocalDate.parse(str).atStartOfDay()
    } catch (e: Exception) {}

    return null
}

fun Any?.toLocalDateTime(): LocalDateTime {
    if (this == null) return LocalDateTime.now()
    if (this is String) {
        return parseStringToLocalDateTime(this) ?: LocalDateTime.now()
    }
    if (this is Timestamp) {
        val instant = Instant.ofEpochSecond(this.seconds, this.nanoseconds.toLong())
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    }
    return LocalDateTime.now()
}

fun Any?.toLocalDate(): LocalDate? {
    if (this == null) return null
    if (this is String) {
        return parseStringToLocalDateTime(this)?.toLocalDate()
    }
    if (this is Timestamp) {
        val instant = Instant.ofEpochSecond(this.seconds, this.nanoseconds.toLong())
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate()
    }
    return null
}


// ── InventoryItem ───────────────────────────────────────────────────────────

fun InventoryItem.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "category" to category,
        "batchNumber" to batchNumber,
        "buyingPrice" to buyingPrice,
        "sellingPrice" to sellingPrice,
        "quantity" to quantity,       // legacy mobile
        "stock" to quantity,          // desktop sync compatibility
        "unit" to unit,
        "expiryDate" to expiryDate?.toString(),
        "reorderLevel" to reorderLevel,
        "supplierId" to supplierId,
        "requiresPrescription" to requiresPrescription, // legacy mobile
        "prescriptionRequired" to requiresPrescription,  // desktop sync compatibility
        "imageUrl" to imageUrl,
        "createdAt" to createdAt.toString(),
        "updatedAt" to updatedAt.toString()
    )
}

fun Map<String, Any?>.toInventoryItem(docId: String = ""): InventoryItem {
    return InventoryItem(
        id = this["id"] as? String ?: docId,
        name = this["name"] as? String ?: "",
        category = this["category"] as? String ?: "",
        batchNumber = this["batchNumber"] as? String ?: "",
        buyingPrice = (this["buyingPrice"] as? Number)?.toDouble() ?: 0.0,
        sellingPrice = (this["sellingPrice"] as? Number)?.toDouble() ?: 0.0,
        quantity = ((this["stock"] ?: this["quantity"]) as? Number)?.toInt() ?: 0,
        unit = this["unit"] as? String ?: "pcs",
        expiryDate = this["expiryDate"].toLocalDate(),
        reorderLevel = (this["reorderLevel"] as? Number)?.toInt() ?: 10,
        supplierId = this["supplierId"] as? String,
        requiresPrescription = ((this["prescriptionRequired"] ?: this["requiresPrescription"]) as? Boolean) ?: false,
        imageUrl = this["imageUrl"] as? String,
        createdAt = this["createdAt"].toLocalDateTime(),
        updatedAt = this["updatedAt"].toLocalDateTime()
    )
}

// ── Customer ─────────────────────────────────────────────────────────────────

fun Customer.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "phone" to phone,
        "creditBalance" to creditBalance,
        "prescriptionHistory" to prescriptionHistory,
        "createdAt" to createdAt.toString()
    )
}

fun Map<String, Any?>.toCustomer(docId: String = ""): Customer {
    return Customer(
        id = this["id"] as? String ?: docId,
        name = this["name"] as? String ?: "",
        phone = this["phone"] as? String ?: "",
        creditBalance = (this["creditBalance"] as? Number)?.toDouble() ?: 0.0,
        prescriptionHistory = (this["prescriptionHistory"] as? List<*>)?.map { it.toString() } ?: emptyList(),
        createdAt = this["createdAt"].toLocalDateTime()
    )
}

// ── Supplier ─────────────────────────────────────────────────────────────────

fun Supplier.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "phone" to phone,
        "email" to email,
        "address" to address,
        "createdAt" to createdAt.toString()
    )
}

fun Map<String, Any?>.toSupplier(docId: String = ""): Supplier {
    return Supplier(
        id = this["id"] as? String ?: docId,
        name = this["name"] as? String ?: "",
        phone = this["phone"] as? String ?: "",
        email = this["email"] as? String ?: "",
        address = this["address"] as? String ?: "",
        createdAt = this["createdAt"].toLocalDateTime()
    )
}

// ── Expense ──────────────────────────────────────────────────────────────────

fun Expense.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "amount" to amount,
        "category" to category,
        "description" to description,
        "createdAt" to createdAt.toString()
    )
}

fun Map<String, Any?>.toExpense(docId: String = ""): Expense {
    return Expense(
        id = this["id"] as? String ?: docId,
        amount = (this["amount"] as? Number)?.toDouble() ?: 0.0,
        category = this["category"] as? String ?: "",
        description = this["description"] as? String ?: "",
        createdAt = this["createdAt"].toLocalDateTime()
    )
}

// ── Sale & SaleLineItem ───────────────────────────────────────────────────────

fun SaleLineItem.toMap(): Map<String, Any?> {
    return mapOf(
        "itemId" to itemId,
        "name" to name,
        "productName" to name, // desktop compatibility
        "quantity" to quantity,
        "unitPrice" to unitPrice,
        "requiresPrescription" to requiresPrescription,
        "prescriptionRequired" to requiresPrescription // desktop compatibility
    )
}

fun Map<String, Any?>.toSaleLineItem(): SaleLineItem {
    return SaleLineItem(
        itemId = this["itemId"] as? String ?: "",
        name = (this["productName"] ?: this["name"]) as? String ?: "",
        quantity = (this["quantity"] as? Number)?.toInt() ?: 1,
        unitPrice = (this["unitPrice"] as? Number)?.toDouble() ?: 0.0,
        requiresPrescription = ((this["prescriptionRequired"] ?: this["requiresPrescription"]) as? Boolean) ?: false
    )
}

fun Sale.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "items" to items.map { it.toMap() },
        "total" to total,             // legacy subtotal
        "subtotal" to total,          // desktop compatibility subtotal
        "discount" to discount,       // legacy discount
        "discountAmount" to discount, // desktop compatibility discount
        "amountPaid" to amountPaid,
        "totalAmount" to netTotal,    // desktop compatibility total net
        "paymentMethod" to paymentMethod.name,
        "servedBy" to servedBy,
        "customerId" to customerId,
        "prescriptionRef" to prescriptionRef,
        "notes" to notes,
        "createdAt" to createdAt.toString()
    )
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toSale(docId: String = ""): Sale {
    val totalVal = ((this["subtotal"] ?: this["total"]) as? Number)?.toDouble() ?: 0.0
    val discountVal = ((this["discountAmount"] ?: this["discount"]) as? Number)?.toDouble() ?: 0.0
    val netTotalVal = totalVal - discountVal
    return Sale(
        id = this["id"] as? String ?: docId,
        items = (this["items"] as? List<*>)?.map { (it as Map<String, Any?>).toSaleLineItem() } ?: emptyList(),
        total = totalVal,
        discount = discountVal,
        amountPaid = ((this["amountPaid"] ?: this["totalAmount"] ?: netTotalVal) as? Number)?.toDouble() ?: 0.0,
        paymentMethod = try {
            val methodStr = (this["paymentMethod"] as? String)?.uppercase() ?: PaymentMethod.CASH.name
            PaymentMethod.valueOf(methodStr)
        } catch (e: Exception) {
            PaymentMethod.CASH
        },
        servedBy = this["servedBy"] as? String ?: "",
        customerId = this["customerId"] as? String,
        prescriptionRef = this["prescriptionRef"] as? String,
        notes = this["notes"] as? String ?: "",
        createdAt = this["createdAt"].toLocalDateTime()
    )
}

// ── PurchaseOrder & PurchaseOrderItem ────────────────────────────────────────

fun PurchaseOrderItem.toMap(): Map<String, Any?> {
    return mapOf(
        "itemId" to itemId,
        "name" to name,
        "quantity" to quantity,
        "unitCost" to unitCost
    )
}

fun Map<String, Any?>.toPurchaseOrderItem(): PurchaseOrderItem {
    return PurchaseOrderItem(
        itemId = this["itemId"] as? String ?: "",
        name = this["name"] as? String ?: "",
        quantity = (this["quantity"] as? Number)?.toInt() ?: 0,
        unitCost = (this["unitCost"] as? Number)?.toDouble() ?: 0.0
    )
}

fun PurchaseOrder.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "supplierId" to supplierId,
        "supplierName" to supplierName,
        "items" to items.map { it.toMap() },
        "totalCost" to totalCost,
        "status" to status.name,
        "deliveredAt" to deliveredAt?.toString(),
        "createdAt" to createdAt.toString()
    )
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toPurchaseOrder(docId: String = ""): PurchaseOrder {
    return PurchaseOrder(
        id = this["id"] as? String ?: docId,
        supplierId = this["supplierId"] as? String ?: "",
        supplierName = this["supplierName"] as? String ?: "",
        items = (this["items"] as? List<*>)?.map { (it as Map<String, Any?>).toPurchaseOrderItem() } ?: emptyList(),
        totalCost = (this["totalCost"] as? Number)?.toDouble() ?: 0.0,
        status = OrderStatus.valueOf(this["status"] as? String ?: OrderStatus.PENDING.name),
        deliveredAt = (this["deliveredAt"] as? String)?.let { parseStringToLocalDateTime(it) },
        createdAt = this["createdAt"].toLocalDateTime()
    )
}
