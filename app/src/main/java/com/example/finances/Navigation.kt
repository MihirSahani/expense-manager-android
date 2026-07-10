package com.example.finances

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
        }
        composable(Screen.Transaction.ROUTE) {
        }
        composable(Screen.Settings.route) {
        }
    }
}