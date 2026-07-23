package com.example.finances

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.transaction.ui.screens.TransactionHistoryScreen
import com.example.transaction.ui.viewmodel.TransactionViewModel

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
        composable(Screen.Transaction.ROUTE) {
        }
        composable(Screen.Settings.route) {
        }
    }
}