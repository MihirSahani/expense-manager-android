package com.example.common.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.common.utils.MyInput
import com.example.common.utils.MyText

/**
 * Prompts the user to pick one item from [items] in a scrollable list dialog. Shared by every
 * screen that needs an "AlertDialog + list of rows + Cancel button" picker (account, category,
 * loan-type, cycle-type, currency selection, etc). [itemContent] renders each row's contents
 * (defaults to the item's [toString]); selecting a row invokes [onItemSelected] with that item
 * and dismisses the dialog. [headerContent], if given, is rendered above the list (e.g. a
 * "remember this choice" switch).
 */
@Composable
fun <T> PickerDialog(
    title: String,
    show: Boolean,
    items: List<T>,
    onDismiss: () -> Unit,
    onItemSelected: (T) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
    itemContent: @Composable (T) -> Unit = {
        MyText.RowHeader(it.toString(), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
) {
    if (!show) return

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        onDismissRequest = onDismiss,
        title = { MyText.SecondaryHeader(title) },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                headerContent?.invoke()
                LazyListOfItems(items) { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemSelected(item)
                                onDismiss()
                            }
                    ) {
                        itemContent(item)
                    }
                }
            }
        },
        confirmButton = {
            MyInput.Button("Cancel", onClick = onDismiss)
        }
    )
}
