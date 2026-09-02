package com.habit.mychemistpos.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habit.mychemistpos.data.model.Customer
import com.habit.mychemistpos.data.model.Expense
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.data.model.OrderStatus
import com.habit.mychemistpos.data.model.PaymentMethod
import com.habit.mychemistpos.data.model.PurchaseOrder
import com.habit.mychemistpos.data.model.PurchaseOrderItem
import com.habit.mychemistpos.data.model.Sale
import com.habit.mychemistpos.data.model.SaleLineItem
import com.habit.mychemistpos.data.model.Supplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Seeds the database with realistic Kenyan pharmacy mock data when tables are empty.
 * Uses onOpen (not onCreate) so it works after destructive migrations too.
 * Inject the [ChemistDatabase] lazily via [databaseProvider] to avoid circular dependency.
 */
class SeedDatabaseCallback(
    private val enableSeeding: Boolean = true,
    private val databaseProvider: () -> ChemistDatabase
) : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        if (!enableSeeding) return
        CoroutineScope(Dispatchers.IO).launch {
            val database = databaseProvider()
            // Only seed if inventory is empty (first launch or after destructive migration)
            val count = db.query("SELECT COUNT(*) FROM inventory_items").use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            }
            if (count == 0) {
                seedAll(database)
            }
        }
    }

    suspend fun seedAll(db: ChemistDatabase) {
        val now = LocalDateTime.now()

        // ── Suppliers ───────────────────────────────────────────────────────
        val suppliers = listOf(
            Supplier("sup_001", "Goodlife Pharmacy Distributors", "0722100200", "orders@goodlife.co.ke", "Mombasa Rd, Nairobi", now.minusDays(90)),
            Supplier("sup_002", "Cosmos Pharma Ltd", "0733200300", "supply@cosmospharma.co.ke", "Industrial Area, Nairobi", now.minusDays(60)),
            Supplier("sup_003", "Dawa Ltd", "0711300400", "info@dawaltd.com", "Baba Dogo Rd, Nairobi", now.minusDays(45)),
            Supplier("sup_004", "PharmAccess Kenya", "0700400500", "procurement@pharmaccess.ke", "Westlands, Nairobi", now.minusDays(30)),
        )
        suppliers.forEach { db.supplierDao().insert(it) }

        // ── Customers ───────────────────────────────────────────────────────
        val customers = listOf(
            Customer("cust_001", "Grace Wanjiku", "0712345678", 0.0, listOf("Amoxicillin", "Paracetamol"), now.minusDays(20)),
            Customer("cust_002", "James Ochieng", "0723456789", 1500.0, listOf("Metformin"), now.minusDays(15)),
            Customer("cust_003", "Fatma Hassan", "0734567890", 0.0, emptyList(), now.minusDays(10)),
            Customer("cust_004", "Peter Kamau", "0745678901", 3200.0, listOf("Losartan", "Omeprazole"), now.minusDays(8)),
            Customer("cust_005", "Aisha Mohamed", "0756789012", 0.0, listOf("Salbutamol Inhaler"), now.minusDays(5)),
            Customer("cust_006", "David Mwangi", "0767890123", 800.0, emptyList(), now.minusDays(3)),
        )
        customers.forEach { db.customerDao().insert(it) }

        // ── Inventory Items ─────────────────────────────────────────────────
        val inventory = listOf(
            // Tablets & Capsules
            InventoryItem("inv_001", "Paracetamol 500mg", "Tablets & Capsules", "BN-2025-001", 150.0, 250.0, 120, "tablets", LocalDate.now().plusMonths(18), 20, "sup_001", false, "preset:pills", now.minusDays(30), now),
            InventoryItem("inv_002", "Amoxicillin 250mg", "Tablets & Capsules", "BN-2025-002", 320.0, 500.0, 45, "capsules", LocalDate.now().plusMonths(12), 15, "sup_001", true, "preset:pills", now.minusDays(28), now),
            InventoryItem("inv_003", "Cetrizine 10mg", "Tablets & Capsules", "BN-2025-003", 80.0, 150.0, 8, "tablets", LocalDate.now().plusDays(15), 10, "sup_002", false, "preset:pills", now.minusDays(25), now),
            InventoryItem("inv_004", "Ibuprofen 400mg", "Tablets & Capsules", "BN-2025-004", 200.0, 350.0, 90, "tablets", LocalDate.now().plusMonths(24), 25, "sup_001", false, "preset:pills", now.minusDays(22), now),
            InventoryItem("inv_005", "Metformin 500mg", "Tablets & Capsules", "BN-2025-005", 250.0, 420.0, 5, "tablets", LocalDate.now().plusMonths(6), 10, "sup_003", true, "preset:pills", now.minusDays(20), now),
            InventoryItem("inv_006", "Omeprazole 20mg", "Tablets & Capsules", "BN-2025-006", 180.0, 300.0, 60, "capsules", LocalDate.now().plusMonths(9), 15, "sup_002", false, "preset:pills", now.minusDays(18), now),
            InventoryItem("inv_007", "Losartan 50mg", "Tablets & Capsules", "BN-2025-007", 300.0, 480.0, 35, "tablets", LocalDate.now().plusMonths(14), 10, "sup_003", true, "preset:pills", now.minusDays(16), now),
            InventoryItem("inv_008", "Doxycycline 100mg", "Tablets & Capsules", "BN-2025-008", 280.0, 450.0, 40, "capsules", LocalDate.now().plusMonths(8), 10, "sup_001", true, "preset:pills", now.minusDays(14), now),

            // Syrups & Liquids
            InventoryItem("inv_009", "Cough Linctus 100ml", "Syrups & Liquids", "BN-2025-009", 120.0, 220.0, 25, "bottles", LocalDate.now().plusMonths(10), 8, "sup_002", false, "preset:liquid", now.minusDays(12), now),
            InventoryItem("inv_010", "Gripe Water 150ml", "Syrups & Liquids", "BN-2025-010", 180.0, 300.0, 18, "bottles", LocalDate.now().plusMonths(7), 5, "sup_004", false, "preset:liquid", now.minusDays(10), now),

            // Creams & Ointments
            InventoryItem("inv_011", "Clotrimazole Cream 20g", "Creams & Ointments", "BN-2025-011", 120.0, 220.0, 3, "tubes", LocalDate.now().minusDays(5), 8, "sup_001", false, "preset:cream", now.minusDays(30), now),
            InventoryItem("inv_012", "Betamethasone Cream 15g", "Creams & Ointments", "BN-2025-012", 200.0, 350.0, 12, "tubes", LocalDate.now().plusMonths(11), 5, "sup_003", true, "preset:cream", now.minusDays(8), now),

            // Inhalers & Sprays
            InventoryItem("inv_013", "Salbutamol Inhaler", "Inhalers & Sprays", "BN-2025-013", 800.0, 1200.0, 7, "pcs", LocalDate.now().plusMonths(15), 5, "sup_003", true, "preset:spray", now.minusDays(20), now),

            // Surgical & First Aid
            InventoryItem("inv_014", "Bandage Roll 10cm", "Surgical & First Aid", "BN-2025-014", 50.0, 100.0, 40, "pcs", null, 10, "sup_004", false, "preset:surgical", now.minusDays(15), now),
            InventoryItem("inv_015", "Surgical Gloves (Box)", "Surgical & First Aid", "BN-2025-015", 350.0, 550.0, 15, "boxes", null, 5, "sup_004", false, "preset:surgical", now.minusDays(10), now),

            // Vitamins & Supplements
            InventoryItem("inv_016", "Vitamin C 1000mg", "Vitamins & Supplements", "BN-2025-016", 350.0, 550.0, 200, "tablets", LocalDate.now().plusMonths(20), 30, "sup_002", false, "preset:pills", now.minusDays(25), now),
            InventoryItem("inv_017", "Folic Acid 5mg", "Vitamins & Supplements", "BN-2025-017", 60.0, 120.0, 150, "tablets", LocalDate.now().plusMonths(16), 20, "sup_002", false, "preset:pills", now.minusDays(22), now),

            // OTC Drugs & General
            InventoryItem("inv_018", "ORS Sachets (Pack of 10)", "OTC Drugs & General", "BN-2025-018", 80.0, 150.0, 50, "packs", LocalDate.now().plusMonths(24), 15, "sup_004", false, "preset:general", now.minusDays(18), now),
        )
        inventory.forEach { db.inventoryDao().insert(it) }

        // ── Sales (today + recent days) ─────────────────────────────────────
        val sales = listOf(
            // Today's sales
            Sale("sale_001",
                items = listOf(
                    SaleLineItem("inv_001", "Paracetamol 500mg", 2, 250.0, false),
                    SaleLineItem("inv_006", "Omeprazole 20mg", 1, 300.0, false),
                ),
                total = 800.0, discount = 0.0, amountPaid = 800.0,
                paymentMethod = PaymentMethod.CASH, servedBy = "Pharmacist",
                customerId = "cust_001", createdAt = now.minusHours(2)
            ),
            Sale("sale_002",
                items = listOf(
                    SaleLineItem("inv_005", "Metformin 500mg", 3, 420.0, true),
                ),
                total = 1260.0, discount = 60.0, amountPaid = 1200.0,
                paymentMethod = PaymentMethod.CASH, servedBy = "Pharmacist",
                customerId = "cust_002", createdAt = now.minusHours(1)
            ),
            Sale("sale_003",
                items = listOf(
                    SaleLineItem("inv_013", "Salbutamol Inhaler", 1, 1200.0, true),
                    SaleLineItem("inv_016", "Vitamin C 1000mg", 1, 550.0, false),
                ),
                total = 1750.0, discount = 0.0, amountPaid = 1750.0,
                paymentMethod = PaymentMethod.CASH, servedBy = "Pharmacist",
                customerId = "cust_005", createdAt = now.minusMinutes(30)
            ),
            // Yesterday
            Sale("sale_004",
                items = listOf(
                    SaleLineItem("inv_004", "Ibuprofen 400mg", 2, 350.0, false),
                    SaleLineItem("inv_009", "Cough Linctus 100ml", 1, 220.0, false),
                ),
                total = 920.0, discount = 20.0, amountPaid = 900.0,
                paymentMethod = PaymentMethod.CASH, servedBy = "Pharmacist",
                createdAt = now.minusDays(1).withHour(14)
            ),
            Sale("sale_005",
                items = listOf(
                    SaleLineItem("inv_007", "Losartan 50mg", 2, 480.0, true),
                ),
                total = 960.0, discount = 0.0, amountPaid = 500.0,
                paymentMethod = PaymentMethod.CREDIT, servedBy = "Pharmacist",
                customerId = "cust_004", createdAt = now.minusDays(1).withHour(10)
            ),
            // 2 days ago
            Sale("sale_006",
                items = listOf(
                    SaleLineItem("inv_002", "Amoxicillin 250mg", 1, 500.0, true),
                    SaleLineItem("inv_001", "Paracetamol 500mg", 3, 250.0, false),
                ),
                total = 1250.0, discount = 50.0, amountPaid = 1200.0,
                paymentMethod = PaymentMethod.CASH, servedBy = "Pharmacist",
                customerId = "cust_001", createdAt = now.minusDays(2).withHour(16)
            ),
            // 3 days ago
            Sale("sale_007",
                items = listOf(
                    SaleLineItem("inv_014", "Bandage Roll 10cm", 5, 100.0, false),
                    SaleLineItem("inv_015", "Surgical Gloves (Box)", 2, 550.0, false),
                ),
                total = 1600.0, discount = 100.0, amountPaid = 1500.0,
                paymentMethod = PaymentMethod.CASH, servedBy = "Pharmacist",
                createdAt = now.minusDays(3).withHour(11)
            ),
            Sale("sale_008",
                items = listOf(
                    SaleLineItem("inv_018", "ORS Sachets (Pack of 10)", 3, 150.0, false),
                    SaleLineItem("inv_010", "Gripe Water 150ml", 1, 300.0, false),
                ),
                total = 750.0, discount = 0.0, amountPaid = 750.0,
                paymentMethod = PaymentMethod.CASH, servedBy = "Pharmacist",
                customerId = "cust_003", createdAt = now.minusDays(3).withHour(9)
            ),
        )
        sales.forEach { db.saleDao().insert(it) }

        // ── Expenses ────────────────────────────────────────────────────────
        val expenses = listOf(
            Expense("exp_001", "Rent", "Monthly shop rent – May 2025", 35000.0, now.minusDays(5)),
            Expense("exp_002", "Utilities", "KPLC electricity bill", 4500.0, now.minusDays(4)),
            Expense("exp_003", "Salaries", "Part-time assistant – week 3", 8000.0, now.minusDays(3)),
            Expense("exp_004", "Transport", "Delivery van fuel", 2500.0, now.minusDays(2)),
            Expense("exp_005", "Supplies", "Receipt printer paper rolls", 1200.0, now.minusDays(1)),
            Expense("exp_006", "Maintenance", "AC unit servicing", 3500.0, now.minusDays(7)),
            Expense("exp_007", "Licensing", "Annual pharmacy board renewal", 15000.0, now.minusDays(14)),
        )
        expenses.forEach { db.expenseDao().insert(it) }

        // ── Purchase Orders ─────────────────────────────────────────────────
        val purchaseOrders = listOf(
            PurchaseOrder("po_001", "sup_001", "Goodlife Pharmacy Distributors",
                items = listOf(
                    PurchaseOrderItem("inv_001", "Paracetamol 500mg", 200, 150.0),
                    PurchaseOrderItem("inv_002", "Amoxicillin 250mg", 100, 320.0),
                ),
                totalCost = 62000.0, status = OrderStatus.DELIVERED,
                deliveredAt = now.minusDays(10), createdAt = now.minusDays(15)
            ),
            PurchaseOrder("po_002", "sup_002", "Cosmos Pharma Ltd",
                items = listOf(
                    PurchaseOrderItem("inv_003", "Cetrizine 10mg", 50, 80.0),
                    PurchaseOrderItem("inv_016", "Vitamin C 1000mg", 100, 350.0),
                ),
                totalCost = 39000.0, status = OrderStatus.DELIVERED,
                deliveredAt = now.minusDays(5), createdAt = now.minusDays(8)
            ),
            PurchaseOrder("po_003", "sup_003", "Dawa Ltd",
                items = listOf(
                    PurchaseOrderItem("inv_005", "Metformin 500mg", 60, 250.0),
                    PurchaseOrderItem("inv_013", "Salbutamol Inhaler", 10, 800.0),
                ),
                totalCost = 23000.0, status = OrderStatus.PENDING,
                createdAt = now.minusDays(2)
            ),
            PurchaseOrder("po_004", "sup_004", "PharmAccess Kenya",
                items = listOf(
                    PurchaseOrderItem("inv_015", "Surgical Gloves (Box)", 30, 350.0),
                ),
                totalCost = 10500.0, status = OrderStatus.PENDING,
                createdAt = now.minusDays(1)
            ),
        )
        purchaseOrders.forEach { db.purchaseOrderDao().insert(it) }
    }
}
