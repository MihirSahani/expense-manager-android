package com.example.account.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.common.model.DefaultColors
import com.example.core.database.models.AccountIcon

@Composable
fun ColorAndIconPicker(
    existingColor: Int?,
    existingIcon: AccountIcon?,
    updateColor: (Int) -> Unit,
    updateIcon: (AccountIcon) -> Unit,
    showDialog: Boolean,
    onDismiss: () -> Unit

) {
    var showIconPicker by remember { mutableStateOf(false) }

    ColorPicker(
        showDialog,
        { onDismiss() },
        {
            updateColor(it)
            onDismiss()
            showIconPicker = true
        },
        existingColor ?: DefaultColors.GRAY.hexValue
    )

    IconPicker(
        showIconPicker,
        selectedIcon = existingIcon ?: AccountIcon.OTHER,
        color = existingColor ?: 0x2E2727,
        onIconChange = {
            updateIcon(it)
        },
        onDismiss = { showIconPicker = false }
    )

}