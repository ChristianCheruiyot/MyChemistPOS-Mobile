package com.habit.mychemistpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.data.model.PaymentMethod
import com.habit.mychemistpos.data.model.Sale
import com.habit.mychemistpos.data.model.SaleLineItem
import com.habit.mychemistpos.data.repository.AuthRepository
import com.habit.mychemistpos.data.repository.InventoryRepository
import com.habit.mychemistpos.data.repository.SalesRepository
import com.habit.mychemistpos.data.repository.impl.FirestoreSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class POSUiState(
    val cartItems: List<SaleLineItem> = emptyList(),
    val searchQuery: String = "",
    val allProducts: List<InventoryItem> = emptyList(),
    val selectedCategory: String = "All",
    val discount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val amountPaid: Double = 0.0,
    val isProcessing: Boolean = false,
    val receiptSaleId: String? = null, // Set when sale succeeds to show receipt
) {
    val subtotal: Double get() = cartItems.sumOf { it.subtotal }
    val total: Double get() = subtotal - discount
    val change: Double get() = if (amountPaid > total) amountPaid - total else 0.0
    val canCheckout: Boolean get() = cartItems.isNotEmpty() && (paymentMethod == PaymentMethod.CREDIT || amountPaid >= total)

    // Filter products based on search and category
    val filteredProducts: List<InventoryItem> get() {
        val q = searchQuery.trim().lowercase()
        val filtered = allProducts.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            val matchesSearch = q.isEmpty() || 
                item.name.contains(q, ignoreCase = true) || 
                item.category.contains(q, ignoreCase = true) || 
                item.batchNumber.contains(q, ignoreCase = true)
            matchesCategory && matchesSearch && item.quantity > 0
        }
        return if (q.isNotEmpty()) {
            filtered.sortedWith(
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
            filtered.sortedBy { it.name.lowercase() }
        }
    }

    // Dynamic unique categories listing
    val categories: List<String> get() {
        return listOf("All") + allProducts.map { it.category }.distinct().filter { it.isNotBlank() }.sorted()
    }
}

@HiltViewModel
class POSViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val salesRepository: SalesRepository,
    private val authRepository: AuthRepository,
    private val syncManager: FirestoreSyncManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(POSUiState())
    val uiState: StateFlow<POSUiState> = _uiState.asStateFlow()

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
        // Collect inventory items in real-time
        viewModelScope.launch {
            inventoryRepository.getAllItems().collect { items ->
                _uiState.value = _uiState.value.copy(allProducts = items)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun addToCart(item: InventoryItem) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.itemId == item.id }

        if (existingIndex != -1) {
            val existing = currentCart[existingIndex]
            // check stock limit
            if (existing.quantity < item.quantity) {
                currentCart[existingIndex] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            currentCart.add(
                SaleLineItem(
                    itemId = item.id,
                    name = item.name,
                    quantity = 1,
                    unitPrice = item.sellingPrice,
                    requiresPrescription = item.requiresPrescription
                )
            )
        }
        
        _uiState.value = _uiState.value.copy(
            cartItems = currentCart,
            amountPaid = currentCart.sumOf { it.subtotal } - _uiState.value.discount // auto-fill amount paid
        )
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(itemId)
            return
        }
        val currentCart = _uiState.value.cartItems.map {
            if (it.itemId == itemId) it.copy(quantity = newQuantity) else it
        }
        _uiState.value = _uiState.value.copy(
            cartItems = currentCart,
            amountPaid = currentCart.sumOf { it.subtotal } - _uiState.value.discount
        )
    }

    fun removeFromCart(itemId: String) {
        val currentCart = _uiState.value.cartItems.filter { it.itemId != itemId }
        _uiState.value = _uiState.value.copy(
            cartItems = currentCart,
            amountPaid = currentCart.sumOf { it.subtotal } - _uiState.value.discount
        )
    }

    fun updateDiscount(discount: Double) {
        _uiState.value = _uiState.value.copy(
            discount = discount,
            amountPaid = _uiState.value.subtotal - discount
        )
    }

    fun updateAmountPaid(amount: Double) {
        _uiState.value = _uiState.value.copy(amountPaid = amount)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun processSale() {
        if (!_uiState.value.canCheckout) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            val user = authRepository.getCurrentUser()
            val state = _uiState.value
            
            val sale = Sale(
                items = state.cartItems,
                total = state.subtotal,
                discount = state.discount,
                amountPaid = state.amountPaid,
                paymentMethod = state.paymentMethod,
                servedBy = user?.name ?: "Unknown",
                createdAt = LocalDateTime.now()
            )
            
            val result = salesRepository.addSale(sale)
            result.onSuccess { saleId ->
                // Deduct inventory
                val deductions = state.cartItems.associate { it.itemId to it.quantity }
                inventoryRepository.deductStock(deductions)
                
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    receiptSaleId = saleId
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    fun resetPOS() {
        _uiState.value = POSUiState()
    }
}
