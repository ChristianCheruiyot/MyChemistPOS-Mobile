package com.habit.mychemistpos.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantProvider @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private var cachedTenantId: String? = null

    /**
     * Resolves the [tenantId] linked to the user's account under /users/{uid}.
     * If not found, a new UUID-based tenantId will be generated, uploaded, and cached.
     */
    suspend fun getTenantId(uid: String): String {
        cachedTenantId?.let { return it }

        return try {
            val docRef = firestore.collection("users").document(uid)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val tenantId = snapshot.getString("tenantId")
                if (!tenantId.isNullOrEmpty()) {
                    cachedTenantId = tenantId
                    return tenantId
                }
            }

            // Generate and save a new one if not found in Firestore
            val newTenantId = java.util.UUID.randomUUID().toString()
            docRef.set(mapOf(
                "uid" to uid,
                "tenantId" to newTenantId,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )).await()

            cachedTenantId = newTenantId
            newTenantId
        } catch (e: Exception) {
            // Fallback to generating a temporary cached local ID if network fails to avoid blocking offline database writes
            val fallbackId = java.util.UUID.randomUUID().toString()
            cachedTenantId = fallbackId
            fallbackId
        }
    }

    fun clearCache() {
        cachedTenantId = null
    }
}
