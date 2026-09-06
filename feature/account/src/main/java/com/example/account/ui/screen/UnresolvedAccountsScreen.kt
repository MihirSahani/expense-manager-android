package com.example.account.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.account.ui.viewmodel.UnresolvedAccountsViewModel
import com.example.common.ui.component.IconAndRow
import com.example.common.ui.component.LazyListOfItems
import com.example.common.ui.component.PickerDialog
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.core.database.entity.Account
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import com.example.core.database.models.DefaultColors
import com.example.core.database.projection.UnresolvedAccountSummary

/**
 * Which step of the discovery flow is currently shown. In [CREATE_ACCOUNTS], tapping a raw
 * number opens the add-account screen prefilled with it. In [TAG_DEBIT_CARDS], tapping a raw
 * number opens a picker to attribute it to an existing account as a debit card.
 */
private enum class DiscoveryMode { CREATE_ACCOUNTS, TAG_DEBIT_CARDS }

/**
 * Lets the user resolve every raw account/card number seen on imported transactions that isn't
 * linked to an account yet: first by creating real accounts for the ones that are the user's own
 * bank accounts, then by tagging whatever remains as debit cards belonging to existing accounts.
 *
 * @param onCreateAccount navigates to the add-account screen, prefilled with the given raw
 * account number.
 * @param onFinish invoked when the user taps "Next" to move past the debit-card-tagging step
 * (this screen no longer ends the overall discovery flow — payee-category-discovery follows it),
 * or automatically as soon as there are no unresolved raw numbers left (e.g. everything was
 * already resolved).
 */
@Composable
fun UnresolvedAccountsScreen(
    onCreateAccount: (String) -> Unit,
    onFinish: () -> Unit,
    vm: UnresolvedAccountsViewModel = hiltViewModel()
) {
    val unresolvedAccounts by vm.unresolvedAccounts.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()

    // `null` means the list hasn't loaded yet; only a loaded, empty list means there is
    // genuinely nothing left to resolve, so only then should we skip straight to onFinish.
    LaunchedEffect(unresolvedAccounts) {
        if (unresolvedAccounts?.isEmpty() == true) {
            onFinish()
        }
    }

    UnresolvedAccountsContent(
        unresolvedAccounts = unresolvedAccounts.orEmpty(),
        accounts = accounts,
        onCreateAccount = onCreateAccount,
        onTagToAccount = vm::tagToAccount,
        onFinish = onFinish
    )
}

@Composable
fun UnresolvedAccountsContent(
    unresolvedAccounts: List<UnresolvedAccountSummary>,
    accounts: List<Account>,
    onCreateAccount: (String) -> Unit,
    onTagToAccount: (String, Int) -> Unit,
    onFinish: () -> Unit
) {
    var mode by remember { mutableStateOf(DiscoveryMode.CREATE_ACCOUNTS) }

    ScreenScaffold(
        title = when (mode) {
            DiscoveryMode.CREATE_ACCOUNTS -> "Identify Accounts"
            DiscoveryMode.TAG_DEBIT_CARDS -> "Tag Debit Cards"
        }
    ) { paddingValues ->
        ListWrapper(paddingValues) {
            LazyListOfItems(unresolvedAccounts, "Unresolved Account Numbers") { summary ->
                var showPicker by remember(summary.rawAccountNo) { mutableStateOf(false) }

                PickerDialog(
                    title = "Select Account",
                    show = showPicker,
                    items = accounts,
                    onDismiss = { showPicker = false },
                    onItemSelected = { account -> onTagToAccount(summary.rawAccountNo, account.id) },
                    itemContent = { account ->
                        IconAndRow(account.icon.imageVector, account.color) {
                            MyText.RowHeader(account.name)
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (mode) {
                                DiscoveryMode.CREATE_ACCOUNTS -> onCreateAccount(summary.rawAccountNo)
                                DiscoveryMode.TAG_DEBIT_CARDS -> showPicker = true
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MyText.RowHeader(summary.rawAccountNo)
                    MyText.RowBody("${summary.transactionCount} transactions")
                }
            }

            MyInput.Button(
                text = when (mode) {
                    DiscoveryMode.CREATE_ACCOUNTS -> "Next"
                    DiscoveryMode.TAG_DEBIT_CARDS -> "Next"
                },
                onClick = {
                    when (mode) {
                        DiscoveryMode.CREATE_ACCOUNTS -> mode = DiscoveryMode.TAG_DEBIT_CARDS
                        DiscoveryMode.TAG_DEBIT_CARDS -> onFinish()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UnresolvedAccountsContentPreview() {
    FinancesTheme {
        UnresolvedAccountsContent(
            unresolvedAccounts = previewUnresolvedAccounts(),
            accounts = previewAccounts(),
            onCreateAccount = {},
            onTagToAccount = { _, _ -> },
            onFinish = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UnresolvedAccountsContentPreviewDark() {
    FinancesTheme(darkTheme = true) {
        UnresolvedAccountsContent(
            unresolvedAccounts = previewUnresolvedAccounts(),
            accounts = previewAccounts(),
            onCreateAccount = {},
            onTagToAccount = { _, _ -> },
            onFinish = {}
        )
    }
}

private fun previewUnresolvedAccounts() = listOf(
    UnresolvedAccountSummary(rawAccountNo = "1234", transactionCount = 12),
    UnresolvedAccountSummary(rawAccountNo = "5678", transactionCount = 3)
)

private fun previewAccounts() = listOf(
    Account(1, "Checking", 1000L, AccountType.CHECKING, "9012", DefaultColors.RED.hexValue, AccountIcon.SAVINGS),
    Account(2, "Savings", 5000L, AccountType.SAVINGS, "3456", DefaultColors.BLUE.hexValue, AccountIcon.SAVINGS)
)
