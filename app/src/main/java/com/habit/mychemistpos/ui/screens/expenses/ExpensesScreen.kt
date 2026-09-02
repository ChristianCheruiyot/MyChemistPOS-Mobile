package com.habit.mychemistpos.ui.screens.expenses

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
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
import com.habit.mychemistpos.data.model.Expense
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
import com.habit.mychemistpos.util.formatCurrency
import com.habit.mychemistpos.viewmodel.ExpenseViewModel
import java.time.format.DateTimeFormatter

@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            ChemistTopBar(title = "Expenses", showBack = true, onBackClick = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddSheet,
                containerColor = Primary,
                contentColor = Surface
            ) {
                Icon(Icons.Outlined.Add, "Add Expense")
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
                state.expenses.isEmpty() -> {
                    EmptyState(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = "No Expenses Recorded",
                        subtitle = "Tap + to record an expense"
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
                        items(state.expenses, key = { it.id }) { expense ->
                            ExpenseCard(
                                expense = expense,
                                onEdit = { viewModel.openEditSheet(expense) },
                                onDelete = { viewModel.deleteExpense(expense) }
                            )
                        }
                    }
                }
            }
        }

        if (state.showAddEditSheet) {
            AddEditExpenseSheet(
                expense = state.editingExpense,
                onDismiss = viewModel::closeSheet,
                onSave = {
                    viewModel.saveExpense(it)
                    viewModel.closeSheet()
                }
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
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
                Text(expense.category, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Dimens.SpacingXs))
                Text(expense.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(Dimens.SpacingXs))
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                Text(expense.createdAt.format(formatter), style = MaterialTheme.typography.labelSmall, color = TextHint)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatCurrency(expense.amount), style = MaterialTheme.typography.titleMedium, color = Danger)
                Spacer(Modifier.width(Dimens.SpacingS))
                Column {
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
}
