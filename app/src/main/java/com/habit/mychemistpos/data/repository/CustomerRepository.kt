package com.habit.mychemistpos.data.repository

import com.habit.mychemistpos.data.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    fun searchCustomers(query: String): Flow<List<Customer>>

    suspend fun getCustomerById(id: String): Customer?
    suspend fun addCustomer(customer: Customer): Result<String>
    suspend fun updateCustomer(customer: Customer): Result<Unit>
    suspend fun deleteCustomer(id: String): Result<Unit>
    suspend fun updateCreditBalance(id: String, amount: Double): Result<Unit>
    suspend fun addPrescriptionRef(id: String, prescriptionRef: String): Result<Unit>
}
