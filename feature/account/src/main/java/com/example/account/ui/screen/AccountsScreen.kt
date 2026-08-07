package com.example.account.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.account.ui.components.AccountItem
import com.example.account.ui.components.NetBalanceDisplay
import com.example.account.ui.viewmodel.AccountsViewModel
import com.example.common.ui.component.LazyListOfItems
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.core.database.entity.Account
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType

@Composable
fun AccountsScreen(onAccountClick: (Int) -> Unit, onAddAccountClick: () -> Unit) {
    val viewmodel: AccountsViewModel = hiltViewModel()
    val accounts by viewmodel.accounts.collectAsStateWithLifecycle(emptyList())
    val netBalance by viewmodel.netWorth.collectAsStateWithLifecycle(0L)

    AccountsContent(
        accounts = accounts,
        netBalance = netBalance,
        onAccountClick = onAccountClick,
        onAddAccountClick = onAddAccountClick
    )
}

@Composable
fun AccountsContent(
    accounts: List<Account>,
    netBalance: Long,
    onAccountClick: (Int) -> Unit,
    onAddAccountClick: () -> Unit
) {
    ScreenScaffold(
        title = "Accounts",
        floatingActionButton = { modifier ->
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Account",
                modifier = modifier
                    .clickable { onAddAccountClick() }
            )
        },
        isLoading = accounts.isEmpty()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            NetBalanceDisplay(netBalance)

            LazyListOfItems(accounts) { account ->
                AccountItem(
                    account = account,
                    onClick = { onAccountClick(account.id) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountsContentPreview() {
    val sampleAccounts = listOf(
        Account(id = 1, name = "Checking", balance = 1000L, type = AccountType.CHECKING, accountNumber = "1234", color = 0xFF6200, icon = AccountIcon.SAVINGS),
        Account(id = 2, name = "Savings", balance = 5000L, type = AccountType.SAVINGS, accountNumber = "5678", color = 0xFF03DA, icon = AccountIcon.SAVINGS),
        Account(id = 3, name = "Credit Card", balance = -200L, type = AccountType.CREDIT_CARD, accountNumber = "9012", color = 0xFFB000, icon = AccountIcon.CREDIT_CARD),
        Account(id = 4, name = "Investment", balance = 10000L, type = AccountType.INVESTMENT, accountNumber = "3456", color = 0xFF6200, icon = AccountIcon.INVESTMENT),
        Account(id = 5, name = "Cash", balance = 300L, type = AccountType.CASH, accountNumber = "7890", color = 0xFF03DA, icon = AccountIcon.CASH)
    )
    FinancesTheme {
        AccountsContent(
            accounts = sampleAccounts,
            netBalance = 5800L,
            onAccountClick = {},
            onAddAccountClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountsContentPreviewDark() {
    val sampleAccounts = listOf(
        Account(id = 1, name = "Checking", balance = 1000L, type = AccountType.CHECKING, accountNumber = "1234", color = 0xFF6200, icon = AccountIcon.SAVINGS),
        Account(id = 2, name = "Savings", balance = 5000L, type = AccountType.SAVINGS, accountNumber = "5678", color = 0xFF03DA, icon = AccountIcon.SAVINGS),
        Account(id = 3, name = "Credit Card", balance = -200L, type = AccountType.CREDIT_CARD, accountNumber = "9012", color = 0xFFB000, icon = AccountIcon.CREDIT_CARD),
        Account(id = 4, name = "Investment", balance = 10000L, type = AccountType.INVESTMENT, accountNumber = "3456", color = 0xFF6200, icon = AccountIcon.INVESTMENT),
        Account(id = 5, name = "Cash", balance = 300L, type = AccountType.CASH, accountNumber = "7890", color = 0xFF03DA, icon = AccountIcon.CASH)
    )
    FinancesTheme(darkTheme = true) {
        AccountsContent(
            accounts = sampleAccounts,
            netBalance = 5800L,
            onAccountClick = {},
            onAddAccountClick = {}
        )
    }
}