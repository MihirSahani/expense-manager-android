package com.example.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.datastore.Setting
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(private val repo: Setting): ViewModel() {
    val isOnboardingDone = repo.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setupUserName(firstName: String, lastName: String?) {
        viewModelScope.launch {
            repo.setUserFirstName(firstName)
            repo.setUserLastName(if (lastName == "") null else lastName)
        }
    }
}