package com.example.common.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt


class MyInput {
    companion object {
        @Composable
        fun TextField(
            value: String,
            onValueChange: (String) -> Unit,
            label: String,
            modifier: Modifier = Modifier,
            placeholder: String = "",
            leadingIcon: @Composable (() -> Unit)? = null,
            trailingIcon: @Composable (() -> Unit)? = null,
            isError: Boolean = false,
            errorMessage: String = "",
            keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
            singleLine: Boolean = true,
            readOnly: Boolean = false,
            enabled: Boolean = true
        ) {
            Column(modifier = modifier) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = {
                        Text(
                            text = label,
                            modifier = Modifier.graphicsLayer {
                                scaleX = 0.7f
                                scaleY = 0.7f
                                transformOrigin = TransformOrigin(0.3f, 0.3f)
                            }
                        )
                    },
                    placeholder = { Text(placeholder) },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    isError = isError,
                    singleLine = singleLine,
                    readOnly = readOnly,
                    enabled = enabled,
                    keyboardOptions = keyboardOptions,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,

                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,

                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,

                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                        cursorColor = MaterialTheme.colorScheme.primary,

                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        errorLeadingIconColor = MaterialTheme.colorScheme.error,
                        errorTrailingIconColor = MaterialTheme.colorScheme.error,
                        errorCursorColor = MaterialTheme.colorScheme.error,

                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )

                if (isError && errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }

        @Composable
        fun Switch(
            checked: Boolean,
            onCheckedChange: (Boolean) -> Unit,
            modifier: Modifier = Modifier,
            enabled: Boolean = true
        ) {
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides Dp.Unspecified
            ) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                modifier = modifier.scaledSize(0.7f),
                colors = SwitchDefaults.colors(
                    // Checked (ON) state
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedBorderColor = Color.Transparent,
                    checkedIconColor = MaterialTheme.colorScheme.primary,

                    // Unchecked (OFF) state
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    uncheckedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    // Disabled state
                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.38f),
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                    disabledUncheckedThumbColor = Color.White.copy(alpha = 0.38f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                )
            )
            }
        }

        @Composable
        fun Button(
            text: String,
            onClick: () -> Unit,
            modifier: Modifier = Modifier,
            enabled: Boolean = true,
            leadingIcon: @Composable (() -> Unit)? = null
        ) {
            Box(
                modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = enabled) { onClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                MyText.RowHeader(text, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        /**
         * A compact, wrap-content button for secondary/inline actions (e.g. a top-bar action
         * next to a screen title), as opposed to [Button] which always fills the available width
         * and uses a larger header-sized label.
         */
        @Composable
        fun SmallButton(
            text: String,
            onClick: () -> Unit,
            modifier: Modifier = Modifier,
            enabled: Boolean = true
        ) {
            Box(
                modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = enabled) { onClick() }
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                MyText.RowBody(text, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
            }
        }
    }
}

private fun Modifier.scaledSize(scale: Float): Modifier = this
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val width = (placeable.width * scale).roundToInt()
        val height = (placeable.height * scale).roundToInt()
        layout(width, height) {
            placeable.place(
                (width - placeable.width) / 2,
                (height - placeable.height) / 2
            )
        }
    }
    .scale(scale)