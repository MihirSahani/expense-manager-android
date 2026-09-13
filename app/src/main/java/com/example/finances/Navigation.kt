package com.example.finances

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.account.ui.screen.AccountsScreen
import com.example.account.ui.screen.AddEditAccountScreen
import com.example.account.ui.screen.UnresolvedAccountsScreen
import com.example.account.ui.viewmodel.UnresolvedAccountsViewModel
import com.example.analysis.ui.screen.AnalysisScreen
import com.example.category.ui.screen.CategoriesScreen
import com.example.category.ui.screen.EditCategoryScreen
import com.example.category.ui.screen.PayeeDiscoveryScreen
import com.example.category.ui.viewmodel.PayeeDiscoveryViewModel
import com.example.common.ui.component.NavigationBar
import com.example.home.HomeScreen
import com.example.loan.ui.screen.AddEditLoanScreen
import com.example.loan.ui.screen.LoanHistoryScreen
import com.example.login.ui.screen.LoginScreen
import com.example.permission.ui.screen.PermissionScreen
import com.example.setting.ui.screen.SettingsScreen
import com.example.setting.ui.screen.UserProfileScreen
import com.example.transaction.ui.screen.TransactionHistoryScreen
import com.example.transaction.ui.screen.EditTransactionScreen

@Composable
fun App() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Screen.Accounts.route,
        Screen.Analytics.route,
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
                    },
                    navigateToAccountDiscovery = {
                        navController.navigate(Screen.AccountDiscovery.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.AccountDiscovery.route) {
                val vm: UnresolvedAccountsViewModel = hiltViewModel()
                UnresolvedAccountsScreen(
                    onCreateAccount = { number ->
                        navController.navigate(Screen.AddAccount(number).route)
                    },
                    onFinish = {
                        navController.navigate(Screen.PayeeDiscovery(fromOnboarding = true).route) {
                            popUpTo(Screen.AccountDiscovery.route) { inclusive = true }
                        }
                    },
                    vm = vm
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    navigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                    navigateToCategories = { navController.navigate(Screen.Categories.route) },
                    navigateToLoans = { navController.navigate(Screen.Loans.route) },
                    vm = hiltViewModel()
                )
            }

            // Account Screens
            composable(Screen.Accounts.route) {
                AccountsScreen(
                    { id -> navController.navigate(Screen.Account(id).route) },
                    { navController.navigate(Screen.AddAccount().route) }
                )
            }
            composable(
                Screen.AddAccount.ROUTE,
                listOf(navArgument("number") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) {
                AddEditAccountScreen ( { navController.popBackStack() } )
            }
            composable(
                Screen.Account.ROUTE,
                listOf(navArgument("id") {
                    type = NavType.IntType
                })
            ) {
                AddEditAccountScreen ( { navController.popBackStack() } )
            }

            // Category Screens
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    { id -> navController.navigate(Screen.Category(id).route) },
                    { navController.navigate(Screen.PayeeDiscovery().route) }
                )
            }

            composable(
                Screen.PayeeDiscovery.ROUTE,
                listOf(navArgument("onboarding") {
                    type = NavType.BoolType
                    defaultValue = false
                })
            ) { backStackEntry ->
                val fromOnboarding = backStackEntry.arguments?.getBoolean("onboarding") ?: false
                val vm: PayeeDiscoveryViewModel = hiltViewModel()
                PayeeDiscoveryScreen(
                    onFinish = {
                        if (fromOnboarding) {
                            vm.completeOnboarding()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.PayeeDiscovery.ROUTE) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    fromOnboarding = fromOnboarding,
                    vm = vm
                )
            }

            composable(
                Screen.Category.ROUTE,
                listOf(navArgument("id") {
                    type = NavType.IntType
                    // nullable = true
                })
            ) {
                EditCategoryScreen({ navController.popBackStack() })
            }

            // Loan Screens
            composable(Screen.Loans.route) {
                LoanHistoryScreen(
                    onLoanClick = { id -> navController.navigate(Screen.Loan(id).route) },
                    addLoanScreen = { navController.navigate(Screen.AddLoan.route) }
                )
            }

            composable (Screen.AddLoan.route) {
                AddEditLoanScreen({ navController.popBackStack() })
            }

            composable (
                Screen.Loan.ROUTE,
                listOf(navArgument("id") {
                    type = NavType.IntType
                    // nullable = true
                })
            ) {
                AddEditLoanScreen({ navController.popBackStack() })
            }


            composable(Screen.Analytics.route) {
                AnalysisScreen()
            }

            // Transaction Screens
            composable(Screen.Transactions.route) {
                TransactionHistoryScreen { id -> navController.navigate(Screen.Transaction(id).route) }
            }
            composable(
                Screen.Transaction.ROUTE,
                listOf(
                    navArgument("id") { type = NavType.IntType }
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