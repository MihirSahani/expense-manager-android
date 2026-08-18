package com.example.transaction.ui.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.core.database.models.DefaultColors
import com.example.common.ui.component.LazyListOfItems
import com.example.common.utils.MyInput
import com.example.common.utils.MyText


@Composable
fun SimpleDialog(
    title: String,
    items: List<Triple<ImageVector?, String, Int?>>,
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
            LazyListOfItems(
                items
            ) { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item.first?.let {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Color(item.third ?: DefaultColors.GRAY.hexValue)
                                )
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = it,
                                contentDescription = it.name,
                            )
                        }
                    }

                    MyText.RowHeader(
                        item.second,
                        modifier = Modifier
                            .clickable { onItemSelected(items.indexOf(item)) }
                            .padding(16.dp)
                    )
                }

            }
        },
        confirmButton = {
            MyInput.Button("Cancel", onClick = onDismiss)
        }
    )
}
