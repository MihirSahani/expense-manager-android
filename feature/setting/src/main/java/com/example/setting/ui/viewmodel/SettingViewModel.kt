package com.example.setting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datastore.Setting
import com.example.datastore.model.Currency
import com.example.datastore.model.CycleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repo : Setting
) : ViewModel() {
    val firstName = repo.userFirstName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val lastName = repo.userLastName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val cycleType = repo.cycleType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CycleType.entries.first())
    val currency = repo.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.entries.first())
    val isOnboardingDone = repo.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val salaryCreditTime = repo.salaryCreditTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun updateCycleType(cycleType: CycleType) {
        viewModelScope.launch {
            repo.setCycleType(cycleType)
        }
    }

    fun updateUserName(firstName: String, lastName: String?) {
        viewModelScope.launch {
            repo.setUserFirstName(firstName)
            repo.setUserLastName(lastName)
        }
    }
}