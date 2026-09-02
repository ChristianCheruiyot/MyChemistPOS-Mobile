package com.habit.mychemistpos.data.repository.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.habit.mychemistpos.data.local.dao.InventoryDao
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.data.repository.InventoryRepository
import com.habit.mychemistpos.util.toMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomInventoryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: InventoryDao,
    private val firestore: FirebaseFirestore,
    private val tenantProvider: TenantProvider
) : InventoryRepository {

    companion object {
        private const val TAG = "RoomInventoryRepo"
    }

    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    private suspend fun uploadAndCompressImage(tenantId: String, itemId: String, contentUriString: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "uploadAndCompressImage: starting for item=$itemId, uri=$contentUriString")
            val uri = Uri.parse(contentUriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "uploadAndCompressImage: Failed to open InputStream for uri=$contentUriString")
                return@withContext null
            }
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                Log.e(TAG, "uploadAndCompressImage: BitmapFactory.decodeStream returned null for uri=$contentUriString")
                return@withContext null
            }
            Log.d(TAG, "uploadAndCompressImage: decoded bitmap ${bitmap.width}x${bitmap.height}")
            
            val maxSide = 512
            val (newWidth, newHeight) = if (bitmap.width > bitmap.height) {
                val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()
                Pair(maxSide, (maxSide * ratio).toInt())
            } else {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                Pair((maxSide * ratio).toInt(), maxSide)
            }
            val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
            val data = outputStream.toByteArray()
            Log.d(TAG, "uploadAndCompressImage: compressed to ${data.size} bytes")
            
            val storageRef = FirebaseStorage.getInstance().reference
                .child("businesses").child(tenantId).child("products").child("$itemId.jpg")
            
            Log.d(TAG, "uploadAndCompressImage: uploading to Firebase Storage path=${storageRef.path}")
            storageRef.putBytes(data).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "uploadAndCompressImage: SUCCESS, downloadUrl=$downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "uploadAndCompressImage: FAILED for item=$itemId", e)
            null
        }
    }

    override fun getAllItems(): Flow<List<InventoryItem>> = dao.getAll()

    override fun searchItems(query: String): Flow<List<InventoryItem>> = dao.search(query)

    override fun getItemsByCategory(category: String): Flow<List<InventoryItem>> =
        dao.getByCategory(category)

    override fun getLowStockItems(): Flow<List<InventoryItem>> = dao.getLowStock()

    override fun getExpiringSoonItems(withinDays: Int): Flow<List<InventoryItem>> {
        return dao.getAll().map { items ->
            val today = LocalDate.now()
            items.filter { item ->
                val exp = item.expiryDate ?: return@filter false
                !exp.isBefore(today) && exp.isBefore(today.plusDays(withinDays.toLong()))
            }
        }
    }

    override fun getExpiredItems(): Flow<List<InventoryItem>> {
        return dao.getAll().map { items ->
            val today = LocalDate.now()
            items.filter { it.expiryDate?.isBefore(today) == true }
        }
    }

    override suspend fun getItemById(id: String): InventoryItem? = dao.getById(id)

    override suspend fun addItem(item: InventoryItem): Result<String> {
        return try {
            val id = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id
            val now = LocalDateTime.now()
            
            Log.d(TAG, "addItem: incoming imageUrl=${item.imageUrl}, userId=$userId")
            var finalImageUrl = item.imageUrl
            val uid = userId
            if (uid != null) {
                val tenantId = tenantProvider.getTenantId(uid)
                val rawUrl = item.imageUrl
                if (rawUrl != null && rawUrl.startsWith("content://")) {
                    finalImageUrl = uploadAndCompressImage(tenantId, id, rawUrl)
                    Log.d(TAG, "addItem: after upload, finalImageUrl=$finalImageUrl")
                }
            } else {
                Log.w(TAG, "addItem: userId is null, skipping image upload")
            }

            val copiedItem = item.copy(id = id, imageUrl = finalImageUrl, createdAt = now, updatedAt = now)
            Log.d(TAG, "addItem: saving item id=$id with imageUrl=${copiedItem.imageUrl}")
            dao.insert(copiedItem)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("products").document(id)
                    .set(copiedItem.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItem(item: InventoryItem): Result<Unit> {
        return try {
            val now = LocalDateTime.now()
            Log.d(TAG, "updateItem: incoming imageUrl=${item.imageUrl}, userId=$userId")
            var finalImageUrl = item.imageUrl
            val uid = userId
            if (uid != null) {
                val tenantId = tenantProvider.getTenantId(uid)
                val rawUrl = item.imageUrl
                if (rawUrl != null && rawUrl.startsWith("content://")) {
                    finalImageUrl = uploadAndCompressImage(tenantId, item.id, rawUrl)
                    Log.d(TAG, "updateItem: after upload, finalImageUrl=$finalImageUrl")
                }
            } else {
                Log.w(TAG, "updateItem: userId is null, skipping image upload")
            }

            val copiedItem = item.copy(imageUrl = finalImageUrl, updatedAt = now)
            Log.d(TAG, "updateItem: saving item id=${item.id} with imageUrl=${copiedItem.imageUrl}")
            dao.update(copiedItem)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("products").document(copiedItem.id)
                    .set(copiedItem.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            dao.deleteById(id)
            
            userId?.let { uid ->
                val tenantId = tenantProvider.getTenantId(uid)
                firestore.collection("businesses").document(tenantId)
                    .collection("products").document(id)
                    .delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(id: String, newQuantity: Int): Result<Unit> {
        return try {
            dao.updateQuantity(id, newQuantity, LocalDateTime.now().toString())
            
            val updatedItem = dao.getById(id)
            if (updatedItem != null && userId != null) {
                val tenantId = tenantProvider.getTenantId(userId!!)
                firestore.collection("businesses").document(tenantId)
                    .collection("products").document(id)
                    .set(updatedItem.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deductStock(items: Map<String, Int>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            var hasUpdates = false
            val tenantId = userId?.let { tenantProvider.getTenantId(it) }

            for ((itemId, deductQty) in items) {
                val current = dao.getById(itemId) ?: continue
                val newQty = (current.quantity - deductQty).coerceAtLeast(0)
                val now = LocalDateTime.now()
                
                // 1. Update local Room DB
                dao.updateQuantity(itemId, newQty, now.toString())
                
                // 2. Add Firestore update to the batch
                if (tenantId != null) {
                    val docRef = firestore.collection("businesses").document(tenantId)
                        .collection("products").document(itemId)
                    val updatedItem = current.copy(quantity = newQty, updatedAt = now)
                    
                    batch.set(docRef, updatedItem.toMap(), com.google.firebase.firestore.SetOptions.merge())
                    hasUpdates = true
                }
            }

            // 3. Commit batch atomically
            if (hasUpdates) {
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
