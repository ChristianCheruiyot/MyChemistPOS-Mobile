package com.habit.mychemistpos.ui.screens.suppliers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.data.model.Supplier
import com.habit.mychemistpos.ui.components.ChemistCard
import com.habit.mychemistpos.ui.components.ChemistTopBar
import com.habit.mychemistpos.ui.components.EmptyState
import com.habit.mychemistpos.ui.components.ShimmerCardList
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.Surface
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.viewmodel.SupplierViewModel

@Composable
fun SuppliersScreen(
    viewModel: SupplierViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            ChemistTopBar(title = "Suppliers", showBack = true, onBackClick = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddSheet,
                containerColor = Primary,
                contentColor = Surface
            ) {
                Icon(Icons.Outlined.Add, "Add Supplier")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> ShimmerCardList()
                state.suppliers.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.LocalShipping,
                        title = "No Suppliers Yet",
                        subtitle = "Tap + to add your first supplier"
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = Dimens.ScreenPadding,
                            end = Dimens.ScreenPadding,
                            top = Dimens.ScreenPadding,
                            bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.suppliers, key = { it.id }) { supplier ->
                            SupplierCard(
                                supplier = supplier,
                                onEdit = { viewModel.openEditSheet(supplier) },
                                onDelete = { viewModel.deleteSupplier(supplier) }
                            )
                        }
                    }
                }
            }
        }

        if (state.showAddEditSheet) {
            AddEditSupplierSheet(
                supplier = state.editingSupplier,
                onDismiss = viewModel::closeSheet,
                onSave = {
                    viewModel.saveSupplier(it)
                    viewModel.closeSheet()
                }
            )
        }
    }
}

@Composable
private fun SupplierCard(
    supplier: Supplier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ChemistCard(onClick = onEdit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(supplier.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Dimens.SpacingXs))
                if (supplier.phone.isNotEmpty()) {
                    Text(supplier.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                if (supplier.email.isNotEmpty()) {
                    Text(supplier.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                if (supplier.address.isNotEmpty()) {
                    Text(supplier.address, style = MaterialTheme.typography.bodySmall, color = TextHint)
                }
            }
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, "Edit", modifier = Modifier.size(20.dp), tint = TextSecondary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, "Delete", modifier = Modifier.size(20.dp), tint = Danger)
                }
            }
        }
    }
}
