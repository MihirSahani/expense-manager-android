package com.example.setting.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.core.database.models.DefaultColors
import com.example.common.ui.component.LazyListOfItems
import com.example.common.utils.MyInput
import com.example.common.utils.MyText

@Composable
fun <T> SimpleDialog(
    title: String,
    showDialog: Boolean,
    items: List<T>,
    onDismiss: () -> Unit,
    onItemSelected: (T) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            onDismissRequest = onDismiss,
            title = { MyText.SecondaryHeader(title) },
            text = {
                LazyListOfItems(
                    items
                ) { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onItemSelected(item) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MyText.RowHeader(item.toString())
                    }
                }
            },
            confirmButton = {
                MyInput.Button("Cancel", onClick = onDismiss)
            }
        )
    }
}