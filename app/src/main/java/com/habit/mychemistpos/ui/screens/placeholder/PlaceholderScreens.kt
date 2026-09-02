package com.habit.mychemistpos.ui.screens.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.components.ChemistTopBar
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextPrimary
import com.habit.mychemistpos.ui.theme.TextSecondary

@Composable
fun PlaceholderScreen(title: String, icon: ImageVector) {
    Scaffold(
        containerColor = PageBackground,
        topBar = { ChemistTopBar(title = title) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, Modifier.size(64.dp), tint = TextHint)
            Spacer(Modifier.height(Dimens.SpacingL))
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextHint)
            Text("Coming in the next build", style = MaterialTheme.typography.bodySmall, color = TextHint)
        }
    }
}

@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
) {
    Scaffold(
        containerColor = PageBackground,
        topBar = { ChemistTopBar(title = "More") }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            MoreMenuItem(Icons.Outlined.LocalShipping, "Suppliers", "Manage your suppliers") {
                onNavigate("suppliers")
            }
            MoreMenuItem(Icons.Outlined.Groups, "Customers", "Customer profiles & credit") {
                onNavigate("customers")
            }
            MoreMenuItem(Icons.Outlined.Payments, "Expenses", "Track daily expenses") {
                onNavigate("expenses")
            }
            MoreMenuItem(Icons.Outlined.Settings, "Settings", "App configuration") {
                onNavigate("settings")
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(Dimens.IconSize), tint = Primary)
        Spacer(Modifier.height(Dimens.SpacingL))
        Column(modifier = Modifier.weight(1f).padding(start = Dimens.SpacingM)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
    HorizontalDivider(thickness = Dimens.DividerThickness, color = Border)
}
