package com.example.loan.ui.component

import com.example.core.database.entity.Loan

sealed class LoanListItem {
    data class DateHeader(val date: String): LoanListItem()
    data class LoanItem(val loan: Loan): LoanListItem()
}