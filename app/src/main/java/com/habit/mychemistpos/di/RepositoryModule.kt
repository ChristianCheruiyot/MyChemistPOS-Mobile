package com.habit.mychemistpos.di

import com.habit.mychemistpos.data.repository.AuthRepository
import com.habit.mychemistpos.data.repository.CustomerRepository
import com.habit.mychemistpos.data.repository.ExpenseRepository
import com.habit.mychemistpos.data.repository.InventoryRepository
import com.habit.mychemistpos.data.repository.PurchaseOrderRepository
import com.habit.mychemistpos.data.repository.SalesRepository
import com.habit.mychemistpos.data.repository.SupplierRepository
import com.habit.mychemistpos.data.repository.LicenseRepository
import com.habit.mychemistpos.data.repository.impl.FirebaseAuthRepository
import com.habit.mychemistpos.data.repository.impl.RoomCustomerRepository
import com.habit.mychemistpos.data.repository.impl.RoomExpenseRepository
import com.habit.mychemistpos.data.repository.impl.RoomInventoryRepository
import com.habit.mychemistpos.data.repository.impl.RoomPurchaseOrderRepository
import com.habit.mychemistpos.data.repository.impl.RoomSalesRepository
import com.habit.mychemistpos.data.repository.impl.RoomSupplierRepository
import com.habit.mychemistpos.data.repository.impl.LicenseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Hilt module that binds repository interfaces to their Room implementations.
 * Auth remains Firebase-backed. All data repos are now backed by Room (offline-first).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLicenseRepository(impl: LicenseRepositoryImpl): LicenseRepository


    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: RoomInventoryRepository): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindSalesRepository(impl: RoomSalesRepository): SalesRepository

    @Binds
    @Singleton
    abstract fun bindSupplierRepository(impl: RoomSupplierRepository): SupplierRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(impl: RoomCustomerRepository): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: RoomExpenseRepository): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindPurchaseOrderRepository(impl: RoomPurchaseOrderRepository): PurchaseOrderRepository
}
