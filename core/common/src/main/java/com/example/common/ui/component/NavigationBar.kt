package com.example.common.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText

object NavRoutes {
    const val ACCOUNTS = "accounts"
    const val ANALYTICS = "analytics"
    const val TRANSACTIONS = "transactions"
    const val SETTINGS = "settings"
}

@Composable
fun NavigationBar(
    currentRoute: String?,
    navigateToAccounts: () -> Unit,
    navigateToAnalytics: () -> Unit,
    navigateToTransactionHistory: () -> Unit,
    navigateToSettings: () -> Unit,
    accountsRoute: String = NavRoutes.ACCOUNTS,
    analyticsRoute: String = NavRoutes.ANALYTICS,
    transactionsRoute: String = NavRoutes.TRANSACTIONS,
    settingsRoute: String = NavRoutes.SETTINGS
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleNavigationButton(
            text = "Accounts",
            imageVector = Icons.Default.AccountBalance,
            selected = currentRoute == accountsRoute,
            onClick = navigateToAccounts
        )
        SingleNavigationButton(
            text = "Analytics",
            imageVector = Icons.Default.Analytics,
            selected = currentRoute == analyticsRoute,
            onClick = navigateToAnalytics
        )
        SingleNavigationButton(
            text = "Transactions",
            imageVector = Icons.Default.Money,
            selected = currentRoute == transactionsRoute,
            onClick = navigateToTransactionHistory
        )
        SingleNavigationButton(
            text = "Settings",
            imageVector = Icons.Default.Settings,
            selected = currentRoute == settingsRoute,
            onClick = navigateToSettings
        )

    }
}

@Preview(showBackground = true)
@Composable
fun NavigationBarPreview() {
    FinancesTheme {
        Surface {
            NavigationBar(
                currentRoute = "accounts",
                navigateToAccounts = {},
                navigateToAnalytics = {},
                navigateToTransactionHistory = {},
                navigateToSettings = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationBarPreviewDark() {
    FinancesTheme(true) {
        Surface {
            NavigationBar(
                currentRoute = "transactions",
                navigateToAccounts = {},
                navigateToAnalytics = {},
                navigateToTransactionHistory = {},
                navigateToSettings = {}
            )
        }
    }
}