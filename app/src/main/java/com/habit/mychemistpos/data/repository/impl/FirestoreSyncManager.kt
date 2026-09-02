package com.habit.mychemistpos.data.repository.impl

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.habit.mychemistpos.data.local.ChemistDatabase
import com.habit.mychemistpos.util.*
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: ChemistDatabase,
    private val firestore: FirebaseFirestore,
    private val tenantProvider: TenantProvider
) {
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private var lastSyncExecutionTime = 0L

    suspend fun syncAllFromCloud(force: Boolean = false): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("User is not authenticated"))

        _isSyncing.value = true
        return try {
            val tenantId = tenantProvider.getTenantId(uid)
            val tenantDoc = firestore.collection("businesses").document(tenantId)
            val prefs = context.getSharedPreferences("sync_preferences", Context.MODE_PRIVATE)

            // 1. Fetch updated/new records from Firestore based on previous sync timestamps
            
            // Products (query by updatedAt)
            val lastProductsSync = prefs.getString("last_sync_products", "") ?: ""
            var productsQuery = tenantDoc.collection("products") as com.google.firebase.firestore.Query
            if (lastProductsSync.isNotEmpty()) {
                productsQuery = productsQuery.whereGreaterThan("updatedAt", lastProductsSync)
            }
            val inventoryItems = productsQuery.get().await().documents.mapNotNull { doc ->
                doc.data?.toInventoryItem(doc.id)
            }

            // Customers (query by createdAt)
            val lastCustomersSync = prefs.getString("last_sync_customers", "") ?: ""
            var customersQuery = tenantDoc.collection("customers") as com.google.firebase.firestore.Query
            if (lastCustomersSync.isNotEmpty()) {
                customersQuery = customersQuery.whereGreaterThan("createdAt", lastCustomersSync)
            }
            val customers = customersQuery.get().await().documents.mapNotNull { doc ->
                doc.data?.toCustomer(doc.id)
            }

            // Suppliers (query by createdAt)
            val lastSuppliersSync = prefs.getString("last_sync_suppliers", "") ?: ""
            var suppliersQuery = tenantDoc.collection("suppliers") as com.google.firebase.firestore.Query
            if (lastSuppliersSync.isNotEmpty()) {
                suppliersQuery = suppliersQuery.whereGreaterThan("createdAt", lastSuppliersSync)
            }
            val suppliers = suppliersQuery.get().await().documents.mapNotNull { doc ->
                doc.data?.toSupplier(doc.id)
            }

            // Expenses (query by createdAt)
            val lastExpensesSync = prefs.getString("last_sync_expenses", "") ?: ""
            var expensesQuery = tenantDoc.collection("expenses") as com.google.firebase.firestore.Query
            if (lastExpensesSync.isNotEmpty()) {
                expensesQuery = expensesQuery.whereGreaterThan("createdAt", lastExpensesSync)
            }
            val expenses = expensesQuery.get().await().documents.mapNotNull { doc ->
                doc.data?.toExpense(doc.id)
            }

            // Sales/Transactions (query by createdAt)
            val lastSalesSync = prefs.getString("last_sync_sales", "") ?: ""
            var salesQuery = tenantDoc.collection("transactions") as com.google.firebase.firestore.Query
            if (lastSalesSync.isNotEmpty()) {
                salesQuery = salesQuery.whereGreaterThan("createdAt", lastSalesSync)
            }
            val sales = salesQuery.get().await().documents.mapNotNull { doc ->
                doc.data?.toSale(doc.id)
            }

            // Purchase Orders (query by createdAt)
            val lastPOSync = prefs.getString("last_sync_purchase_orders", "") ?: ""
            var poQuery = tenantDoc.collection("purchase_orders") as com.google.firebase.firestore.Query
            if (lastPOSync.isNotEmpty()) {
                poQuery = poQuery.whereGreaterThan("createdAt", lastPOSync)
            }
            val purchaseOrders = poQuery.get().await().documents.mapNotNull { doc ->
                doc.data?.toPurchaseOrder(doc.id)
            }

            // 2. Perform local database write inside a Room suspending transaction
            database.withTransaction {
                // Insert all synced records (Room REPLACE strategy will merge/overwrite existing)
                inventoryItems.forEach { database.inventoryDao().insert(it) }
                customers.forEach { database.customerDao().insert(it) }
                suppliers.forEach { database.supplierDao().insert(it) }
                expenses.forEach { database.expenseDao().insert(it) }
                sales.forEach { database.saleDao().insert(it) }
                purchaseOrders.forEach { database.purchaseOrderDao().insert(it) }
            }

            // 3. Save new sync cursors (maximum timestamps of the successfully synced records)
            prefs.edit().apply {
                if (inventoryItems.isNotEmpty()) {
                    inventoryItems.maxOfOrNull { it.updatedAt.toString() }?.let { putString("last_sync_products", it) }
                }
                if (customers.isNotEmpty()) {
                    customers.maxOfOrNull { it.createdAt.toString() }?.let { putString("last_sync_customers", it) }
                }
                if (suppliers.isNotEmpty()) {
                    suppliers.maxOfOrNull { it.createdAt.toString() }?.let { putString("last_sync_suppliers", it) }
                }
                if (expenses.isNotEmpty()) {
                    expenses.maxOfOrNull { it.createdAt.toString() }?.let { putString("last_sync_expenses", it) }
                }
                if (sales.isNotEmpty()) {
                    sales.maxOfOrNull { it.createdAt.toString() }?.let { putString("last_sync_sales", it) }
                }
                if (purchaseOrders.isNotEmpty()) {
                    purchaseOrders.maxOfOrNull { it.createdAt.toString() }?.let { putString("last_sync_purchase_orders", it) }
                }
            }.apply()

            lastSyncExecutionTime = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    fun clearSyncTimestamps() {
        val prefs = context.getSharedPreferences("sync_preferences", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        lastSyncExecutionTime = 0L
    }
}
