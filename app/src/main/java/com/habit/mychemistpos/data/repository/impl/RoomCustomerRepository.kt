package com.habit.mychemistpos.data.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.habit.mychemistpos.data.local.dao.CustomerDao
import com.habit.mychemistpos.data.model.Customer
import com.habit.mychemistpos.data.repository.CustomerRepository
import com.habit.mychemistpos.util.toMap
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCustomerRepository @Inject constructor(
    private val dao: CustomerDao,
    private val firestore: FirebaseFirestore,
    private val tenantProvider: TenantProvider
) : CustomerRepository {

    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    override fun getAllCustomers(): Flow<List<Customer>> = dao.getAll()

    override fun searchCustomers(query: String): Flow<List<Customer>> = dao.search(query)

    override suspend fun getCustomerById(id: String): Customer? = dao.getById(id)

    override suspend fun addCustomer(customer: Customer): Result<String> {
        return try {
            val id = if (customer.id.isBlank()) UUID.randomUUID().toString() else customer.id
            val copiedCustomer = customer.copy(id = id, createdAt = LocalDateTime.now())
            dao.insert(copiedCustomer)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("customers").document(id)
                    .set(copiedCustomer.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> {
        return try {
            dao.update(customer)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("customers").document(customer.id)
                    .set(customer.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomer(id: String): Result<Unit> {
        return try {
            dao.deleteById(id)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("customers").document(id)
                    .delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCreditBalance(id: String, amount: Double): Result<Unit> {
        return try {
            dao.updateCreditBalance(id, amount)
            
            val updated = dao.getById(id)
            if (updated != null && userId != null) {
                val tenantId = tenantProvider.getTenantId(userId!!)
                firestore.collection("businesses").document(tenantId)
                    .collection("customers").document(id)
                    .set(updated.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addPrescriptionRef(id: String, prescriptionRef: String): Result<Unit> {
        return try {
            val customer = dao.getById(id) ?: return Result.failure(Exception("Customer not found"))
            val updated = customer.copy(
                prescriptionHistory = customer.prescriptionHistory + prescriptionRef
            )
            dao.update(updated)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("customers").document(id)
                    .set(updated.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
