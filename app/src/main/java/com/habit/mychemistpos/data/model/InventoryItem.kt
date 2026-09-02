package com.habit.mychemistpos.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "",
    val batchNumber: String = "",
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val quantity: Int = 0,
    val unit: String = "pcs",
    val expiryDate: LocalDate? = null,
    val reorderLevel: Int = 10,
    val supplierId: String? = null,
    val requiresPrescription: Boolean = false,
    val imageUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    /** Profit margin per unit */
    val profitMargin: Double
        get() = if (buyingPrice > 0) sellingPrice - buyingPrice else 0.0

    /** Profit margin percentage */
    val profitMarginPercent: Double
        get() = if (buyingPrice > 0) ((sellingPrice - buyingPrice) / buyingPrice) * 100.0 else 0.0

    /** Total stock value at selling price */
    val stockValue: Double
        get() = sellingPrice * quantity

    /** Whether stock is at or below reorder level */
    val isLowStock: Boolean
        get() = quantity <= reorderLevel

    /** Whether item has expired */
    val isExpired: Boolean
        get() = expiryDate?.isBefore(LocalDate.now()) == true

    /** Whether item expires within 30 days */
    val isExpiringSoon: Boolean
        get() {
            val exp = expiryDate ?: return false
            val today = LocalDate.now()
            return !exp.isBefore(today) && exp.isBefore(today.plusDays(30))
        }
}

/** Inventory filter categories */
enum class InventoryFilter {
    ALL,
    LOW_STOCK,
    EXPIRING,
    EXPIRED,
    PRESCRIPTION
}

/** Common pharmaceutical categories */
val MedicineCategories = listOf(
    "Analgesics",
    "Antibiotics",
    "Antihistamines",
    "Antifungals",
    "Cardiovascular",
    "Cough & Cold",
    "Dermatology",
    "Diabetes",
    "Gastrointestinal",
    "Nutritional Supplements",
    "Ophthalmic",
    "Respiratory",
    "Surgical Supplies",
    "Topical",
    "Vitamins",
    "Other"
)
