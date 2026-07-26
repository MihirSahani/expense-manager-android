package com.example.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.common.utils.MyText
import com.example.common.utils.MyText.Companion.toIndianFormat
import com.example.common.utils.toDateString
import com.example.common.utils.toDateTimeString
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction

@Composable
fun TransactionDetails(
    transaction: Transaction?,
    accounts: List<Account>,
    categories: List<Category>,
    onAccountUpdate: (Transaction) -> Unit,
    onCategoryUpdate: (Transaction, Boolean) -> Unit,
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            MyText.ScreenHeader("Transaction not found")
        }
    } else {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            MyText.ScreenHeader("Transaction Details")

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DetailItem("Payee", transaction.payee)

                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                DetailItemAmount(transaction.amount, transaction.transactionType)

                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                DetailItem("Date", transaction.datetime.toDateTimeString())

                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                DetailItem("Type", transaction.transactionType.name)

                transaction.referenceId?.run {
                    HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DetailItem("Reference ID", this.toString())
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val currentCategory = categories.find { it.id == transaction.categoryId }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDialog = true }
                            .padding(horizontal = 16.dp)
                            .height(50.dp),
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
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                val currentAccount = accounts.find { it.id == transaction.accountId }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountDialog = true }
                        .padding(horizontal = 16.dp)
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MyText.RowBody("Account")
                    MyText.RowHeader(
                        text = currentAccount?.name ?:
                        "Not Assigned (${transaction.rawAccountNo})",
                        color = if (currentAccount == null) Color.Red
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (transaction.description != null) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        MyText.RowBody("Description")
                        MyText.RowHeader(text = transaction.description!!)
                    }
                }
            }
        }
    }
}