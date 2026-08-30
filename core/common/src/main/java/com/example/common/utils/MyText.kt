package com.example.common.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.models.DefaultColors
import com.example.core.database.models.TransactionType
import com.example.common.ui.theme.SamsungTextGrayDark
import com.example.common.ui.theme.SamsungTextGrayLight
import com.example.core.database.models.LoanType
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class MyText {
    companion object {
        @Composable
        fun ScreenHeader(title: String) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }

        @Composable
        fun SecondaryHeader(title: String, color: Color? = null, modifier: Modifier = Modifier) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color ?: MaterialTheme.colorScheme.onSurface,
                modifier = modifier.padding(horizontal = 8.dp)
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
                text = abs(amount/100.0).toIndianFormat(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = color ?:
                if (type == TransactionType.DEBIT || amount < 0)
                    Color(DefaultColors.RED.hexValue)
                else
                    Color(DefaultColors.GREEN.hexValue),
                modifier = modifier
            )
        }

        @Composable
        fun TransactionAmount(
            amount: Long,
            type: LoanType,
            modifier: Modifier = Modifier,
            fontSize: TextUnit = 16.sp,
            color: Color? = null,
        ) {
            Text(
                text = abs(amount/100.0).toIndianFormat(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = color ?:
                if (type == LoanType.DEBT|| amount < 0)
                    Color(DefaultColors.RED.hexValue)
                else
                    Color(DefaultColors.GREEN.hexValue),
                modifier = modifier
            )
        }

        @Composable
        fun TransactionAmount(
            amount: Long,
            modifier: Modifier = Modifier,
            fontSize: TextUnit = 16.sp,
            color: Color? = null,
        ) {
            Text(
                text = abs(amount/100.0).toIndianFormat(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = color ?:
                if (amount < 0)
                    Color(DefaultColors.RED.hexValue)
                else
                    Color(DefaultColors.GREEN.hexValue),
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

@Preview(showBackground = true)
@Composable
fun MyTextPreview() {
    Column {
        MyText.ScreenHeader("Screen Header")
        MyText.SecondaryHeader("Secondary Header")
        MyText.RowHeader("Row Header")
        MyText.RowBody("Row Body")
        MyText.TransactionAmount(123456L, TransactionType.CREDIT)
        MyText.TransactionAmount(123456L, TransactionType.DEBIT)
        MyText.TransactionAmount(123456L, LoanType.CREDIT)
        MyText.TransactionAmount(123456L, LoanType.DEBT)
        MyText.TransactionAmount(123456L)
        MyText.Date("Date")
    }

}