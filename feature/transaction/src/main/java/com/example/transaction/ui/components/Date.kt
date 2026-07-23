package com.example.transaction.ui.components

import android.view.Surface
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.tooling.preview.Preview
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText

@Composable
fun Date(date: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MyText.Date(date)
        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
fun DatePreview() {
    Date(date = "Sun, 01 Jan, 2023")
}

@Preview(showBackground = true)
@Composable
fun DatePreviewDark() {
    FinancesTheme(darkTheme = true) {
        Surface {
            Date(date = "Sun, 01 Jan, 2023")
        }
    }
}