package com.example.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.models.TransactionType

@Composable
fun AccountUpdateDialog(
    showAccountDialog: Boolean,
    transaction: Transaction?,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onUpdateAccount: (Transaction) -> Unit
) {
    if (showAccountDialog && transaction != null) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            onDismissRequest = { onDismiss() },
            shape = RoundedCornerShape(16.dp),
            title = { MyText.SecondaryHeader("Select Account") },
            text = {
                Column {
                    ListOfItems(accounts) { account ->
                        TextButton(
                            onClick = {
                                val updatedTransaction = transaction.copy(
                                    accountId = account.id
                                )
                                onUpdateAccount(updatedTransaction)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MyText.RowHeader(account.name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    onClick = { onDismiss() }
                ) {
                    MyText.RowHeader("Cancel")
                }
            }
        )
    }
}