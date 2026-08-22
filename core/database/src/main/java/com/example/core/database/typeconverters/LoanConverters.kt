package com.example.core.database.typeconverters

import androidx.room3.ColumnTypeConverter
import com.example.core.database.models.LoanType

class LoanConverters {
    @ColumnTypeConverter
    fun fromLoanType(loanType: LoanType): String = loanType.name

    @ColumnTypeConverter
    fun toLoanType(value: String): LoanType = LoanType.valueOf(value)
}