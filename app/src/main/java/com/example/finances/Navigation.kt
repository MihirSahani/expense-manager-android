package com.example.finances

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.account.ui.screen.AccountsScreen
import com.example.account.ui.screen.AddEditAccountScreen
import com.example.category.ui.screen.CategoriesScreen
import com.example.category.ui.screen.EditCategoryScreen
import com.example.common.ui.component.NavigationBar
import com.example.home.HomeScreen
import com.example.login.ui.screen.LoginScreen
import com.example.permission.ui.screen.PermissionScreen
import com.example.setting.ui.screen.SettingsScreen
import com.example.setting.ui.screen.UserProfileScreen
import com.example.setting.ui.viewmodel.SettingViewModel
import com.example.transaction.ui.screen.TransactionHistoryScreen
import com.example.transaction.ui.screen.EditTransactionScreen

@Composable
fun App() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Screen.Accounts.route,
        Screen.Home.route,
        Screen.Transactions.route,
        Screen.Settings.route
    )

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar(
                    currentRoute = currentRoute,
                    navigateToAccounts = { navigateToTab(Screen.Accounts.route) },
                    navigateToAnalytics = { navigateToTab(Screen.Analytics.route) },
                    navigateToHome = { navigateToTab(Screen.Home.route) },
                    navigateToTransactionHistory = { navigateToTab(Screen.Transactions.route) },
                    navigateToSettings = { navigateToTab(Screen.Settings.route) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Permissions.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Permissions.route) {
                PermissionScreen(
                    navigateToNextScreen = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Permissions.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    navigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(hiltViewModel())
            }
            composable(Screen.Accounts.route) {
                AccountsScreen(
                    { id -> navController.navigate(Screen.Account(id)) },
                    { navController.navigate(Screen.AddAccount.route) }
                )
            }
            composable(Screen.AddAccount.route) {
                AddEditAccountScreen ( { navController.popBackStack() } )
            }
            composable(
                Screen.Account.ROUTE,
                listOf(navArgument("id") {
                    type = androidx.navigation.NavType.IntType
                })
            ) {
                AddEditAccountScreen ( { navController.popBackStack() } )
            }

            composable(Screen.Categories.route) {
                CategoriesScreen({ id -> navController.navigate(Screen.Category(id)) })
            }

            composable(
                Screen.Category.ROUTE,
                listOf(navArgument("id") {
                    type = androidx.navigation.NavType.IntType
                    // nullable = true
                })
            ) {
                EditCategoryScreen()
            }

            composable(Screen.Analytics.route) {
            }

            composable(Screen.Transactions.route) {
                TransactionHistoryScreen { id -> navController.navigate(Screen.Transaction(id)) }
            }
            composable(
                Screen.Transaction.ROUTE,
                listOf(
                    navArgument("id") { type = androidx.navigation.NavType.IntType }
                )
            ) {
                EditTransactionScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen({ navController.navigate(Screen.UserProfile.route) })
            }
            composable(Screen.UserProfile.route) {
                UserProfileScreen { navController.popBackStack() }
            }
        }
    }
}