package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "debit_card_preferences",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("account_id")]
)
data class DebitCardPreference(
    @PrimaryKey
    @ColumnInfo("card_number")
    val cardNumber: String,
    @ColumnInfo("account_id")
    val accountId: Int
)
