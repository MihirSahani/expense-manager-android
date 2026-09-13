package com.example.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.NetBalanceDisplay
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.component.SingleRowItem
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText

@Composable
fun HomeScreen(
    navigateToAccounts: () -> Unit,
    navigateToCategories: () -> Unit,
    navigateToLoans: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        vm.refreshSalaryCreditTime()
    }
    val netWorth by vm.netWorth.collectAsStateWithLifecycle(0L)
    HomeContent(netWorth, navigateToAccounts, navigateToCategories, navigateToLoans)
}

@Composable
fun HomeContent(
    netWorth: Long,
    navigateToAccounts: () -> Unit = {},
    navigateToCategories: () -> Unit = {},
    navigateToLoans: () -> Unit = {},
) {
    ScreenScaffold("Home") { paddingValues ->
        ListWrapper(paddingValues) {

            NetBalanceDisplay(netWorth)

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SingleRowItem(Modifier
                    .weight(1f)
                    .clickable { navigateToAccounts() }
                ) {
                    MyText.RowHeader("Accounts")
                    Icon(
                        imageVector = Icons.Default.ArrowOutward,
                        contentDescription = "Link",
                    )
                }
                SingleRowItem(Modifier
                    .weight(1f)
                    .clickable { navigateToCategories() }
                ) {
                    MyText.RowHeader("Categories")
                    Icon(
                        imageVector = Icons.Default.ArrowOutward,
                        contentDescription = "Link",
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SingleRowItem(Modifier
                    .weight(1f)
                    .clickable { navigateToLoans() }
                ) {
                    MyText.RowHeader("Loans")
                    Icon(
                        imageVector = Icons.Default.ArrowOutward,
                        contentDescription = "Link",
                    )
                }
                Row(Modifier.weight(1f)) { }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    FinancesTheme {
        HomeContent(0L)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreviewDark() {
    FinancesTheme(true) {
        HomeContent(0L)
    }
}