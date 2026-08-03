package com.example.finances

sealed class Screen(val route: String) {
    object Login: Screen("login")
    object Permissions: Screen("permissions")
    object Home: Screen("home")

    object Accounts: Screen("accounts")
    data class Account(val id: Int?): Screen("account/$id") {
        companion object {
            const val ROUTE = "account/{id}"
        }
    }

    object Analytics: Screen("analytics")

    object Transactions: Screen("transactions")
    data class Transaction(val id: Int): Screen("transaction/$id") {
        companion object {
            const val ROUTE = "transaction/{id}"
        }
    }

    object Settings: Screen("settings")
}