package com.example.common.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.common.ui.theme.FinancesTheme

@Composable
fun Screen(title: String, content: @Composable (Modifier) -> Unit) {
    Scaffold(
        topBar = { AppBar(title) },
        bottomBar = {}
    ) { padding ->
        content(Modifier.padding(padding))
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    FinancesTheme {
        Screen(title = "Preview Title") {}
    }
}

@Preview(showBackground = true)
@Composable
fun DarkScreenPreview() {
    FinancesTheme(darkTheme = true) {
        Screen(title = "Preview Title") {}
    }
}