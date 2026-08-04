package com.example.transaction.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.model.DefaultColors
import com.example.common.ui.component.ItemAndDivider
import com.example.common.ui.component.ListOfItems
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.example.common.utils.toDateTimeString
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.models.TransactionType
import java.time.Instant

@Composable
fun TransactionDetails(
    transaction: Transaction?,
    accounts: List<Account>,
    categories: List<Category>,
    onAccountUpdate: (Transaction) -> Unit,
    onCategoryUpdate: (Int?, Boolean) -> Unit,
    padding: PaddingValues
) {
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }

    AccountUpdateDialog(
        showAccountDialog = showAccountDialog,
        transaction = transaction,
        accounts = accounts,
        onDismiss = { showAccountDialog = false },
        onUpdateAccount = onAccountUpdate
    )

    CategoryUpdateDialog(
        showCategoryDialog = showCategoryDialog,
        transaction = transaction,
        categories = categories,
        onDismiss = { showCategoryDialog = false },
        onUpdateCategory = onCategoryUpdate
    )

    if (transaction == null) {
        Box(
            modifier = Modifier.padding(top = padding.calculateTopPadding()).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            MyText.ScreenHeader("Transaction not found")
        }
    } else {
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ListOfItems {
                ItemAndDivider(true) {
                    MyText.RowBody("Payee")
                    MyText.RowHeader(transaction.payee)
                }

                ItemAndDivider {
                    MyText.RowBody("Amount")
                    MyText.TransactionAmount(transaction.amount, transaction.transactionType)
                }

                ItemAndDivider {
                    MyText.RowBody("Date")
                    MyText.RowHeader(transaction.datetime.toDateTimeString())
                }
            }

            ListOfItems {
                ItemAndDivider(true) {
                    MyText.RowBody("Type")
                    MyText.RowHeader(transaction.transactionType.name)
                }

                transaction.referenceId?.let { referenceId ->
                    ItemAndDivider {
                        MyText.RowBody("Reference ID")
                        MyText.RowHeader(referenceId.toString())
                    }
                }

                ItemAndDivider {
                    val currentCategory = categories.find { it.id == transaction.categoryId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDialog = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MyText.RowBody("Category")
                        MyText.RowHeader(
                            currentCategory?.name ?: "Not Assigned",
                            modifier = Modifier,
                            color = if (currentCategory == null) Color.Red
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                ItemAndDivider {
                    val currentAccount = accounts.find { it.id == transaction.accountId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAccountDialog = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MyText.RowBody("Account")
                        MyText.RowHeader(
                            text = currentAccount?.name
                                ?: "Not Assigned (${transaction.rawAccountNo})",
                            color = if (currentAccount == null) Color.Red
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            transaction.description?.let { description ->
                ListOfItems {
                    ItemAndDivider(true) {
                        MyText.RowBody("Description")
                        MyText.RowHeader(text = description)
                    }
                }
            }
        }
    }
}

private val sampleTransaction = Transaction(
    id = 1,
    amount = 10000,
    datetime = Instant.now().epochSecond,
    accountId = 1,
    categoryId = 1,
    payee = "Sample Payee",
    referenceId = 1,
    description = "Sample Description",
    transactionType = TransactionType.DEBIT,
    rawAccountNo = "1234"
)

private val sampleAccounts = listOf(
    Account(1, "Account 1", 0, AccountType.SAVINGS, "1234", DefaultColors.RED.hexValue, AccountIcon.SAVINGS),
    Account(2, "Account 2", 0, AccountType.CHECKING, "5678", DefaultColors.BLUE.hexValue, AccountIcon.CHECKING),
    Account(3, "Account 3", 0, AccountType.CASH, "9012", DefaultColors.CYAN.hexValue, AccountIcon.CASH)
)

private val sampleCategories = listOf(
    Category(1, "Food", CategoryType.EXPENSE, null, 0xFF0000, CategoryIcon.FOOD),
    Category(2, "Transport", CategoryType.EXPENSE, null, 0x00FF00, CategoryIcon.TRANSPORT),
    Category(3, "Shopping", CategoryType.EXPENSE, null, 0x0000FF, CategoryIcon.SHOPPING)
)

@Preview(showBackground = true)
@Composable
fun TransactionScreenPreview() {
    FinancesTheme {
        ScreenScaffold(
            "Transaction Details",
            {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Transaction")
                }
            },
            isLoading = true
        ) { padding ->
            TransactionDetails(
                transaction = sampleTransaction,
                accounts = sampleAccounts,
                categories = sampleCategories,
                onAccountUpdate = {},
                onCategoryUpdate = { _, _ -> },
                padding = padding
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionScreenPreviewDark() {
    FinancesTheme(darkTheme = true) {
        ScreenScaffold(
            "Transaction Details",
            {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Transaction")
                }
            },
            isLoading = false
        ) { padding ->
            TransactionDetails(
                transaction = sampleTransaction,
                accounts = sampleAccounts,
                categories = sampleCategories,
                onAccountUpdate = {},
                onCategoryUpdate = { _, _ -> },
                padding = padding
            )
        }
    }
}
