package com.example.common.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun <T> LazyListOfItems(items: List<T>, itemContent: @Composable (T) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
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

@Preview(showBackground = true)
@Composable
fun LazyListOfItemsPreview() {
    LazyListOfItems(items = listOf("Item 1", "Item 2", "Item 3")) { item ->
        Text(
            text = item,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}