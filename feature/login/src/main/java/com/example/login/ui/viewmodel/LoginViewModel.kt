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

@HiltViewModel
class LoginViewModel @Inject constructor(private val repo: Setting, private val sms: SmsParser): ViewModel() {
    val isOnboardingDone = repo.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _isReadingSms = MutableStateFlow(true)
    val isReadingSms = _isReadingSms.asStateFlow()

    private val _isUserSetupDone = MutableStateFlow(false)
    val isUserSetupDone = _isUserSetupDone.asStateFlow()

    init {
        readAllSms()
    }

    fun setupUserName(firstName: String, lastName: String?) {
        viewModelScope.launch {
            repo.setUserFirstName(firstName)
            repo.setUserLastName(if (lastName == "") null else lastName)
            _isUserSetupDone.value = true
        }
    }

    fun readAllSms() {
        viewModelScope.launch {
            _isReadingSms.value = true
            sms.parseSmses()
            _isReadingSms.value = false
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { repo.setOnboardingDone(true) }
    }
}