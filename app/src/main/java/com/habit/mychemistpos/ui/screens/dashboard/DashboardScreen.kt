package com.habit.mychemistpos.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.components.BadgeType
import com.habit.mychemistpos.ui.components.ChemistCard
import com.habit.mychemistpos.ui.components.ChemistTopBar
import com.habit.mychemistpos.ui.components.ErrorState
import com.habit.mychemistpos.ui.components.ShimmerCardList
import com.habit.mychemistpos.ui.components.StatusBadge
import com.habit.mychemistpos.ui.components.SyncLifecycleObserver
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.PrimaryTint
import com.habit.mychemistpos.ui.theme.Success
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextPrimary
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.util.formatCurrency
import com.habit.mychemistpos.util.formatCurrencyCompact
import com.habit.mychemistpos.util.formatTime
import com.habit.mychemistpos.util.relativeLabel
import com.habit.mychemistpos.viewmodel.DashboardViewModel
import java.time.LocalTime

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToInventory: () -> Unit = {},
    onNavigateToPOS: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToReceipt: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    // Trigger sync automatically when screen is resumed/navigated to
    SyncLifecycleObserver {
        viewModel.sync()
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            ChemistTopBar(
                title = "Sales",
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
        when {
            state.isLoading -> ShimmerCardList()
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = viewModel::loadDashboard
            )
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenPadding,
                        end = Dimens.ScreenPadding,
                        top = Dimens.SpacingM,
                        bottom = Dimens.SpacingXxl
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // ── Greeting ──────────────────────────────────────────────
                    item {
                        val greeting = when (LocalTime.now().hour) {
                            in 0..11 -> "Good morning"
                            in 12..16 -> "Good afternoon"
                            else -> "Good evening"
                        }
                        Text(
                            text = "$greeting, ${state.userName} 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (state.businessName.isNotEmpty()) {
                            Text(
                                text = state.businessName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(Modifier.height(Dimens.SpacingM))
                    }

                    // ── Quick Actions ─────────────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                        ) {
                            QuickActionChip(
                                label = "New Sale",
                                icon = Icons.Outlined.ShoppingCart,
                                onClick = onNavigateToPOS,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionChip(
                                label = "Add Stock",
                                icon = Icons.Outlined.Add,
                                onClick = onNavigateToInventory,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionChip(
                                label = "Reports",
                                icon = Icons.Outlined.BarChart,
                                onClick = onNavigateToReports,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(Dimens.SpacingL))
                    }

                    // ── Sales History Header ─────────────────────────────────
                    item {
                        Text(
                            text = "Sales History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(Dimens.SpacingS))
                    }

                    // ── Sales History List ────────────────────────────────────
                    if (state.recentSales.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No sales recorded yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextHint
                                )
                            }
                        }
                    } else {
                        item {
                            ChemistCard(contentPadding = 0.dp) {
                                state.recentSales.forEachIndexed { index, sale ->
                                    // Build a concise item summary: "Paracetamol ×2, Amoxicillin ×1"
                                    val itemSummary = sale.items.joinToString(", ") { lineItem ->
                                        "${lineItem.name} ×${lineItem.quantity}"
                                    }
                                    val totalQty = sale.items.sumOf { it.quantity }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateToReceipt(sale.id) }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Left column: items sold + time
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = itemSummary,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "${sale.createdAt.toLocalDate().relativeLabel()} · ${sale.createdAt.formatTime()} · $totalQty item${if (totalQty != 1) "s" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextHint
                                            )
                                        }

                                        Spacer(Modifier.width(Dimens.SpacingM))

                                        // Right column: amount + discount indicator
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = formatCurrency(sale.netTotal),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Success
                                            )
                                            if (sale.discount > 0) {
                                                Text(
                                                    text = "-${formatCurrencyCompact(sale.discount)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Danger
                                                )
                                            }
                                        }
                                    }

                                    if (index < state.recentSales.lastIndex) {
                                        HorizontalDivider(
                                            thickness = Dimens.DividerThickness,
                                            color = Border,
                                            modifier = Modifier.padding(horizontal = 14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Low Stock Alerts ──────────────────────────────────────
                    if (state.lowStockItems.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(Dimens.SpacingL))
                            Text(
                                text = "Low Stock Alerts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(Dimens.SpacingS))
                        }
                        item {
                            ChemistCard(contentPadding = 0.dp) {
                                state.lowStockItems.forEachIndexed { i, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${item.quantity} ${item.unit} remaining",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextHint
                                            )
                                        }
                                        StatusBadge(
                                            text = if (item.quantity == 0) "Out" else "Low",
                                            type = if (item.quantity == 0) BadgeType.DANGER else BadgeType.WARNING
                                        )
                                    }
                                    if (i < state.lowStockItems.lastIndex) {
                                        HorizontalDivider(
                                            thickness = Dimens.DividerThickness,
                                            color = Border,
                                            modifier = Modifier.padding(horizontal = 14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(Dimens.SpacingXl)) }
                }
            }
        }
    }
}

// ─── Compact Quick Action Chip ─────────────────────────────────────────────────
@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PrimaryTint)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = Primary)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
    }
}
