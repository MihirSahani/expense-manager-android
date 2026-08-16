package com.example.permission.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datastore.Setting
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    repo: Setting
) : ViewModel() {

    class PermissionWithInfo(val permission: String, val header: String, val description: String)

    class PermissionUiItem(val info: PermissionWithInfo, val isGranted: Boolean, val showRational: Boolean = false)

    private val mandatory = listOf(
        PermissionWithInfo(
            Manifest.permission.READ_SMS,
            "Read SMS messages",
            "Read SMS messages to extract transaction data."
        ),
        PermissionWithInfo(
            Manifest.permission.RECEIVE_SMS,
            "Receive SMS messages",
            "Receive SMS messages to extract transaction data."
        )
    )

    private val optional =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                PermissionWithInfo(
                    Manifest.permission.POST_NOTIFICATIONS,
                    "Post Notifications",
                    "Allow the app to send you notifications about your transactions and account activity."
                )
            )
        } else {
            emptyList()
        }

    private val _mandatoryState = MutableStateFlow(mandatory.map { it.toUiItem() })
    val mandatoryState: StateFlow<List<PermissionUiItem>> = _mandatoryState.asStateFlow()

    private val _optionalState = MutableStateFlow(optional.map { it.toUiItem() })
    val optionalState: StateFlow<List<PermissionUiItem>> = _optionalState.asStateFlow()

    val isOnboardingDone: StateFlow<Boolean> = repo.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun refreshPermissionsStatus() {
        _mandatoryState.update { list -> list.map { it.info.toUiItem() } }
        _optionalState.update { list -> list.map { it.info.toUiItem() } }
    }

    fun grantablePermissions(): Array<String> =
        (mandatory + optional).map { it.permission }.toTypedArray()

    fun isPermissionGranted(permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun PermissionWithInfo.toUiItem(): PermissionUiItem {
        val granted = isPermissionGranted(permission)
        return PermissionUiItem(this, isGranted = granted, showRational = !granted)
    }
}