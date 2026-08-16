package com.example.account.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.account.ui.components.ColorAndIconPicker
import com.example.account.ui.components.DropDown
import com.example.account.ui.viewmodel.AccountViewModel
import com.example.common.model.DefaultColors
import com.example.common.ui.component.ItemAndDivider
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.component.SingleRowItem
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.core.database.entity.Account
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType

@Composable
fun AddEditAccountScreen(
    afterSave: () -> Unit = {},
    vm: AccountViewModel = hiltViewModel()
) {
    val account by vm.account.collectAsStateWithLifecycle()

    AddEditAccountContent(
        account = account,
        onIconButtonClick = { account ->
            if (!vm.isNewAccount()) {
                vm.deleteAccount(account!!.id)
            }
        },
        onSave = { account ->
            if (vm.isNewAccount()) {
                vm.createAccount(account)
            }
            else {
                vm.updateAccount(account)
            }
            afterSave()
        }
    )
}

@Composable
fun AddEditAccountContent(
    account: Account?,
    onIconButtonClick: (Account?) -> Unit,
    onSave: (Account) -> Unit
) {
    ScreenScaffold(
        "Add Account",
        {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete Account",
                modifier = Modifier.clickable { onIconButtonClick(account) }
            )
        },
        isLoading = account == null
    ) { paddingValues ->
        if (account == null) throw IllegalStateException("Account is null, this should not happen")

        var name by remember { mutableStateOf(account.name) }
        var balanceText by remember { mutableStateOf(account.balance.toDouble().div(100).toString()) }

        var type by remember { mutableStateOf(account.type) } // drop down
        var accountNumber by remember { mutableStateOf(account.accountNumber) }

        var icon by remember { mutableStateOf(account.icon) } // drop down
        var color by remember { mutableStateOf(account.color) } // drop down

        var showDialog by remember { mutableStateOf(false) }

        ListWrapper(paddingValues) {

            ColorAndIconPicker(
                color,
                icon,
                { color = it },
                { icon = it },
                showDialog,
                { showDialog = false }
            )

            MyInput.TextField(
                name,
                { name = it },
                "Account Name",
            )

            MyInput.TextField(
                balanceText,
                { balanceText = it },
                "Balance",
            )

            DropDown(type) { type = it }

            MyInput.TextField(
                accountNumber ?: "",
                { accountNumber = it },
                "Account Number (Last 4 digits)",
            )

            SingleRowItem(modifier = Modifier.clickable { showDialog = true }) {
                MyText.RowHeader("Icon and Color")
                Box(
                    modifier = Modifier
                        // .padding(vertical = 8.dp)
                        .clip(CircleShape)
                        .background(Color(color ?: DefaultColors.GRAY.hexValue))
                ) {
                    Icon(
                        icon.imageVector,
                        contentDescription = "Account Icon",
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }

            MyInput.Button(
                text = "Save",
                onClick = {
                    account.name = name
                    account.balance = balanceText.toDoubleOrNull()?.times(100)?.toLong() ?: 0L
                    account.type = type
                    account.accountNumber = accountNumber
                    account.icon = icon
                    account.color = color
                    onSave(account)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditAccountContentPreview() {
    FinancesTheme {
        AddEditAccountContent(
            account = Account(
                id = 1,
                name = "Checking",
                balance = 1000L,
                type = AccountType.CHECKING,
                accountNumber = "1234",
                color = null,
                icon = AccountIcon.SAVINGS
            ),
            onIconButtonClick = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditAccountContentPreviewDark() {
    FinancesTheme(true) {
        AddEditAccountContent(
            account = Account(
                id = 1,
                name = "Checking",
                balance = 1000L,
                type = AccountType.CHECKING,
                accountNumber = "1234",
                color = null,
                icon = AccountIcon.SAVINGS
            ),
            onIconButtonClick = {},
            onSave = {}
        )
    }
}
