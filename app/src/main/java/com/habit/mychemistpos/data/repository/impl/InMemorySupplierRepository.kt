package com.habit.mychemistpos.data.repository.impl

import com.habit.mychemistpos.data.model.Supplier
import com.habit.mychemistpos.data.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemorySupplierRepository @Inject constructor() : SupplierRepository {

    private val _suppliers = MutableStateFlow(seedSuppliers())

    override fun getAllSuppliers(): Flow<List<Supplier>> = _suppliers

    override fun searchSuppliers(query: String): Flow<List<Supplier>> =
        _suppliers.map { list ->
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.phone.contains(query)
            }
        }

    override suspend fun getSupplierById(id: String): Supplier? =
        _suppliers.value.find { it.id == id }

    override suspend fun addSupplier(supplier: Supplier): Result<String> {
        val id = UUID.randomUUID().toString()
        _suppliers.value = _suppliers.value + supplier.copy(id = id, createdAt = LocalDateTime.now())
        return Result.success(id)
    }

    override suspend fun updateSupplier(supplier: Supplier): Result<Unit> {
        _suppliers.value = _suppliers.value.map { if (it.id == supplier.id) supplier else it }
        return Result.success(Unit)
    }

    override suspend fun deleteSupplier(id: String): Result<Unit> {
        _suppliers.value = _suppliers.value.filter { it.id != id }
        return Result.success(Unit)
    }
}

private fun seedSuppliers(): List<Supplier> {
    val now = LocalDateTime.now()
    return listOf(
        Supplier("sup_001", "PharmaCare Distributors", "0722 123 456", "info@pharmacare.co.ke", "Mombasa Road, Nairobi", now),
        Supplier("sup_002", "MedSupply Kenya Ltd", "0733 987 654", "orders@medsupply.co.ke", "Industrial Area, Nairobi", now),
        Supplier("sup_003", "HealthFirst Wholesale", "0711 555 888", "sales@healthfirst.co.ke", "Likoni Road, Mombasa", now),
    )
}
