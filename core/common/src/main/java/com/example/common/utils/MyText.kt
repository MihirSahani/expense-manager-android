package com.example.common.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.models.TransactionType
import java.text.NumberFormat
import java.util.Locale

class MyText {
    companion object {
        @Composable
        fun ScreenHeader(title: String) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, top = 16.dp)
            )
        }

        @Composable
        fun RowHeader(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = modifier,
                color = color
            )
        }

        @Composable
        fun RowBody(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, modifier: Modifier = Modifier, fontSize: TextUnit = 14.sp) {
            Text(
                text = text,
                fontSize = fontSize,
                color = color,
                modifier = modifier
            )
        }

        @Composable
        fun TransactionAmount(
            amount: Long,
            modifier: Modifier = Modifier,
            fontSize: TextUnit = 16.sp,
            color: Color? = null,
            type: TransactionType
        ) {
            Text(
                text = (amount/100.0).toIndianFormat(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = color
                    ?: if (type == TransactionType.DEBIT || amount < 0) Color(0xFF9B2600)
                    else Color(0xFF02AF34),
                modifier = modifier
            )
        }

        @Composable
        fun Date(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(horizontal = 16.dp),
                color = color
            )
        }

        private fun Double.toIndianFormat(): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2

            return formatter.format(this)
        }
    }


}