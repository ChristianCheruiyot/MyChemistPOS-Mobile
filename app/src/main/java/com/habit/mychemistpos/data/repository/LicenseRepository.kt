package com.habit.mychemistpos.data.repository

import kotlinx.coroutines.flow.Flow

interface LicenseRepository {
    val isLicensed: Flow<Boolean>
    suspend fun getLicenseKey(): String
    suspend fun getDeviceId(): String
    suspend fun activateLicense(key: String): Result<String>
    suspend fun checkLicenseValiditySilent(): Boolean
    suspend fun deactivateLicense()
}

