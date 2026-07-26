package com.example.core.database.typeconverters

import androidx.room3.ColumnTypeConverter
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType

class AccountConverters {
    @ColumnTypeConverter
    fun fromAccountType(accountType: AccountType): String = accountType.name

    @ColumnTypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

   @ColumnTypeConverter
   fun fromAccountIcon(icon: AccountIcon): String = icon.name

    @ColumnTypeConverter
    fun toAccountIcon(value: String): AccountIcon = AccountIcon.valueOf(value)
}