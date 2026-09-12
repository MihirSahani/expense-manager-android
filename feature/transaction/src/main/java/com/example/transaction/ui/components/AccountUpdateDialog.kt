package com.example.transaction.ui.components

import androidx.compose.runtime.Composable
import com.example.common.ui.component.IconAndRow
import com.example.common.ui.component.PickerDialog
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
    if (transaction != null) {
        PickerDialog(
            title = "Select Account",
            show = showAccountDialog,
            items = accounts,
            onDismiss = onDismiss,
            onItemSelected = { account ->
                onUpdateAccount(transaction.copy(accountId = account.id))
            },
            itemContent = { account ->
                IconAndRow(account.icon.imageVector, account.color) {
                    MyText.RowHeader(account.name)
                }
            }
        )
    }
}
