package com.example.transaction.ui.viewmodel

import com.example.core.database.models.TransactionType
import com.example.core.database.projection.TransactionWithCategory

sealed class TransactionListItem {
    data class DateHeader(val date: String) : TransactionListItem()

    data class TransactionItem(val transactionWithCategory: TransactionWithCategory) : TransactionListItem()
}
