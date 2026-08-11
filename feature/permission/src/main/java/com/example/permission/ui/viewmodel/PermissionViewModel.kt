package com.example.permission.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(private val context: Context): ViewModel() {

    class PermissionsWithDescription(val permission: String, val header: String, val description: String)

    val mandatoryPermissions = listOf(
        PermissionsWithDescription(Manifest.permission.READ_SMS, "Read SMS messages", "Read SMS messages to extract transaction data."),
        PermissionsWithDescription(Manifest.permission.RECEIVE_SMS, "Receive SMS messages", "Receive SMS messages to extract transaction data.")
    )

    fun hasMandatoryPermission(): Boolean {
        return mandatoryPermissions.all { permission ->
            context.checkSelfPermission(permission.permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}