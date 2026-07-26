package com.example.transaction.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.common.utils.MyText


@Composable
fun SimpleDialog(
    title: String,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 16.dp),
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = { MyText.SecondaryHeader(title) },
        text = {
            ListOfItems(
                items
            ) { item ->
                MyText.RowHeader(
                    item,
                    modifier = Modifier
                        .clickable { onItemSelected(items.indexOf(item)) }
                        .padding(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                MyText.RowHeader("Cancel")
            }
        }
    )
}
