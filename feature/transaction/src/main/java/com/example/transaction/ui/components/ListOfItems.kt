package com.example.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun <T> ListOfItems(items: List<T>, itemContent: @Composable (T) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth(),
    ) {
        items(items) { item ->
            val isLast = items.indexOf(item) == items.size - 1
            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                itemContent(item)
                if (!isLast) { HorizontalDivider(Modifier.padding(horizontal = 8.dp)) }
            }
        }
    }
}