package com.habit.mychemistpos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.habit.mychemistpos.data.local.dao.CustomerDao
import com.habit.mychemistpos.data.local.dao.ExpenseDao
import com.habit.mychemistpos.data.local.dao.InventoryDao
import com.habit.mychemistpos.data.local.dao.PurchaseOrderDao
import com.habit.mychemistpos.data.local.dao.SaleDao
import com.habit.mychemistpos.data.local.dao.SupplierDao
import com.habit.mychemistpos.data.model.Customer
import com.habit.mychemistpos.data.model.Expense
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.data.model.PurchaseOrder
import com.habit.mychemistpos.data.model.Sale
import com.habit.mychemistpos.data.model.Supplier

@Database(
    entities = [
        InventoryItem::class,
        Sale::class,
        Customer::class,
        Supplier::class,
        Expense::class,
        PurchaseOrder::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ChemistDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao
}
