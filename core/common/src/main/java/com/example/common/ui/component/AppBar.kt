package com.example.common.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText

@Composable
fun AppBar(title: String, icon: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MyText.ScreenHeader(title)
        icon()
    }
}

@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    FinancesTheme {
        AppBar(title = "Preview Title") {
            Icon(imageVector = Icons.Filled.History, contentDescription = "History Icon")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DarkAppBarPreview() {
    FinancesTheme(darkTheme = true) {
        Surface {
            AppBar(title = "Preview Title") {
                Icon(imageVector = Icons.Filled.Today, contentDescription = "Today Icon")
            }
        }
    }
}