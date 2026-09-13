package com.example.finances

sealed class Screen(val route: String) {
    object Login: Screen("login")
    object Permissions: Screen("permissions")
    object Home: Screen("home")

    object AccountDiscovery: Screen("account_discovery")

    object Accounts: Screen("accounts")
    data class AddAccount(val prefillNumber: String? = null): Screen(
        if (prefillNumber != null) "account/new?number=$prefillNumber" else "account/new"
    ) {
        companion object {
            const val ROUTE = "account/new?number={number}"
        }
    }
    data class Account(val id: Int): Screen("account/$id") {
        companion object {
            const val ROUTE = "account/{id}"
        }
    }

    object Categories: Screen("categories")
    data class PayeeDiscovery(val fromOnboarding: Boolean = false): Screen(
        "payee_discovery?onboarding=$fromOnboarding"
    ) {
        companion object {
            const val ROUTE = "payee_discovery?onboarding={onboarding}"
        }
    }
    data class Category(val id: Int?): Screen("category/$id") {
        companion object {
            const val ROUTE = "category/{id}"
        }
    }

    object Loans: Screen("loans")

    object AddLoan: Screen("loan/new")

    data class Loan(val id: Int?): Screen("loan/$id") {
        companion object {
            const val ROUTE = "loan/{id}"
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

    object UserProfile: Screen("user_profile")
}