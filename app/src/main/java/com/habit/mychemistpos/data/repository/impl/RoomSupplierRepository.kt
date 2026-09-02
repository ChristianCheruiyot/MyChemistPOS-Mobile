package com.habit.mychemistpos.data.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.habit.mychemistpos.data.local.dao.SupplierDao
import com.habit.mychemistpos.data.model.Supplier
import com.habit.mychemistpos.data.repository.SupplierRepository
import com.habit.mychemistpos.util.toMap
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSupplierRepository @Inject constructor(
    private val dao: SupplierDao,
    private val firestore: FirebaseFirestore,
    private val tenantProvider: TenantProvider
) : SupplierRepository {

    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    override fun getAllSuppliers(): Flow<List<Supplier>> = dao.getAll()

    override fun searchSuppliers(query: String): Flow<List<Supplier>> = dao.search(query)

    override suspend fun getSupplierById(id: String): Supplier? = dao.getById(id)

    override suspend fun addSupplier(supplier: Supplier): Result<String> {
        return try {
            val id = if (supplier.id.isBlank()) UUID.randomUUID().toString() else supplier.id
            val copiedSupplier = supplier.copy(id = id, createdAt = LocalDateTime.now())
            dao.insert(copiedSupplier)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("suppliers").document(id)
                    .set(copiedSupplier.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSupplier(supplier: Supplier): Result<Unit> {
        return try {
            dao.update(supplier)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("suppliers").document(supplier.id)
                    .set(supplier.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSupplier(id: String): Result<Unit> {
        return try {
            dao.deleteById(id)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("suppliers").document(id)
                    .delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
