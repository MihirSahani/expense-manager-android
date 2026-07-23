package com.example.core.database.typeconverters

import androidx.room3.ColumnTypeConverter
import com.example.core.database.models.TransactionType

class TransactionConverters {
    @ColumnTypeConverter
    fun fromTransactionType(transactionType: TransactionType): String = transactionType.name

    @ColumnTypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}