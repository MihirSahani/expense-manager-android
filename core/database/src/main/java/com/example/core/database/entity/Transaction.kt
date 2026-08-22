package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.core.database.models.TransactionType

@Entity(
    "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("category_id"),
        Index("datetime"),
        Index("account_id")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("amount")
    var amount: Long,
    @ColumnInfo("category_id")
    val categoryId: Int?,
    @ColumnInfo("datetime")
    val datetime: Long,
    @ColumnInfo("raw_account_no")
    var rawAccountNo: String?,
    @ColumnInfo("account_id")
    var accountId: Int?,
    @ColumnInfo("payee")
    var payee: String,
    @ColumnInfo("transaction_type")
    var transactionType: TransactionType,
    @ColumnInfo("reference_id")
    var referenceId: String?,
    @ColumnInfo("description")
    val description: String?
)