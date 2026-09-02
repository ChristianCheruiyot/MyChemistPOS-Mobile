package com.habit.mychemistpos.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.habit.mychemistpos.data.local.ChemistDatabase
import com.habit.mychemistpos.data.local.SeedDatabaseCallback
import com.habit.mychemistpos.data.local.dao.CustomerDao
import com.habit.mychemistpos.data.local.dao.ExpenseDao
import com.habit.mychemistpos.data.local.dao.InventoryDao
import com.habit.mychemistpos.data.local.dao.PurchaseOrderDao
import com.habit.mychemistpos.data.local.dao.SaleDao
import com.habit.mychemistpos.data.local.dao.SupplierDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    // ── Firebase ─────────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): com.google.firebase.firestore.FirebaseFirestore =
        com.google.firebase.firestore.FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
        CredentialManager.create(context)

    // ── Room Database ────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChemistDatabase {
        lateinit var database: ChemistDatabase
        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        database = Room.databaseBuilder(
            context,
            ChemistDatabase::class.java,
            "chemist_pos.db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .addCallback(SeedDatabaseCallback(enableSeeding = isDebug) { database })
        .build()
        return database
    }

    // ── DAOs ─────────────────────────────────────────────────────────────────
    @Provides fun provideInventoryDao(db: ChemistDatabase): InventoryDao = db.inventoryDao()
    @Provides fun provideSaleDao(db: ChemistDatabase): SaleDao = db.saleDao()
    @Provides fun provideCustomerDao(db: ChemistDatabase): CustomerDao = db.customerDao()
    @Provides fun provideSupplierDao(db: ChemistDatabase): SupplierDao = db.supplierDao()
    @Provides fun provideExpenseDao(db: ChemistDatabase): ExpenseDao = db.expenseDao()
    @Provides fun providePurchaseOrderDao(db: ChemistDatabase): PurchaseOrderDao = db.purchaseOrderDao()
}
