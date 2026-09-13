package com.example.setting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datastore.Setting
import com.example.datastore.model.Currency
import com.example.datastore.model.CycleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes and updates user profile and app preference settings for the settings screen. */
@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repo : Setting
) : ViewModel() {
    /** The user's first name. */
    val firstName = repo.userFirstName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    /** The user's last name, or `null` if unset. */
    val lastName = repo.userLastName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    /** The currently selected budget cycle type. */
    val cycleType = repo.cycleType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CycleType.entries.first())
    /** The currently selected display currency. */
    val currency = repo.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.entries.first())
    /** Whether onboarding has already been completed in a previous session. */
    val isOnboardingDone = repo.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    /** The epoch-second start of the current salary-date cycle. */
    val salaryCreditTime = repo.salaryCreditTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    /**
     * Updates the user's preferred budget cycle type.
     *
     * @param cycleType the new [CycleType].
     */
    fun updateCycleType(cycleType: CycleType) {
        viewModelScope.launch {
            repo.setCycleType(cycleType)
        }
    }

    /**
     * Updates the user's stored name.
     *
     * @param firstName the user's first name.
     * @param lastName the user's last name, or `null` to clear it.
     */
    fun updateUserName(firstName: String, lastName: String?) {
        viewModelScope.launch {
            repo.setUserFirstName(firstName)
            repo.setUserLastName(lastName)
        }
    }
}