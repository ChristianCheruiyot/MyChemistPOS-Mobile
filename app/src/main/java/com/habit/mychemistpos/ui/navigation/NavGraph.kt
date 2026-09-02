package com.habit.mychemistpos.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habit.mychemistpos.ui.screens.auth.LoginScreen
import com.habit.mychemistpos.ui.screens.dashboard.DashboardScreen
import com.habit.mychemistpos.ui.screens.placeholder.MoreScreen
import com.habit.mychemistpos.ui.screens.placeholder.PlaceholderScreen
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.viewmodel.AuthViewModel
import com.habit.mychemistpos.viewmodel.DashboardViewModel

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel = hiltViewModel(),
    licenseViewModel: com.habit.mychemistpos.viewmodel.LicenseViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val licenseState by licenseViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    when {
        licenseState.isCheckingLicense -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = com.habit.mychemistpos.ui.theme.Primary
                )
            }
        }
        !licenseState.isLicensed -> {
            com.habit.mychemistpos.ui.screens.auth.LicenseScreen(viewModel = licenseViewModel)
        }
        authState.isCheckingAuth -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = com.habit.mychemistpos.ui.theme.Primary
                )
            }
        }
        authState.isLoggedIn -> {
            MainScaffold(navController = navController, authViewModel = authViewModel)
        }
        else -> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { /* State-driven, no manual nav needed */ }
            )
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom nav on main tabs
    val showBottomBar = currentRoute in BottomNavItem.entries.map { it.screen.route }

    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            if (showBottomBar) {
                ChemistBottomNavBar(
                    currentRoute = currentRoute,
                    onItemSelected = { screen ->
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.PointOfSale.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.PointOfSale.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToInventory = {
                        navController.navigate(Screen.Inventory.route) {
                            popUpTo(Screen.PointOfSale.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPOS = {
                        navController.navigate(Screen.PointOfSale.route) {
                            popUpTo(Screen.PointOfSale.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToReports = {
                        navController.navigate(Screen.Reports.route) {
                            popUpTo(Screen.PointOfSale.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToReceipt = { saleId ->
                        navController.navigate(Screen.Receipt.createRoute(saleId))
                    }
                )
            }
            composable(Screen.Inventory.route) {
                val vm: com.habit.mychemistpos.viewmodel.InventoryViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.inventory.InventoryListScreen(viewModel = vm)
            }
            composable(Screen.PointOfSale.route) {
                val vm: com.habit.mychemistpos.viewmodel.POSViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.pos.PointOfSaleScreen(
                    viewModel = vm,
                    onNavigateToReceipt = { saleId ->
                        navController.navigate(Screen.Receipt.createRoute(saleId)) {
                            popUpTo(Screen.PointOfSale.route)
                        }
                    }
                )
            }
            composable(Screen.Receipt.route) {
                val vm: com.habit.mychemistpos.viewmodel.ReceiptViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.pos.ReceiptScreen(
                    viewModel = vm,
                    onNavigateHome = {
                        navController.navigate(Screen.PointOfSale.route) {
                            popUpTo(Screen.PointOfSale.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Reports.route) {
                val vm: com.habit.mychemistpos.viewmodel.ReportsViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.reports.ReportsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.More.route) {
                // For More tab, we can route directly to Settings for now
                val vm: com.habit.mychemistpos.viewmodel.AuthViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.settings.SettingsScreen(
                    authViewModel = vm,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Suppliers.route) {
                val vm: com.habit.mychemistpos.viewmodel.SupplierViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.suppliers.SuppliersScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Customers.route) {
                val vm: com.habit.mychemistpos.viewmodel.CustomerViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.customers.CustomersScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Expenses.route) {
                val vm: com.habit.mychemistpos.viewmodel.ExpenseViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.expenses.ExpensesScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                val vm: com.habit.mychemistpos.viewmodel.AuthViewModel = hiltViewModel()
                com.habit.mychemistpos.ui.screens.settings.SettingsScreen(
                    authViewModel = vm,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
