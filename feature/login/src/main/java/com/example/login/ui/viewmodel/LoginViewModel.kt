package com.example.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.datastore.Setting
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import com.example.sms.SmsParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Backs the onboarding/login flow: parses existing SMS transactions in the background, then
 * collects the user's name and marks onboarding complete.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(private val repo: Setting, private val sms: SmsParser): ViewModel() {
    /** Whether onboarding has already been completed in a previous session. */
    val isOnboardingDone = repo.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _isReadingSms = MutableStateFlow(true)
    /** Whether SMS parsing is currently in progress. */
    val isReadingSms = _isReadingSms.asStateFlow()

    private val _isUserSetupDone = MutableStateFlow(false)
    /** Whether the user's name has been collected and saved. */
    val isUserSetupDone = _isUserSetupDone.asStateFlow()

    init {
        readAllSms()
    }

    /**
     * Saves the user's name and marks [isUserSetupDone].
     *
     * @param firstName the user's first name.
     * @param lastName the user's last name, or `null`/blank to leave it unset.
     */
    fun setupUserName(firstName: String, lastName: String?) {
        viewModelScope.launch {
            repo.setUserFirstName(firstName)
            repo.setUserLastName(if (lastName == "") null else lastName)
            _isUserSetupDone.value = true
        }
    }

    /** Parses all SMS messages for transactions, tracking progress via [isReadingSms]. */
    fun readAllSms() {
        viewModelScope.launch {
            _isReadingSms.value = true
            sms.parseSmses()
            _isReadingSms.value = false
        }
    }
}