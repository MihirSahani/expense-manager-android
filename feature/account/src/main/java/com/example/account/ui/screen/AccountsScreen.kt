package com.example.account.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.account.ui.components.AccountsContent
import com.example.account.ui.viewmodel.AccountsViewModel
import com.example.common.ui.component.ScreenScaffold

@Composable
fun AccountsScreen(onAccountClick: (Int) -> Unit, onAddAccountClick: () -> Unit) {
    val viewmodel: AccountsViewModel = hiltViewModel()
    val accounts by viewmodel.accounts.collectAsStateWithLifecycle(emptyList())
    val netBalance by viewmodel.netWorth.collectAsStateWithLifecycle(0L)

    ScreenScaffold(
        title = "Accounts",
        floatingActionButton = { modifier ->
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Account",
                modifier = modifier
                    .clickable { onAddAccountClick() }
            )
        }
    ) { padding ->
        AccountsContent(
            accounts = accounts,
            netBalance = netBalance,
            onAccountClick = { id -> onAccountClick(id) },
            padding = padding
        )
    }
}