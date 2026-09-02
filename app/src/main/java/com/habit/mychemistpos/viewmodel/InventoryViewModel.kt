package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.model.InventoryFilter
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.data.repository.InventoryRepository
import com.habit.mychemistpos.data.repository.impl.FirestoreSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<InventoryItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: InventoryFilter = InventoryFilter.ALL,
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val editingItem: InventoryItem? = null,
    val showAddEditSheet: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val itemToDelete: InventoryItem? = null,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val syncManager: FirestoreSyncManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

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

    private var allItemsList: List<InventoryItem> = emptyList()

    init { loadItems() }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.getAllItems().collect { allItems ->
                    allItemsList = allItems
                    val filtered = applyFilters(allItems)
                    val defaultCategories = listOf(
                        "Tablets & Capsules",
                        "Syrups & Liquids",
                        "Injections & Infusions",
                        "Creams & Ointments",
                        "Inhalers & Sprays",
                        "Surgical & First Aid",
                        "Medical Devices & Equipment",
                        "Vitamins & Supplements",
                        "OTC Drugs & General"
                    )
                    val cats = (defaultCategories + allItems.map { it.category })
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        items = filtered,
                        categories = cats,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Failed to load inventory"
                )
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refreshFiltered()
    }

    fun onFilterChange(filter: InventoryFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter, selectedCategory = null)
        refreshFiltered()
    }

    fun onCategorySelect(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        refreshFiltered()
    }

    fun openAddSheet() {
        _uiState.value = _uiState.value.copy(editingItem = null, showAddEditSheet = true, saveSuccess = false)
    }

    fun openEditSheet(item: InventoryItem) {
        _uiState.value = _uiState.value.copy(editingItem = item, showAddEditSheet = true, saveSuccess = false)
    }

    fun closeSheet() {
        _uiState.value = _uiState.value.copy(showAddEditSheet = false, editingItem = null, saveSuccess = false)
    }

    fun requestDelete(item: InventoryItem) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true, itemToDelete = item)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false, itemToDelete = null)
    }

    fun confirmDelete() {
        val item = _uiState.value.itemToDelete ?: return
        viewModelScope.launch {
            repository.deleteItem(item.id)
            _uiState.value = _uiState.value.copy(showDeleteConfirm = false, itemToDelete = null)
        }
    }

    fun saveItem(item: InventoryItem) {
        viewModelScope.launch {
            val isEdit = _uiState.value.editingItem != null
            if (isEdit) {
                repository.updateItem(item)
            } else {
                repository.addItem(item)
            }
            _uiState.value = _uiState.value.copy(saveSuccess = true)
        }
    }

    private fun refreshFiltered() {
        _uiState.value = _uiState.value.copy(items = applyFilters(allItemsList))
    }

    private fun applyFilters(items: List<InventoryItem>): List<InventoryItem> {
        val state = _uiState.value
        var result = items
        val q = state.searchQuery.trim().lowercase()

        // Search
        if (q.isNotEmpty()) {
            result = result.filter {
                it.name.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.batchNumber.lowercase().contains(q)
            }
        }

        // Filter
        result = when (state.activeFilter) {
            InventoryFilter.ALL -> result
            InventoryFilter.LOW_STOCK -> result.filter { it.isLowStock }
            InventoryFilter.EXPIRING -> result.filter { it.isExpiringSoon }
            InventoryFilter.EXPIRED -> result.filter { it.isExpired }
            InventoryFilter.PRESCRIPTION -> result.filter { it.requiresPrescription }
        }

        // Category
        if (state.selectedCategory != null) {
            result = result.filter { it.category == state.selectedCategory }
        }

        return if (q.isNotEmpty()) {
            result.sortedWith(
                compareBy<InventoryItem> {
                    if (it.name.lowercase().startsWith(q)) 0 else 1
                }.thenBy {
                    if (it.category.lowercase().startsWith(q)) 0 else 1
                }.thenBy {
                    if (it.batchNumber.lowercase().startsWith(q)) 0 else 1
                }.thenBy {
                    it.name.lowercase()
                }
            )
        } else {
            result.sortedBy { it.name.lowercase() }
        }
    }
}
