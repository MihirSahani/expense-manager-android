package com.example.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.common.ui.component.LazyListOfItems
import com.example.common.utils.MyText
import com.example.core.database.entity.Category
import com.example.core.database.entity.Transaction

@Composable
fun CategoryUpdateDialog(
    showCategoryDialog: Boolean,
    transaction: Transaction?,
    categories: List<Category>,
    onUpdateCategory: (Transaction, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var updateCategoryForAllTransactionsWithPayee by remember { mutableStateOf(false) }
    if (showCategoryDialog && transaction != null) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            onDismissRequest = { onDismiss() },
            title = { MyText.SecondaryHeader("Select Category") },
            text = {
                Column(
                    Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp)
                        ,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MyText.RowBody(
                            "Update all for this payee",
                            modifier = Modifier.weight(1f),
                        )
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

                    Spacer(Modifier.padding(8.dp))

                    LazyListOfItems(categories) { category ->
                        TextButton(
                            onClick = {
                                val updatedTransaction = transaction.copy(
                                    categoryId = category.id
                                )
                                onUpdateCategory(
                                    updatedTransaction,
                                    updateCategoryForAllTransactionsWithPayee
                                )
                                onDismiss()
                            },
                            // modifier = Modifier.fillMaxWidth().padding( horizontal = 8.dp)
                        ) {
                            MyText.RowHeader(category.name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    shape = RoundedCornerShape(25),
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