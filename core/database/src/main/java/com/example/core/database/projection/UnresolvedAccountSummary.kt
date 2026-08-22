package com.example.core.database.projection

import androidx.room3.ColumnInfo

/**
 * A raw account/card number seen on imported transactions that has not yet been linked to an
 * [com.example.core.database.entity.Account], summarized for account-discovery UI.
 */
data class UnresolvedAccountSummary(
    @ColumnInfo(name = "raw_account_no")
    val rawAccountNo: String,
    @ColumnInfo(name = "transaction_count")
    val transactionCount: Int
)
