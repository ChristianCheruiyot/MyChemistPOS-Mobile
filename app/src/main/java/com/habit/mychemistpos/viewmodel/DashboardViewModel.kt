package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.data.model.Sale
import com.habit.mychemistpos.data.repository.AuthRepository
import com.habit.mychemistpos.data.repository.InventoryRepository
import com.habit.mychemistpos.data.repository.SalesRepository
import com.habit.mychemistpos.data.repository.impl.FirestoreSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userName: String = "",
    val businessName: String = "",
    val todaySalesTotal: Double = 0.0,
    val todayTransactionCount: Int = 0,
    val lowStockCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val lowStockItems: List<InventoryItem> = emptyList(),
    val expiringSoonItems: List<InventoryItem> = emptyList(),
    val recentSales: List<Sale> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val inventoryRepository: InventoryRepository,
    private val salesRepository: SalesRepository,
    private val syncManager: FirestoreSyncManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing

    fun sync() {
        viewModelScope.launch {
            syncManager.syncAllFromCloud(force = false)
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            syncManager.syncAllFromCloud(force = true)
        }
    }

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = authRepository.getCurrentUser()
                val userName = user?.name?.split(" ")?.firstOrNull() ?: "there"
                val businessName = user?.businessName ?: ""

                _uiState.value = _uiState.value.copy(
                    userName = userName,
                    businessName = businessName
                )

                // Collect low stock items reactively
                launch {
                    inventoryRepository.getLowStockItems().collect { lowStock ->
                        _uiState.value = _uiState.value.copy(
                            lowStockCount = lowStock.size,
                            lowStockItems = lowStock.take(5)
                        )
                    }
                }

                // Collect expiring items reactively
                launch {
                    inventoryRepository.getExpiringSoonItems().collect { expiring ->
                        _uiState.value = _uiState.value.copy(
                            expiringSoonCount = expiring.size,
                            expiringSoonItems = expiring.take(5)
                        )
                    }
                }

                // Collect all sales reactively
                launch {
                    salesRepository.getAllSales().collect { allSales ->
                        val today = java.time.LocalDate.now()
                        val todaySalesList = allSales.filter { sale ->
                            sale.createdAt.toLocalDate().isEqual(today)
                        }
                        val todayTotal = todaySalesList.sumOf { it.netTotal }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            todaySalesTotal = todayTotal,
                            todayTransactionCount = todaySalesList.size,
                            recentSales = allSales.sortedByDescending { it.createdAt }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }
}
