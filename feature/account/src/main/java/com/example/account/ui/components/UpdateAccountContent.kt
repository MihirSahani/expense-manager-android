package com.example.account.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.common.model.DefaultColors
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.core.database.entity.Account

@Composable
fun UpdateAccountContent(account: Account, onSave: (Account) -> Unit) {
    var name by remember { mutableStateOf(account.name) }
    var balanceText by remember { mutableStateOf(account.balance.toString()) }

    var type by remember { mutableStateOf(account.type) } // drop down
    var accountNumber by remember { mutableStateOf(account.accountNumber) }

    var icon by remember { mutableStateOf(account.icon) } // drop down
    var color by remember { mutableStateOf(account.color) } // drop down

    var showDialog by remember { mutableStateOf(false) }
    ColorAndIconPicker(
        color,
        icon,
        { color = it },
        { icon = it },
        showDialog,
        { showDialog = false }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        MyInput.TextField(
            name,
            { name = it },
            "Account Name",
            modifier = Modifier.fillMaxWidth()
        )

        MyInput.TextField(
            balanceText,
            { balanceText = it },
            "Balance",
            modifier = Modifier.fillMaxWidth()
        )

        DropDown(type) { type = it }

        MyInput.TextField(
            accountNumber?:"",
            { accountNumber = it },
            "Account Number (Last 4 digits)",
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showDialog = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            MyText.RowHeader("Icon and Color")
            Icon(
                imageVector = icon.imageVector,
                contentDescription = "Account Icon",
                modifier = Modifier.padding(8.dp),
                tint = Color(color ?: DefaultColors.GRAY.hexValue)
            )
        }

        MyInput.Button(
            text = "Save",
            onClick = {
                account.name = name
                account.balance = balanceText.toLongOrNull() ?: 0L
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

