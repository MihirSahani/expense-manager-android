package com.example.account.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.core.database.models.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDown(type: AccountType, onTypeSelected: (AccountType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
    ) {
        MyInput.TextField(
            type.display(),
            {},
            "Account Type",
            trailingIcon = null,
            readOnly = true,
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            AccountType.entries.forEach { accountType->
                DropdownMenuItem(
                    text =  { MyText.RowBody(accountType.display()) },
                    onClick = {
                        onTypeSelected(accountType)
                        expanded = false
                    }
                )
            }
        }
    }
}