package com.example.account.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.AccountValidationException
import com.example.common.repository.AccountRepository
import com.example.common.repository.TransactionRepository
import com.example.common.viewmodel.SaveState
import com.example.core.database.entity.Account
import com.example.core.database.models.AccountIcon
import com.example.core.database.models.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
/**
 * Backs the add/edit account screen. Loads the account named by the `id` saved-state argument
 * (or a blank template when creating a new one), and creates, updates, or deletes it.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val accountId: Int? = savedStateHandle["id"]
    private val prefillNumber: String? = savedStateHandle["number"]
    private val saveState = SaveState()

    /** Error message from the last failed save, or `null` if the last save succeeded. */
    val saveError: StateFlow<String?> = saveState.error
    /** Whether a save is currently in progress. */
    val isSaving: StateFlow<Boolean> = saveState.isSaving

    /**
     * The account being edited, or a blank template when [isNewAccount] is `true`. The blank
     * template's account number is pre-filled from the `number` saved-state argument when
     * present (e.g. when arriving from account-discovery with a raw account number to assign).
     */
    val account = (accountId?.let { repo.getAccountByIdFlow(it) }
        ?: flowOf(blankAccount.copy(accountNumber = prefillNumber)))
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    /** Whether this screen instance is creating a new account rather than editing one. */
    fun isNewAccount(): Boolean {
        return accountId == null
    }

    /**
     * Creates or updates [account] depending on [isNewAccount], surfacing any validation error
     * via [saveError]. When the saved account has a non-null [Account.accountNumber], any
     * transactions previously imported with a matching raw account number but no linked account
     * are retroactively backfilled to it (see
     * [TransactionRepository.linkRawAccountNoToAccount]).
     *
     * @param account the account to save.
     * @param updateBalance forwarded to [AccountRepository.updateAccount] when editing; ignored
     * when creating.
     * @param afterSave callback invoked once the save attempt succeeds.
     */
    fun saveAccount(account: Account, updateBalance: Boolean = true, afterSave: () -> Unit) {
        saveState.launch<AccountValidationException>(viewModelScope) {
            val savedAccountId = if (isNewAccount()) {
                repo.createAccount(account)
            } else {
                repo.updateAccount(account, updateBalance)
                account.id
            }
            account.accountNumber?.let { accountNumber ->
                transactionRepo.linkRawAccountNoToAccount(accountNumber, savedAccountId)
            }
            afterSave()
        }
    }

    /** Clears the current [saveError], e.g. after it has been shown to the user. */
    fun clearSaveError() {
        saveState.clearError()
    }

    /**
     * Deletes an account by id.
     *
     * @param id the account id to delete.
     */
    fun deleteAccount(id: Int) {
        viewModelScope.launch {
            repo.deleteAccount(id)
        }
    }
    companion object {
        private val blankAccount = Account(
            name = "",
            balance = 0,
            type = AccountType.OTHER,
            accountNumber = null,
            color = null,
            icon = AccountIcon.OTHER
        )
    }
}