package com.example.common.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText

@Composable
fun <T> LazyListOfItems(items: List<T>, title: String? = null, content: @Composable (T) -> Unit) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        title?.let {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyText.RowBody(it)
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                val isLast = items.indexOf(item) == items.size - 1
                Row(
                    Modifier
                        .fillMaxWidth()
                        // .padding(horizontal = 16.dp)
                        .heightIn(min = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    content(item)
                }

                if (!isLast) {
                    HorizontalDivider(
                        Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun LazyListOfItemsPreview() {
    FinancesTheme {
        ScreenScaffold("") { paddingValues ->
            Column(
                Modifier.fillMaxWidth().padding(top = paddingValues.calculateTopPadding()).padding(horizontal = 16.dp)
            ) {
                LazyListOfItems(items = listOf("Item 1", "Item 2", "Item 3"), "Lazy List Title") { item ->
                    Text(
                        text = item,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LazyListOfItemsPreviewDark() {
    FinancesTheme(true) {
        ScreenScaffold("") { paddingValues ->
            Column(
                Modifier.fillMaxWidth().padding(top = paddingValues.calculateTopPadding()).padding(horizontal = 16.dp)
            ) {
                LazyListOfItems(items = listOf("Item 1", "Item 2", "Item 3"), "Lazy List Title") { item ->
                    Text(
                        text = item,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}