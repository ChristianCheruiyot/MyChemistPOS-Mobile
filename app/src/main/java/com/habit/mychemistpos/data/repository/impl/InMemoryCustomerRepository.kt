package com.habit.mychemistpos.data.repository.impl

import com.habit.mychemistpos.data.model.Customer
import com.habit.mychemistpos.data.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryCustomerRepository @Inject constructor() : CustomerRepository {

    private val _customers = MutableStateFlow(seedCustomers())

    override fun getAllCustomers(): Flow<List<Customer>> = _customers

    override fun searchCustomers(query: String): Flow<List<Customer>> =
        _customers.map { list ->
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.phone.contains(query)
            }
        }

    override suspend fun getCustomerById(id: String): Customer? =
        _customers.value.find { it.id == id }

    override suspend fun addCustomer(customer: Customer): Result<String> {
        val id = UUID.randomUUID().toString()
        _customers.value = _customers.value + customer.copy(id = id, createdAt = LocalDateTime.now())
        return Result.success(id)
    }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> {
        _customers.value = _customers.value.map { if (it.id == customer.id) customer else it }
        return Result.success(Unit)
    }

    override suspend fun deleteCustomer(id: String): Result<Unit> {
        _customers.value = _customers.value.filter { it.id != id }
        return Result.success(Unit)
    }

    override suspend fun updateCreditBalance(id: String, amount: Double): Result<Unit> {
        _customers.value = _customers.value.map {
            if (it.id == id) it.copy(creditBalance = it.creditBalance + amount) else it
        }
        return Result.success(Unit)
    }

    override suspend fun addPrescriptionRef(id: String, prescriptionRef: String): Result<Unit> {
        _customers.value = _customers.value.map {
            if (it.id == id) it.copy(prescriptionHistory = it.prescriptionHistory + prescriptionRef) else it
        }
        return Result.success(Unit)
    }
}

private fun seedCustomers(): List<Customer> {
    val now = LocalDateTime.now()
    return listOf(
        Customer("cust_001", "John Mwangi", "0722 111 222", 500.0, listOf("RX-2026-0038", "RX-2026-0042"), now),
        Customer("cust_002", "Faith Wanjiku", "0733 333 444", 0.0, listOf("RX-2026-0039"), now),
        Customer("cust_003", "Peter Odhiambo", "0711 555 666", 1200.0, emptyList(), now),
    )
}
