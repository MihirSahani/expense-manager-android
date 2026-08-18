package com.example.common.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.core.database.models.DefaultColors

@Composable
fun IconAndRow(icon: ImageVector?, bgColor: Int?, row: @Composable () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(bgColor ?: DefaultColors.GRAY.hexValue)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon ?: Icons.Default.Warning,
                contentDescription = null,
                tint = if(icon != null) Color.Black else MaterialTheme.colorScheme.error
            )
        }

        row()
    }
}