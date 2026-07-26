package com.example.common.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.common.ui.theme.FinancesTheme

@Composable
fun ScreenScaffold(title: String, icon: @Composable () -> Unit = {}, content: @Composable (Modifier) -> Unit) {
    Scaffold(
        topBar = { AppBar(title, icon) }
    ) { padding ->
        content(Modifier.padding(padding))
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
            }
        ) {}
    }
}