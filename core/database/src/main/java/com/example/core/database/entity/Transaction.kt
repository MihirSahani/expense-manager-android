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
        Index("datetime")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("amount")
    val amount: Long,
    @ColumnInfo("category_id")
    val categoryId: Int?,
    @ColumnInfo("datetime")
    val datetime: Long,
    @ColumnInfo("account_id")
    val accountId: Int?,
    @ColumnInfo("payee")
    val payee: String,
    @ColumnInfo("transaction_type")
    val transactionType: TransactionType,
    @ColumnInfo("reference_id")
    val referenceId: Int?,
    @ColumnInfo("description")
    val description: String?
)