package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity("transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("amount")
    val amount: Long,
    @ColumnInfo("category_id")
    val categoryId: Int,
    @ColumnInfo("datetime")
    val datetime: Long,
    @ColumnInfo("account_id")
    val accountId: Int,
    @ColumnInfo("transaction_type")
    val transactionType: Int,
    @ColumnInfo("reference_id")
    val referenceId: Int?,
    @ColumnInfo("description")
    val description: String?
)