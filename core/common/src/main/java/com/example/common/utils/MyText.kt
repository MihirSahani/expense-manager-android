package com.example.common.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.common.ui.theme.SamsungTextGrayDark
import com.example.common.ui.theme.SamsungTextGrayLight
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
        fun SecondaryHeader(title: String) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }

        @Composable
        fun RowHeader(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
            Text(
                text = text,
                fontSize = 16.sp,
                modifier = modifier,
                color = color
            )
        }

        @Composable
        fun RowBody(
            text: String,
            modifier: Modifier = Modifier,
            color: Color = if (isSystemInDarkTheme()) SamsungTextGrayDark else SamsungTextGrayLight,
            fontSize: TextUnit = 14.sp
        ) {
            Text(
                modifier = modifier,
                text = text,
                fontSize = fontSize,
                color = color,
            )
        }

        @Composable
        fun TransactionAmount(
            amount: Long,
            type: TransactionType,
            modifier: Modifier = Modifier,
            fontSize: TextUnit = 16.sp,
            color: Color? = null,
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

        fun Double.toIndianFormat(): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2

            return formatter.format(this)
        }
    }


}