package com.example.account.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.AccountRepository
import com.example.common.repository.DebitCardPreferenceRepository
import com.example.common.repository.DebitCardPreferenceValidationException
import com.example.common.repository.TransactionRepository
import com.example.core.database.projection.UnresolvedAccountSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the account-discovery/debit-card-tagging screen. Exposes every raw account/card number
 * seen on imported transactions that isn't linked to an account yet, and every existing account
 * for the debit-card-tagging picker.
 */
@HiltViewModel
class UnresolvedAccountsViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val debitCardPreferenceRepo: DebitCardPreferenceRepository,
) : ViewModel() {
    private val _tagError = MutableStateFlow<String?>(null)

    /**
     * Every raw account/card number not yet linked to an account, or `null` until the first
     * value has loaded. Callers must not treat `null` as "nothing left" (e.g. to auto-finish the
     * flow) — only a loaded, empty list means there is genuinely nothing left to resolve.
     */
    val unresolvedAccounts: StateFlow<List<UnresolvedAccountSummary>?> = transactionRepo.unresolvedAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Every existing account, for the debit-card-tagging picker. */
    val accounts = accountRepo.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Error message from the last failed tag attempt, or `null` if the last attempt succeeded. */
    val tagError: StateFlow<String?> = _tagError.asStateFlow()

    /**
     * Tags a raw account/card number as a debit card belonging to [accountId]: saves the mapping
     * as a [com.example.core.database.entity.DebitCardPreference] and retroactively links every
     * unresolved transaction carrying that raw number to the account.
     *
     * @param rawAccountNo the raw account/card number to tag.
     * @param accountId the account the card should be attributed to.
     */
    fun tagToAccount(rawAccountNo: String, accountId: Int) {
        viewModelScope.launch {
            _tagError.value = null
            try {
                debitCardPreferenceRepo.save(rawAccountNo, accountId)
                transactionRepo.linkRawAccountNoToAccount(rawAccountNo, accountId)
            } catch (e: DebitCardPreferenceValidationException) {
                _tagError.value = e.message
            }
        }
    }

    /** Clears the current [tagError], e.g. after it has been shown to the user. */
    fun clearTagError() {
        _tagError.value = null
    }
}
