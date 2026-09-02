package com.habit.mychemistpos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Inventory : Screen("inventory")
    data object PointOfSale : Screen("pos")
    data object Receipt : Screen("receipt/{saleId}") {
        fun createRoute(saleId: String) = "receipt/$saleId"
    }
    data object Reports : Screen("reports")
    data object More : Screen("more")
    data object Suppliers : Screen("suppliers")
    data object Customers : Screen("customers")
    data object Expenses : Screen("expenses")
    data object Settings : Screen("settings")
}

enum class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String,
) {
    DASHBOARD(Screen.Dashboard, Icons.Outlined.Receipt, "Sales"),
    INVENTORY(Screen.Inventory, Icons.Outlined.Inventory2, "Inventory"),
    POS(Screen.PointOfSale, Icons.Outlined.ShoppingCart, "New Sale"),
    REPORTS(Screen.Reports, Icons.Outlined.BarChart, "Reports"),
    MORE(Screen.More, Icons.Outlined.MoreHoriz, "More"),
}
