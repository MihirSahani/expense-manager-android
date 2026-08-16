package com.example.common.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.ui.theme.FinancesTheme

@Composable
fun ScreenScaffold(title: String, icon: @Composable () -> Unit = {}, floatingActionButton: @Composable (Modifier) -> Unit = {}, isLoading: Boolean = false, content: @Composable (PaddingValues) -> Unit) {
    if(isLoading) {
        Scaffold(
            topBar = { AppBar(title, icon) },
            floatingActionButton = {
                floatingActionButton(
                    Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(30))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                        .size(32.dp)
                )
            },
        ) { padding ->
            Box(
                Modifier
                    .padding(padding.calculateTopPadding())
                    .fillMaxSize()
                ,
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
    else {
        Scaffold(
            topBar = { AppBar(title, icon) },
            floatingActionButton = {
                floatingActionButton(
                    Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(30))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                        .size(32.dp)
                )
            },
        ) { padding ->
            content(padding)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenScaffoldPreview() {
    FinancesTheme {
        ScreenScaffold(
            title = "Preview Title",
            icon = {
                Icon(Icons.Filled.History, contentDescription = "History Icon")
            },
            floatingActionButton = { modifier ->
                Icon(
                    Icons.Filled.Today,
                    contentDescription = "Today Icon",
                    modifier = modifier
                )
            }
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenScaffoldPreviewDark() {
    FinancesTheme(darkTheme = true) {
        ScreenScaffold(
            title = "Preview Title",
            icon = {
                Icon(Icons.Filled.Today, contentDescription = "Today Icon")
            },
            floatingActionButton = { modifier ->
                Icon(Icons.Filled.Today, contentDescription = "Today Icon", modifier = modifier)
            },
            isLoading = true
        ) {}
    }
}