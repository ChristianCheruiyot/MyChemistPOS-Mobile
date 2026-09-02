package com.habit.mychemistpos.ui.screens.pos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.habit.mychemistpos.ui.components.SyncLifecycleObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.data.model.PaymentMethod
import com.habit.mychemistpos.data.model.SaleLineItem
import com.habit.mychemistpos.ui.components.BadgeType
import com.habit.mychemistpos.ui.components.ChemistCard
import com.habit.mychemistpos.ui.components.ChemistPrimaryButton
import com.habit.mychemistpos.ui.components.ChemistTextField
import com.habit.mychemistpos.ui.components.ChemistTopBar
import com.habit.mychemistpos.ui.components.EmptyState
import com.habit.mychemistpos.ui.components.ProductImage
import com.habit.mychemistpos.ui.components.StatusBadge
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.Success
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextPrimary
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.util.formatCurrency
import com.habit.mychemistpos.util.formatCurrencyCompact
import com.habit.mychemistpos.viewmodel.POSViewModel

@Composable
fun PointOfSaleScreen(
    viewModel: POSViewModel,
    onNavigateToReceipt: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Browse Products, 1 = Checkout Cart

    if (state.receiptSaleId != null) {
        onNavigateToReceipt(state.receiptSaleId!!)
        viewModel.resetPOS()
        selectedTab = 0
    }

    SyncLifecycleObserver {
        viewModel.sync()
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            ChemistTopBar(
                title = "New Sale",
                actions = {
                    val isSyncing by viewModel.isSyncing.collectAsState()
                    IconButton(onClick = viewModel::forceSync) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = Primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segmented Tab Switcher (iOS style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingS)
                    .background(Color(0xFFE0F2FE).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                // Products Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) Primary else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Browse Products",
                        color = if (selectedTab == 0) Color.White else TextSecondary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Cart Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) Primary else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val totalQty = state.cartItems.sumOf { it.quantity }
                    Text(
                        text = if (totalQty > 0) "Cart ($totalQty)" else "Cart",
                        color = if (selectedTab == 1) Color.White else TextSecondary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (selectedTab == 0) {
                // ── BROWSE PRODUCTS TAB ──────────────────────────────────────────
                
                // Search Bar
                Box(modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingS)) {
                    ChemistTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        label = "Search Product",
                        placeholder = "Type name or batch...",
                        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextHint) }
                    )
                }

                // Category Chips Selector
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                    modifier = Modifier.padding(bottom = Dimens.SpacingM)
                ) {
                    items(state.categories) { category ->
                        val selected = state.selectedCategory == category
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = com.habit.mychemistpos.ui.theme.Surface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = Primary,
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(Dimens.ChipCorner)
                        )
                    }
                }

                // Catalog Grid
                val filteredProducts = state.filteredProducts
                Box(modifier = Modifier.weight(1f)) {
                    if (filteredProducts.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.ShoppingCart,
                            title = "No Products Found",
                            subtitle = if (state.searchQuery.isNotBlank()) "Try checking the spelling or query" else "Add items in the Inventory tab first"
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = Dimens.ScreenPadding,
                                end = Dimens.ScreenPadding,
                                bottom = if (state.cartItems.isNotEmpty()) 80.dp else Dimens.ScreenPadding
                            ),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
                            verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredProducts, key = { it.id }) { item ->
                                val cartQty = state.cartItems.find { it.itemId == item.id }?.quantity ?: 0
                                POSProductCard(
                                    item = item,
                                    cartQuantity = cartQty,
                                    onClick = { viewModel.addToCart(item) }
                                )
                            }
                        }
                    }

                    // Floating Review Cart bar at bottom
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        AnimatedVisibility(
                            visible = state.cartItems.isNotEmpty()
                        ) {
                            Surface(
                                color = Primary,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTab = 1 }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.ShoppingCart, null, tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "${state.cartItems.sumOf { it.quantity }} items added",
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Checkout: ${formatCurrencyCompact(state.total)} →",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

            } else {
                // ── CHECKOUT CART TAB ─────────────────────────────────────────────
                Box(modifier = Modifier.weight(1f)) {
                    if (state.cartItems.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.ShoppingCart,
                            title = "Cart is Empty",
                            subtitle = "Browse products and tap cards to start a sale"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = Dimens.ScreenPadding, 
                                end = Dimens.ScreenPadding, 
                                bottom = Dimens.ScreenPadding
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                        ) {
                            // Cart List items
                            items(state.cartItems, key = { it.itemId }) { item ->
                                // Look up actual product quantity limit
                                val stockQty = state.allProducts.find { it.id == item.itemId }?.quantity ?: 1
                                CartItemRow(
                                    item = item,
                                    onIncrease = {
                                        if (item.quantity < stockQty) {
                                            viewModel.updateQuantity(item.itemId, item.quantity + 1)
                                        }
                                    },
                                    onDecrease = { viewModel.updateQuantity(item.itemId, item.quantity - 1) },
                                    onRemove = { viewModel.removeFromCart(item.itemId) }
                                )
                            }

                            item { Spacer(Modifier.height(Dimens.SpacingL)) }

                            // Checkout panel
                            item {
                                ChemistCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                        Text(formatCurrency(state.subtotal), style = MaterialTheme.typography.titleMedium)
                                    }
                                    Spacer(Modifier.height(Dimens.SpacingS))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Discount", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
                                        ChemistTextField(
                                            value = if (state.discount == 0.0) "" else state.discount.toString(),
                                            onValueChange = { viewModel.updateDiscount(it.toDoubleOrNull() ?: 0.0) },
                                            label = "",
                                            placeholder = "0.00",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                                            leadingIcon = { Text("KES", color = TextHint, modifier = Modifier.padding(start = 12.dp)) },
                                            modifier = Modifier.width(130.dp)
                                        )
                                    }
                                    
                                    Spacer(Modifier.height(Dimens.SpacingS))
                                    HorizontalDivider(color = Border)
                                    Spacer(Modifier.height(Dimens.SpacingS))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Total", style = MaterialTheme.typography.titleLarge)
                                        Text(formatCurrency(state.total), style = MaterialTheme.typography.displaySmall, color = Primary)
                                    }

                                    Spacer(Modifier.height(Dimens.SpacingM))

                                    // Payment Methods
                                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
                                        PaymentMethodChip(
                                            method = PaymentMethod.CASH,
                                            selected = state.paymentMethod == PaymentMethod.CASH,
                                            onClick = { viewModel.setPaymentMethod(PaymentMethod.CASH) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        PaymentMethodChip(
                                            method = PaymentMethod.CREDIT,
                                            selected = state.paymentMethod == PaymentMethod.CREDIT,
                                            onClick = { viewModel.setPaymentMethod(PaymentMethod.CREDIT) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    if (state.paymentMethod == PaymentMethod.CASH) {
                                        Spacer(Modifier.height(Dimens.SpacingM))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Amount Paid", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
                                            ChemistTextField(
                                                value = if (state.amountPaid == 0.0) "" else state.amountPaid.toString(),
                                                onValueChange = { viewModel.updateAmountPaid(it.toDoubleOrNull() ?: 0.0) },
                                                label = "",
                                                placeholder = "0.00",
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                                                leadingIcon = { Text("KES", color = TextHint, modifier = Modifier.padding(start = 12.dp)) },
                                                modifier = Modifier.width(130.dp)
                                            )
                                        }
                                        if (state.change > 0) {
                                            Spacer(Modifier.height(Dimens.SpacingS))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Change to give", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                                Text(formatCurrency(state.change), style = MaterialTheme.typography.titleMedium, color = Danger)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(Dimens.SpacingL))
                                    
                                    ChemistPrimaryButton(
                                        text = if (state.paymentMethod == PaymentMethod.CREDIT) "Complete Credit Sale" else "Complete Sale",
                                        onClick = viewModel::processSale,
                                        enabled = state.canCheckout,
                                        isLoading = state.isProcessing
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun POSProductCard(
    item: InventoryItem,
    cartQuantity: Int,
    onClick: () -> Unit
) {
    ChemistCard(
        onClick = onClick,
        contentPadding = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                ProductImage(
                    imageUrl = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(6.dp)
                )
                if (item.requiresPrescription) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        StatusBadge(text = "Rx", type = BadgeType.WARNING)
                    }
                }
                if (cartQuantity > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Success, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "x$cartQuantity",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(6.dp))
            
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "${item.quantity} ${item.unit} in stock",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.quantity <= item.reorderLevel) Danger else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = formatCurrencyCompact(item.sellingPrice),
                style = MaterialTheme.typography.titleSmall,
                color = Primary
            )
        }
    }
}

@Composable
private fun CartItemRow(
    item: SaleLineItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    ChemistCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.requiresPrescription) {
                    Spacer(Modifier.height(4.dp))
                    StatusBadge("Rx Required", BadgeType.WARNING)
                }
                Text(formatCurrencyCompact(item.unitPrice), style = MaterialTheme.typography.bodySmall, color = TextHint)
            }
            
            // Quantity Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(PageBackground, RoundedCornerShape(Dimens.ButtonCorner))
            ) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Remove, "Decrease", modifier = Modifier.size(16.dp))
                }
                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Add, "Increase", modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(Modifier.width(Dimens.SpacingM))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrencyCompact(item.subtotal), style = MaterialTheme.typography.titleSmall, color = Primary)
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Delete, "Remove", modifier = Modifier.size(18.dp), tint = Danger)
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f, label = "ChipScale")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .background(
                if (selected) Primary else PageBackground,
                RoundedCornerShape(Dimens.ButtonCorner)
            )
            .padding(vertical = Dimens.SpacingM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = method.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) Color.White else TextPrimary
        )
    }
}
