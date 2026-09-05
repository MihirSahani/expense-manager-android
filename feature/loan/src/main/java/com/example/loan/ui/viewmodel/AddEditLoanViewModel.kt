package com.example.loan.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.LoanRepository
import com.example.common.viewmodel.SaveState
import com.example.core.database.entity.Loan
import com.example.core.database.models.LoanType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * Backs the add/edit loan screen. Loads the loan named by the `id` saved-state argument (or a
 * blank template due in 1 day when creating a new one), and creates, updates, or deletes it.
 */
@HiltViewModel
class AddEditLoanViewModel @Inject constructor(
    private val repository: LoanRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    /** The id of the loan being edited, or `null` when creating a new loan. */
    val loanId: Int? = savedStateHandle["id"]

    /** The loan being edited, or a blank template when [isNewLoan] is `true`. */
    val loan = (loanId?.let { repository.getLoanFlow(it) } ?: flowOf(blankLoan))
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    private val saveState = SaveState()
    /** Whether a save is currently in progress. */
    val isSaving: StateFlow<Boolean> = saveState.isSaving
    /** Error message from the last failed save, or `null` if the last save succeeded. */
    val saveError: StateFlow<String?> = saveState.error

    /**
     * Creates or updates [loan] depending on [isNewLoan], surfacing any error via [saveError].
     *
     * @param loan the loan to save.
     * @param afterSave callback invoked once the save attempt completes (success or failure).
     */
    fun saveLoan(loan: Loan, afterSave: () -> Unit = {}) {
        saveState.launch<Exception>(viewModelScope, always = afterSave) {
            if (isNewLoan()) {
                repository.create(loan)
            } else {
                repository.update(loan)
            }
        }
    }

    /** Deletes the current loan; no-op if [isNewLoan] is `true`. */
    fun deleteLoan() {
        viewModelScope.launch {
            if (!isNewLoan()) {
                repository.deleteById(loanId!!)
            }
        }
    }

    /** Whether this screen instance is creating a new loan rather than editing one. */
    fun isNewLoan(): Boolean {
        return loanId == null
    }

    companion object {
        private val blankLoan = Loan(
            id = 0,
            payee = "",
            amount = 0,
            loanType = LoanType.DEBT,
            loanedDatetime = Clock.System.now().epochSeconds,
            expectedReturnDatetime = Clock.System.now().plus(1.days).epochSeconds
        )
    }
}