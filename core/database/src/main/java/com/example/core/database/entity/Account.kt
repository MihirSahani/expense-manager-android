package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.example.core.database.models.AccountType

@Entity("accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("balance")
    val balance: Long,
    @ColumnInfo("type")
    val type: AccountType,
    @ColumnInfo("account_number")
    val accountNumber: String?,
    @ColumnInfo("color")
    val color: Int?,
    @ColumnInfo("icon")
    val icon: Int?
)