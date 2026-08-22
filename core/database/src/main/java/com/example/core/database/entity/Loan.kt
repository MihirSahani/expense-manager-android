package com.example.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.example.core.database.models.LoanType

@Entity("loan")
data class Loan (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("payee")
    val payee: String,
    @ColumnInfo("amount")
    var amount: Long,
    @ColumnInfo("loan_type")
    val loanType: LoanType,
    @ColumnInfo("loaned_datetime")
    val loanedDatetime: Long,
    @ColumnInfo("expected_return_datetime")
    val expectedReturnDatetime: Long,
)