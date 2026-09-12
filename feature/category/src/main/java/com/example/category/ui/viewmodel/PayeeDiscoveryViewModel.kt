package com.example.category.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.repository.TransactionRepository
import com.example.core.database.projection.PayeeSummary
import com.example.datastore.Setting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the payee-category-discovery screen. Exposes every uncategorized payee seen in the
 * current cycle; a payee drops out of the list as soon as it's assigned a category.
 */
@HiltViewModel
class PayeeDiscoveryViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val setting: Setting,
) : ViewModel() {
    /**
     * Every uncategorized payee/transaction-type pair for the current cycle, or `null` until
     * the first value has loaded. Callers must not treat `null` as "nothing left" (e.g. to
     * auto-finish the flow) — only a loaded, empty list means there is genuinely nothing left
     * to categorize.
     */
    val payees: StateFlow<List<PayeeSummary>?> = transactionRepo.getCurrentCyclePayees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Every category, for the category-picker dialog. */
    val categories = transactionRepo.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Assigns [categoryId] to [payee]/[transactionType]: remembers the preference and
     * retroactively re-tags every matching transaction, causing the payee to drop out of
     * [payees] on the next emission.
     */
    fun assignCategory(payee: PayeeSummary, categoryId: Int) {
        viewModelScope.launch {
            transactionRepo.updateAndRememberCategoryForPayee(payee.payee, payee.transactionType, categoryId)
        }
    }

    /**
     * Marks onboarding as complete. Called once the user finishes the payee-discovery flow that
     * follows account-discovery during initial SMS setup.
     */
    fun completeOnboarding() {
        viewModelScope.launch { setting.setOnboardingDone(true) }
    }
}
