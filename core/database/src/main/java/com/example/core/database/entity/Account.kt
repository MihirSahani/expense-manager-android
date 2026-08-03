package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType

@Entity("accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("name")
    var name: String,
    @ColumnInfo("balance")
    var balance: Long,
    @ColumnInfo("type")
    var type: AccountType,
    @ColumnInfo("account_number")
    var accountNumber: String?,
    @ColumnInfo("color")
    var color: Int?,
    @ColumnInfo("icon")
    var icon: AccountIcon
)