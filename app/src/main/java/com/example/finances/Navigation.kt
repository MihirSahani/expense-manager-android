package com.example.finances

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.account.ui.screen.AccountsScreen
import com.example.account.ui.screen.AddEditAccountScreen
import com.example.setting.ui.screen.SettingsScreen
import com.example.setting.ui.screen.UserProfileScreen
import com.example.setting.ui.viewmodel.SettingViewModel
import com.example.transaction.ui.screen.TransactionHistoryScreen
import com.example.transaction.ui.screen.EditTransactionScreen

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
        }
        composable(Screen.Permissions.route) {
        }
        composable(Screen.Home.route) {
        }
        composable(Screen.Accounts.route) {
            AccountsScreen(
                onAccountClick = { id ->
                    navController.navigate(Screen.Account(id))
                },
                onAddAccountClick = {
                    navController.navigate(Screen.Account(null))
                }
            )
        }
        composable(
            Screen.Account.ROUTE,
            listOf(navArgument("id") {
                type = androidx.navigation.NavType.IntType
                nullable = true
            })
        ) {
            AddEditAccountScreen { navController.popBackStack() }
        }
        composable(Screen.Analytics.route) {
        }
        composable(Screen.Transactions.route) {
            TransactionHistoryScreen { id ->
                navController.navigate(Screen.Transaction(id))
            }
        }
        composable(
            Screen.Transaction.ROUTE,
            listOf(navArgument("id") {
                type = androidx.navigation.NavType.IntType
            })) {
            EditTransactionScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onUserProfileClick = {
                    navController.navigate(Screen.UserProfile.route)
                },
            )
        }
        composable(Screen.UserProfile.route) {
            UserProfileScreen { navController.popBackStack() }
        }
    }
}