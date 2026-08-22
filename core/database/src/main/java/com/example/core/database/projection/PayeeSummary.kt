package com.example.core.database.projection

import androidx.room3.ColumnInfo
import com.example.core.database.models.TransactionType

/**
 * A distinct payee/transaction-type pair with no assigned category, summarized for
 * payee-category-discovery UI. Rows are aggregated over every uncategorized transaction sharing
 * the same payee and [TransactionType] within a cycle window.
 */
data class PayeeSummary(
    @ColumnInfo(name = "payee")
    val payee: String,
    @ColumnInfo(name = "transaction_type")
    val transactionType: TransactionType,
    @ColumnInfo(name = "transaction_count")
    val transactionCount: Int,
    @ColumnInfo(name = "total_amount")
    val totalAmount: Long,
    @ColumnInfo(name = "last_transaction_time")
    val lastTransactionTime: Long
)
