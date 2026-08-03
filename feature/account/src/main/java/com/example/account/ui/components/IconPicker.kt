package com.example.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.common.ui.component.ListOfGrids
import com.example.common.utils.MyText
import com.example.core.database.models.AccountIcon

@Composable
fun IconPicker(
    showIconPicker: Boolean,
    selectedIcon: AccountIcon,
    color: Int = 0x2E2727,
    onIconChange: (AccountIcon) -> Unit,
    onDismiss: () -> Unit
) {
    if(showIconPicker) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { onDismiss() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            title = { MyText.SecondaryHeader("Select Icon") },
            text = {
                ListOfGrids(AccountIcon.entries) { icon->
                    TextButton(
                        onClick = {
                            onIconChange(icon)
                            onDismiss()
                        }
                    ) {
                        var modifier: Modifier = Modifier
                        modifier = if(icon == selectedIcon) {
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
                                .background(Color(color))
                        ) {
                            Icon(
                                imageVector = icon.imageVector,
                                contentDescription = icon.name,
                                modifier = if(icon == selectedIcon) {
                                    Modifier.size(20.dp).align(Alignment.Center)
                                } else { Modifier.size(24.dp).align(Alignment.Center) },
                                tint = Color.Black
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onDismiss() }) {
                    MyText.SecondaryHeader("Cancel")
                }
            },
        )
    }
}