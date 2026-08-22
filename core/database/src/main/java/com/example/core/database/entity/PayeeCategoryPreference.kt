package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import com.example.core.database.models.TransactionType

@Entity(
    tableName = "payee_category_preferences",
    primaryKeys = ["payee", "transaction_type"],
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("category_id"),
        Index(value = ["payee", "transaction_type"], unique = true)
    ]
)
data class PayeeCategoryPreference(
    @ColumnInfo("payee")
    val payee: String,
    @ColumnInfo("transaction_type")
    val transactionType: TransactionType,
    @ColumnInfo("category_id")
    val categoryId: Int
)
