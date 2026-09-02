package com.habit.mychemistpos.data.repository

import com.habit.mychemistpos.data.model.Supplier
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    fun getAllSuppliers(): Flow<List<Supplier>>
    fun searchSuppliers(query: String): Flow<List<Supplier>>

    suspend fun getSupplierById(id: String): Supplier?
    suspend fun addSupplier(supplier: Supplier): Result<String>
    suspend fun updateSupplier(supplier: Supplier): Result<Unit>
    suspend fun deleteSupplier(id: String): Result<Unit>
}
