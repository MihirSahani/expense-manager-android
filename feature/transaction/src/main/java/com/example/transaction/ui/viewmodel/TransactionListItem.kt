package com.example.transaction.ui.viewmodel

import com.example.core.database.models.TransactionType
import com.example.core.database.projection.TransactionWithCategory

/** A row in the transaction history list: either a date-group header or a transaction. */
sealed class TransactionListItem {
    /** A date separator inserted between groups of transactions from different days. */
    data class DateHeader(val date: String) : TransactionListItem()

    /** A single transaction row, with its joined category info. */
    data class TransactionItem(val transactionWithCategory: TransactionWithCategory) : TransactionListItem()
}
