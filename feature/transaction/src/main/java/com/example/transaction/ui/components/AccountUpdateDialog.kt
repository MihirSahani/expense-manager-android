package com.example.transaction.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.common.ui.component.IconAndRow
import com.example.common.ui.component.LazyListOfItems
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.core.database.entity.Account
import com.example.core.database.entity.Transaction

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
                LazyListOfItems(accounts) { account ->
                    IconAndRow(account.icon.imageVector, account.color) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val updatedTransaction = transaction.copy(
                                        accountId = account.id
                                    )
                                    onUpdateAccount(updatedTransaction)
                                    onDismiss()
                                }
                        ) {
                            MyText.RowHeader(account.name)
                        }
                    }
                }
            },
            confirmButton = {
                MyInput.Button("Cancel", onClick = { onDismiss() })
            }
        )
    }
}