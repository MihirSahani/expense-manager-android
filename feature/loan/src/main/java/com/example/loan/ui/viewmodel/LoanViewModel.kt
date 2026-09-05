package com.example.loan.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.common.repository.LoanRepository
import com.example.common.utils.toDateString
import com.example.core.database.entity.Loan
import com.example.core.database.models.LoanType
import com.example.loan.ui.component.LoanListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Exposes paged loan-history lists for the loan history screen, grouped into loans taken
 * (debts) and loans given (credits), each with date-header separators inserted between days.
 */
@HiltViewModel
class LoanViewModel @Inject constructor(
    repository: LoanRepository
) : ViewModel() {
    /** Paged list of debt loans (money the user owes), with date headers, cached in [viewModelScope]. */
    val loansTaken: Flow<PagingData<LoanListItem>> = repository.getLoansPaged(LoanType.DEBT)
        .map { pagingSource ->
            pagingSource
                .map { loan -> LoanListItem.LoanItem(loan) }
                .insertSeparators { before, after ->
                    val beforeLabel = before?.loan?.loanedDatetime?.toDateString()
                    val afterLabel = after?.loan?.loanedDatetime?.toDateString()

                    when {
                        afterLabel != null && beforeLabel != afterLabel -> {
                            LoanListItem.DateHeader(afterLabel)
                        }
                        else -> {
                            null
                        }
                    }
                }

        }.cachedIn(viewModelScope)

    /** Paged list of credit loans (money owed to the user), with date headers, cached in [viewModelScope]. */
    val loansGiven: Flow<PagingData<LoanListItem>> = repository.getLoansPaged(LoanType.CREDIT)
        .map { pagingSource ->
            pagingSource.map { loan -> LoanListItem.LoanItem(loan) }
                .insertSeparators { before, after ->
                    val beforeLabel = before?.loan?.loanedDatetime?.toDateString()
                    val afterLabel = after?.loan?.loanedDatetime?.toDateString()

                    when {
                        afterLabel != null && beforeLabel != afterLabel -> {
                            LoanListItem.DateHeader(afterLabel)
                        }
                        else -> {
                            null
                        }
                    }
                }
        }.cachedIn(viewModelScope)

}