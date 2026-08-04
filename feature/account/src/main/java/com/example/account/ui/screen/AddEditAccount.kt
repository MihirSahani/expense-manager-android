package com.example.account.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.account.ui.components.UpdateAccountContent
import com.example.account.ui.viewmodel.AccountViewModel
import com.example.common.ui.component.ScreenScaffold

@Composable
fun AddEditAccountScreen(vm: AccountViewModel = hiltViewModel()) {
    val currentAccount by vm.account.collectAsStateWithLifecycle()

    ScreenScaffold(
        "Add Account",
        {
            IconButton(
                {
                    currentAccount.let { account ->
                        if (!vm.isNewAccount()) {
                            vm.deleteAccount(account!!.id)
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete Account")
            }
        },
        isLoading = currentAccount == null
    ) {
        UpdateAccountContent(
            account = currentAccount!!,
            onSave = { account ->
                if (vm.isNewAccount()) {
                    vm.createAccount(account)
                }
                else {
                    vm.updateAccount(account)
                }
            },
        )
    }
}

