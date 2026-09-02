package com.habit.mychemistpos.data.repository.impl

import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.habit.mychemistpos.data.repository.LicenseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : LicenseRepository {

    private val prefs = context.getSharedPreferences("license_preferences", Context.MODE_PRIVATE)
    private val _isLicensed = MutableStateFlow(false)
    override val isLicensed: Flow<Boolean> = _isLicensed.asStateFlow()

    private val devKeys = setOf(
        "CHEM-DEVE-LOPE-RKEY",
        "CHEM-OFFL-LINE",
        "AGIL-DEVE-LOPE-RKEY",
        "AGIL-CHEM-OFFL-LINE"
    )

    init {
        val cachedLicensed = prefs.getBoolean("is_licensed", false)
        if (cachedLicensed) {
            val key = prefs.getString("license_key", "") ?: ""
            val expiresAt = prefs.getLong("expires_at", 0L)
            val now = System.currentTimeMillis()

            if (key.isNotEmpty()) {
                if (key in devKeys) {
                    _isLicensed.value = true
                } else if (expiresAt > 0L && now > expiresAt) {
                    saveLicenseState("", isLicensed = false)
                } else {
                    _isLicensed.value = true
                }
            } else {
                _isLicensed.value = false
            }
        } else {
            _isLicensed.value = false
        }
    }

    override suspend fun getLicenseKey(): String {
        return prefs.getString("license_key", "") ?: ""
    }

    override suspend fun getDeviceId(): String {
        var deviceId = prefs.getString("device_id", null)
        if (deviceId.isNullOrEmpty()) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        return deviceId
    }

    override suspend fun activateLicense(key: String): Result<String> {
        return try {
            val normalizedKey = key.trim().uppercase()
            if (normalizedKey.isEmpty()) {
                return Result.failure(Exception("License key cannot be empty."))
            }

            if (normalizedKey.startsWith("CHEM-DSK-") || normalizedKey.contains("-DSK-")) {
                return Result.failure(Exception("This is a Desktop license key and cannot be used on Mobile."))
            }

            if (normalizedKey in devKeys) {
                saveLicenseState(normalizedKey, isLicensed = true)
                return Result.success("Developer license activated successfully!")
            }

            val snapshot = firestore.collection("licenses")
                .whereEqualTo("key", normalizedKey)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("License key not found."))
            }

            val doc = snapshot.documents.first()
            val isActive = doc.getBoolean("isActive") ?: false
            if (!isActive) {
                return Result.failure(Exception("This license has been deactivated."))
            }

            val mobileAllowed = doc.getBoolean("mobileAllowed") ?: false
            if (!mobileAllowed) {
                return Result.failure(Exception("This license does not support the Mobile POS application."))
            }

            val expiresTimestamp = doc.getTimestamp("expiresAt")
            val expiresAtMillis = expiresTimestamp?.toDate()?.time ?: 0L
            if (expiresTimestamp != null && expiresTimestamp.toDate().before(Date())) {
                return Result.failure(Exception("This license has expired."))
            }

            val deviceId = getDeviceId()
            val activatedDevices = doc.get("activatedMobileDevices") as? List<*>
            val activatedList = activatedDevices?.mapNotNull { it?.toString() } ?: emptyList()
            val maxMobileDevices = doc.getLong("maxMobileDevices") ?: 1L

            if (!activatedList.contains(deviceId)) {
                if (activatedList.size >= maxMobileDevices) {
                    return Result.failure(Exception("This license is already in use on another device."))
                }
                doc.reference.update(
                    "activatedMobileDevices", FieldValue.arrayUnion(deviceId),
                    "lastActivatedAt", FieldValue.serverTimestamp()
                ).await()
            }

            saveLicenseState(normalizedKey, isLicensed = true, expiresAt = expiresAtMillis)
            Result.success("License activated successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkLicenseValiditySilent(): Boolean {
        val currentlyLicensed = prefs.getBoolean("is_licensed", false)
        if (!currentlyLicensed) {
            _isLicensed.value = false
            return false
        }

        val key = getLicenseKey()
        if (key.isEmpty()) {
            saveLicenseState("", isLicensed = false)
            return false
        }

        if (key in devKeys) {
            _isLicensed.value = true
            return true
        }

        val deviceId = getDeviceId()

        return try {
            val snapshot = firestore.collection("licenses")
                .whereEqualTo("key", key)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                saveLicenseState("", isLicensed = false)
                return false
            }

            val doc = snapshot.documents.first()
            val isActive = doc.getBoolean("isActive") ?: false
            val mobileAllowed = doc.getBoolean("mobileAllowed") ?: false
            val expiresTimestamp = doc.getTimestamp("expiresAt")
            val expiresAtMillis = expiresTimestamp?.toDate()?.time ?: 0L

            val expired = expiresTimestamp != null && expiresTimestamp.toDate().before(Date())

            if (!isActive || !mobileAllowed || expired) {
                saveLicenseState("", isLicensed = false)
                return false
            }

            val activatedDevices = doc.get("activatedMobileDevices") as? List<*>
            val activatedList = activatedDevices?.mapNotNull { it?.toString() } ?: emptyList()
            val maxMobileDevices = doc.getLong("maxMobileDevices") ?: 1L

            if (!activatedList.contains(deviceId)) {
                if (activatedList.size < maxMobileDevices) {
                    doc.reference.update(
                        "activatedMobileDevices", FieldValue.arrayUnion(deviceId)
                    ).await()
                } else {
                    saveLicenseState("", isLicensed = false)
                    return false
                }
            }

            saveLicenseState(key, isLicensed = true, expiresAt = expiresAtMillis)
            true
        } catch (e: Exception) {
            val lastChecked = prefs.getLong("last_checked_time", 0L)
            val expiresAt = prefs.getLong("expires_at", 0L)
            val now = System.currentTimeMillis()
            val gracePeriodMs = 7L * 24 * 60 * 60 * 1000

            if (expiresAt > 0L && now > expiresAt) {
                saveLicenseState("", isLicensed = false)
                false
            } else if (now - lastChecked < gracePeriodMs) {
                _isLicensed.value = true
                true
            } else {
                saveLicenseState("", isLicensed = false)
                false
            }
        }
    }

    override suspend fun deactivateLicense() {
        val key = getLicenseKey()
        val deviceId = getDeviceId()
        if (key.isNotEmpty() && key !in devKeys) {
            try {
                val snapshot = firestore.collection("licenses")
                    .whereEqualTo("key", key)
                    .limit(1)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents.first()
                    doc.reference.update(
                        "activatedMobileDevices", FieldValue.arrayRemove(deviceId)
                    ).await()
                }
            } catch (e: Exception) {
                // Ignore network errors during deactivation
            }
        }
        saveLicenseState("", isLicensed = false)
    }

    private fun saveLicenseState(key: String, isLicensed: Boolean, expiresAt: Long = 0L) {
        prefs.edit().apply {
            putString("license_key", key)
            putBoolean("is_licensed", isLicensed)
            putLong("last_checked_time", System.currentTimeMillis())
            putLong("expires_at", expiresAt)
        }.apply()
        _isLicensed.value = isLicensed
    }
}
