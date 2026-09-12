package com.example.transaction.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.common.ui.component.IconAndRow
import com.example.common.ui.component.PickerDialog
import com.example.common.ui.component.SingleRowItem
import com.example.common.utils.MyText
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction

@Composable
fun CategoryUpdateDialog(
    showCategoryDialog: Boolean,
    transaction: Transaction?,
    categories: List<Category>,
    onUpdateCategory: (Int, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var updateCategoryForAllTransactionsWithPayee by remember(showCategoryDialog) {
        mutableStateOf(false)
    }
    if (transaction != null) {
        PickerDialog(
            title = "Select Category",
            show = showCategoryDialog,
            items = categories,
            onDismiss = onDismiss,
            onItemSelected = { category ->
                onUpdateCategory(category.id, updateCategoryForAllTransactionsWithPayee)
            },
            headerContent = {
                SingleRowItem {
                    MyText.RowBody("Apply to all and remember for future")
                    Switch(
                        modifier = Modifier.padding(start = 8.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        checked = updateCategoryForAllTransactionsWithPayee,
                        onCheckedChange = { updateCategoryForAllTransactionsWithPayee = it }
                    )
                }
            },
            itemContent = { category ->
                IconAndRow(category.icon.imageVector, category.color) {
                    MyText.RowHeader(category.name, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }
}