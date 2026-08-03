package com.example.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.common.model.DefaultColors
import com.example.common.ui.component.ListOfGrids
import com.example.common.utils.MyText

@Composable
fun ColorPicker(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit,
    selectedColor: Int,
) {
    if (showDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { onDismiss() },
            title = { MyText.ScreenHeader("Select Color") },
            text = {
                ListOfGrids(DefaultColors.entries) { entry ->
                    TextButton(
                        onClick = { onColorSelected(entry.hexValue) }
                    ) {
                        var modifier: Modifier = Modifier
                        modifier = if(entry.hexValue == selectedColor) {
                            modifier
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .padding(5.dp)
                                .size(30.dp)
                        } else {
                            modifier
                                .size(40.dp)
                        }
                        Box(
                            modifier = modifier
                                .clip(CircleShape)
                                .background(Color(entry.hexValue))
                        )

                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onColorSelected(selectedColor)
                    onDismiss()
                }) {
                    MyText.RowHeader("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    MyText.RowHeader("Cancel")
                }
            }
        )
    }
}