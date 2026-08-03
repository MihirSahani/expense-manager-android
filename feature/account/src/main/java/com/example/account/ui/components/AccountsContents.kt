package com.example.account.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.ui.component.LazyListOfItems
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.core.database.entity.Account
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType

@Composable
fun AccountsContent(
    accounts: List<Account>,
    netBalance: Long,
    onAccountClick: (Int) -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = padding.calculateTopPadding())
    ) {
        NetBalanceDisplay(netBalance)

        Spacer(modifier = Modifier.height(16.dp))

        LazyListOfItems(accounts) { account ->
            AccountItem(
                account = account,
                onClick = { onAccountClick(account.id) }
            )
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
    FinancesTheme(darkTheme = true) {
        ScreenScaffold("Accounts") { padding ->
            AccountsContent(
                accounts = sampleAccounts,
                netBalance = 5800L,
                onAccountClick = {},
                padding = padding
            )
        }
    }
}