package com.habit.mychemistpos.ui.screens.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.components.ChemistPrimaryButton
import com.habit.mychemistpos.ui.components.ChemistSecondaryButton
import com.habit.mychemistpos.ui.components.ChemistTopBar
import com.habit.mychemistpos.ui.components.ErrorState
import com.habit.mychemistpos.ui.components.ShimmerCardList
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.PrimaryEnd
import androidx.compose.material3.TextButton
import com.habit.mychemistpos.ui.theme.Surface
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.util.formatCurrency
import com.habit.mychemistpos.viewmodel.ReceiptViewModel
import java.time.format.DateTimeFormatter

@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel,
    onNavigateHome: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PageBackground,
        topBar = { ChemistTopBar("Transaction Success", showBack = false) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.isLoading -> ShimmerCardList(1)
                state.error != null -> ErrorState(state.error!!, onNavigateHome)
                state.sale != null -> {
                    val sale = state.sale!!
                    Spacer(Modifier.height(Dimens.SpacingXxl))
                    
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(64.dp),
                        tint = PrimaryEnd
                    )
                    Spacer(Modifier.height(Dimens.SpacingS))
                    Text(
                        "Payment Successful",
                        style = MaterialTheme.typography.headlineSmall,
                        color = PrimaryEnd
                    )
                    
                    Spacer(Modifier.height(Dimens.SpacingXl))

                    // Receipt Paper
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ScreenPadding)
                            .background(Surface, RoundedCornerShape(Dimens.CardCorner))
                            .border(Dimens.BorderWidth, Border, RoundedCornerShape(Dimens.CardCorner))
                            .padding(Dimens.SpacingXl)
                    ) {
                        Text(
                            text = state.businessName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(Dimens.SpacingXs))
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        Text(
                            text = sale.createdAt.format(formatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(Dimens.SpacingS))
                        Text(
                            text = "Receipt #${sale.id.takeLast(6).uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextHint,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(Dimens.SpacingL))
                        DashedDivider()
                        Spacer(Modifier.height(Dimens.SpacingL))

                        sale.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.SpacingS),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                    Text("${item.quantity} x ${formatCurrency(item.unitPrice)}", style = MaterialTheme.typography.bodySmall, color = TextHint)
                                }
                                Text(formatCurrency(item.subtotal), style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.height(Dimens.SpacingM))
                        DashedDivider()
                        Spacer(Modifier.height(Dimens.SpacingM))

                        if (sale.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(formatCurrency(sale.total), style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text("-${formatCurrency(sale.discount)}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(Dimens.SpacingS))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium)
                            Text(formatCurrency(sale.netTotal), style = MaterialTheme.typography.titleLarge, color = Primary)
                        }

                        Spacer(Modifier.height(Dimens.SpacingL))
                        DashedDivider()
                        Spacer(Modifier.height(Dimens.SpacingL))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid (${sale.paymentMethod.displayName})", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(formatCurrency(sale.amountPaid), style = MaterialTheme.typography.bodyMedium)
                        }
                        if (sale.change > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Change", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(formatCurrency(sale.change), style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.height(Dimens.SpacingXl))
                        Text(
                            "Thank you for your business!",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = TextHint
                        )
                        Text(
                            "Served by: ${sale.servedBy}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = TextHint
                        )
                    }

                    Spacer(Modifier.height(Dimens.SpacingXxl))
                    
                    Column(
                        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
                    ) {
                        ChemistPrimaryButton(
                            text = "New Sale",
                            onClick = onNavigateHome
                        )
                        ChemistSecondaryButton(
                            text = "Print Receipt",
                            onClick = { /* Print integration later */ }
                        )
                        TextButton(
                            onClick = onNavigateHome,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", color = Primary)
                        }
                    }
                    Spacer(Modifier.height(Dimens.SpacingXxl))
                }
            }
        }
    }
}

@Composable
fun DashedDivider(
    color: Color = Border,
    thickness: Float = 2f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = thickness,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
    )
}
