package com.example.finances

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.transaction.ui.screen.TransactionHistoryScreen
import com.example.transaction.ui.screen.TransactionScreen

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
                onAccountClick = { id -> navController.navigate(Screen.Account(id)) },
                onAddAccountClick = { navController.navigate(Screen.Account(null)) }
            )
        }
        composable(Screen.Account.ROUTE) {
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
            TransactionScreen()
        }
        composable(Screen.Settings.route) {
        }
    }
}