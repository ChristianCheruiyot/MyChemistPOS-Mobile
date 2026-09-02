package com.habit.mychemistpos.ui.screens.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.data.model.InventoryFilter
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.ui.components.BadgeType
import com.habit.mychemistpos.ui.components.ChemistCard
import com.habit.mychemistpos.ui.components.ChemistTextField
import com.habit.mychemistpos.ui.components.ChemistTopBar
import com.habit.mychemistpos.ui.components.ConfirmDialog
import com.habit.mychemistpos.ui.components.EmptyState
import com.habit.mychemistpos.ui.components.ProductImage
import com.habit.mychemistpos.ui.components.ShimmerCardList
import com.habit.mychemistpos.ui.components.StatusBadge
import com.habit.mychemistpos.ui.components.SyncLifecycleObserver
import com.habit.mychemistpos.ui.components.stockBadgeType
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.Surface
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.util.expiryLabel
import com.habit.mychemistpos.util.formatCurrencyCompact
import com.habit.mychemistpos.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    viewModel: InventoryViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var searchExpanded by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }

    SyncLifecycleObserver {
        viewModel.sync()
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            ChemistTopBar(
                title = "Inventory",
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
                            Icon(Icons.Default.Sync, "Sync", tint = Primary)
                        }
                    }
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Outlined.ViewList else Icons.Outlined.GridView,
                            contentDescription = if (isGridView) "Show List" else "Show Grid",
                            tint = Primary
                        )
                    }
                    IconButton(onClick = { searchExpanded = !searchExpanded }) {
                        Icon(Icons.Outlined.Search, "Search", tint = Primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddSheet,
                containerColor = Primary,
                contentColor = Surface
            ) {
                Icon(Icons.Outlined.Add, "Add Item")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            AnimatedVisibility(visible = searchExpanded) {
                ChemistTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchChange,
                    label = "Search",
                    placeholder = "Name, category, or batch...",
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextHint) },
                    modifier = Modifier.padding(
                        horizontal = Dimens.ScreenPadding,
                        vertical = Dimens.SpacingM
                    )
                )
            }

            // Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                modifier = Modifier.padding(vertical = Dimens.SpacingM)
            ) {
                val filters = listOf(
                    InventoryFilter.ALL to "All Items",
                    InventoryFilter.LOW_STOCK to "Low Stock",
                    InventoryFilter.EXPIRING to "Expiring Soon",
                    InventoryFilter.PRESCRIPTION to "Rx Only"
                )
                items(filters) { (filter, label) ->
                    val selected = state.activeFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onFilterChange(filter) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Surface,
                            containerColor = Surface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = Primary,
                            borderWidth = 1.dp
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.ChipCorner)
                    )
                }
            }

            // Content
            when {
                state.isLoading -> ShimmerCardList()
                state.items.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.Inventory2,
                        title = "No Items Found",
                        subtitle = if (state.searchQuery.isNotEmpty()) "Try a different search term" else "Click + to add your first item"
                    )
                }
                else -> {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = Dimens.ScreenPadding,
                                end = Dimens.ScreenPadding,
                                bottom = 80.dp // Space for FAB
                            ),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
                            verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                InventoryItemGridCard(
                                    item = item,
                                    onClick = { viewModel.openEditSheet(item) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = Dimens.ScreenPadding,
                                end = Dimens.ScreenPadding,
                                bottom = 80.dp // Space for FAB
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                InventoryItemCard(
                                    item = item,
                                    onClick = { viewModel.openEditSheet(item) },
                                    onDelete = { viewModel.requestDelete(item) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add/Edit Sheet
        if (state.showAddEditSheet) {
            AddEditItemSheet(
                item = state.editingItem,
                categories = state.categories,
                onDismiss = viewModel::closeSheet,
                onSave = viewModel::saveItem,
                onDelete = { item ->
                    viewModel.closeSheet()
                    viewModel.requestDelete(item)
                }
            )
        }

        // Delete Confirmation
        if (state.showDeleteConfirm) {
            ConfirmDialog(
                title = "Delete Item",
                message = "Are you sure you want to delete ${state.itemToDelete?.name}? This action cannot be undone.",
                confirmText = "Delete",
                onConfirm = viewModel::confirmDelete,
                onDismiss = viewModel::cancelDelete
            )
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ChemistCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(
                imageUrl = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(Dimens.CardCorner)
            )
            Spacer(Modifier.width(Dimens.SpacingM))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                Text(
                    text = "${item.category} • Batch: ${item.batchNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(Dimens.SpacingS))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = "${item.quantity} ${item.unit}",
                        type = stockBadgeType(item.quantity, item.reorderLevel)
                    )
                    if (item.requiresPrescription) {
                        Spacer(Modifier.padding(horizontal = Dimens.SpacingS))
                        StatusBadge(text = "Rx", type = BadgeType.WARNING)
                    }
                }
                
                if (item.expiryDate != null) {
                    Spacer(Modifier.height(Dimens.SpacingS))
                    Text(
                        text = item.expiryDate.expiryLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.isExpiringSoon || item.isExpired) Danger else TextHint
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = Dimens.SpacingM)
            ) {
                Text(
                    text = formatCurrencyCompact(item.sellingPrice),
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                Text(
                    text = "Cost: ${formatCurrencyCompact(item.buyingPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
                Spacer(Modifier.height(Dimens.SpacingM))
                Row {
                    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Edit, "Edit", modifier = Modifier.size(20.dp), tint = TextSecondary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, "Delete", modifier = Modifier.size(20.dp), tint = Danger)
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryItemGridCard(
    item: InventoryItem,
    onClick: () -> Unit
) {
    ChemistCard(
        onClick = onClick,
        contentPadding = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
            ) {
                ProductImage(
                    imageUrl = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp)
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
            }
            
            Spacer(Modifier.height(6.dp))
            
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(2.dp))
            
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(6.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = "${item.quantity} ${item.unit}",
                    type = stockBadgeType(item.quantity, item.reorderLevel)
                )
                
                Text(
                    text = formatCurrencyCompact(item.sellingPrice),
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary
                )
            }
            
            if (item.expiryDate != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.expiryDate.expiryLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.isExpiringSoon || item.isExpired) Danger else TextHint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
