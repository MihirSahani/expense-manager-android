package com.example.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.example.common.utils.MyText.Companion.toIndianFormat

@Composable
fun NetBalanceDisplay(netBalance: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText.SecondaryHeader("Net Balance:")
            MyText.SecondaryHeader((netBalance / 100.0).toIndianFormat())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NetBalanceDisplayPreview() {
    FinancesTheme {
        NetBalanceDisplay(netBalance = 1000000)
    }
}

@Preview(showBackground = true)
@Composable
fun NetBalanceDisplayPreviewDark() {
    FinancesTheme(darkTheme = true) {
        Surface {
            NetBalanceDisplay(netBalance = 1000000)
        }
    }
}