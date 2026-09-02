package com.habit.mychemistpos.ui.screens.reports


import java.time.LocalDate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.habit.mychemistpos.ui.components.SyncLifecycleObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.components.ChemistCard
import com.habit.mychemistpos.ui.components.ChemistTopBar
import com.habit.mychemistpos.ui.components.ShimmerCardList
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.DangerTint
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.PrimaryTint
import com.habit.mychemistpos.ui.theme.Success
import com.habit.mychemistpos.ui.theme.SuccessTint
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.util.formatCurrencyCompact
import com.habit.mychemistpos.viewmodel.ReportRange
import com.habit.mychemistpos.viewmodel.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    SyncLifecycleObserver {
        viewModel.sync()
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            val isSyncing by viewModel.isSyncing.collectAsState()
            ChemistTopBar(
                title = "Financial Reports",
                showBack = true,
                onBackClick = onBack,
                actions = {
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
        if (state.isLoading) {
            Column(modifier = Modifier.padding(padding)) {
                ShimmerCardList(3)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Range Filter Tabs
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                    modifier = Modifier.padding(vertical = Dimens.SpacingM)
                ) {
                    val ranges = listOf(
                        ReportRange.LAST_7_DAYS to "7 Days",
                        ReportRange.LAST_30_DAYS to "30 Days",
                        ReportRange.LAST_12_MONTHS to "12 Months"
                    )
                    items(ranges) { (range, label) ->
                        val selected = state.selectedRange == range
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectRange(range) },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
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

                if (state.selectedRange == ReportRange.LAST_7_DAYS) {
                    val weekStart = state.baseDate.minusDays(6)
                    val weekEnd = state.baseDate
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ScreenPadding)
                            .padding(bottom = Dimens.SpacingM),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateWeek(-1) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Previous Week",
                                tint = Primary
                            )
                        }

                        Text(
                            text = "${weekStart.format(formatter)} - ${weekEnd.format(formatter)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = com.habit.mychemistpos.ui.theme.TextPrimary
                        )

                        IconButton(
                            onClick = { viewModel.navigateWeek(1) },
                            enabled = weekEnd.isBefore(LocalDate.now())
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = "Next Week",
                                tint = if (weekEnd.isBefore(LocalDate.now())) Primary else TextHint
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.ScreenPadding)
                        .padding(bottom = Dimens.SpacingXxl),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL)
                ) {
                    // KPI Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
                    ) {
                        ReportKpiCard(
                            title = "Revenue",
                            value = formatCurrencyCompact(state.totalRevenue),
                            icon = Icons.Outlined.AttachMoney,
                            tint = Primary,
                            bg = PrimaryTint,
                            modifier = Modifier.weight(1f)
                        )
                        ReportKpiCard(
                            title = "Expenses",
                            value = formatCurrencyCompact(state.totalExpenses),
                            icon = Icons.Outlined.MoneyOff,
                            tint = Danger,
                            bg = DangerTint,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ReportKpiCard(
                        title = "Net Profit",
                        value = formatCurrencyCompact(state.netProfit),
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        tint = if (state.netProfit >= 0) Success else Danger,
                        bg = if (state.netProfit >= 0) SuccessTint else DangerTint,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Dimens.SpacingS))

                    // Chart Section
                    val rangeTitle = when (state.selectedRange) {
                        ReportRange.LAST_7_DAYS -> "Last 7 Days Comparison"
                        ReportRange.LAST_30_DAYS -> "Last 30 Days Comparison"
                        ReportRange.LAST_12_MONTHS -> "Last 12 Months Comparison"
                    }
                    Text(rangeTitle, style = MaterialTheme.typography.titleMedium)

                    ChemistCard(contentPadding = 16.dp) {
                        Column {
                            // Chart Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                val stats = state.reportStats
                                val maxVal = stats.maxOfOrNull { maxOf(it.revenue, it.expenses) }?.takeIf { it > 0 } ?: 1.0

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    val barGroupWidth = canvasWidth / stats.size
                                    val barSpacing = barGroupWidth * 0.3f
                                    val singleBarWidth = (barGroupWidth - barSpacing) / 2f

                                    // 1. Draw dashed grid lines
                                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    val gridSteps = 4
                                    for (i in 1..gridSteps) {
                                        val y = canvasHeight * (i.toFloat() / gridSteps)
                                        drawLine(
                                            color = Border.copy(alpha = 0.5f),
                                            start = Offset(0f, y),
                                            end = Offset(canvasWidth, y),
                                            pathEffect = pathEffect,
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }

                                    // 2. Draw bars
                                    stats.forEachIndexed { index, stat ->
                                        val groupStartX = index * barGroupWidth + barSpacing / 2f

                                        // Revenue Bar (Teal)
                                        val revHeight = (stat.revenue / maxVal).toFloat() * canvasHeight
                                        if (revHeight > 0) {
                                            drawRoundRect(
                                                color = Primary,
                                                topLeft = Offset(groupStartX, canvasHeight - revHeight),
                                                size = Size(singleBarWidth, revHeight),
                                                cornerRadius = CornerRadius(2.dp.toPx())
                                            )
                                        }

                                        // Expense Bar (Danger Coral)
                                        val expHeight = (stat.expenses / maxVal).toFloat() * canvasHeight
                                        if (expHeight > 0) {
                                            drawRoundRect(
                                                color = Danger,
                                                topLeft = Offset(groupStartX + singleBarWidth, canvasHeight - expHeight),
                                                size = Size(singleBarWidth, expHeight),
                                                cornerRadius = CornerRadius(2.dp.toPx())
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(Dimens.SpacingM))

                            // Labels Row
                            Row(modifier = Modifier.fillMaxWidth()) {
                                state.reportStats.forEachIndexed { index, stat ->
                                    val showLabel = when (state.selectedRange) {
                                        ReportRange.LAST_30_DAYS -> index % 5 == 0 || index == state.reportStats.lastIndex
                                        else -> true
                                    }
                                    Text(
                                        text = if (showLabel) stat.label else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextHint,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Chart Legend
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpacingM),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).size(10.dp).padding(horizontal = 2.dp).align(Alignment.CenterVertically)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = Primary)
                            }
                        }
                        Text("Revenue", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp, end = Dimens.SpacingM))

                        Box(modifier = Modifier.size(10.dp).padding(horizontal = 2.dp).align(Alignment.CenterVertically)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = Danger)
                            }
                        }
                        Text("Expenses", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    ChemistCard(
        modifier = modifier,
        contentPadding = 12.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(Modifier.height(2.dp))
                Text(value, style = MaterialTheme.typography.titleMedium, color = com.habit.mychemistpos.ui.theme.TextPrimary)
            }
        }
    }
}
