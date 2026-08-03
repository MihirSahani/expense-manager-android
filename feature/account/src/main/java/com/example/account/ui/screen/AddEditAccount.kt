package com.example.account.ui.screen

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.account.ui.viewmodel.AccountsViewModel
import com.example.common.ui.component.ScreenScaffold

@Composable
fun AddAccountScreen(vm: AccountsViewModel = hiltViewModel()) {

    ScreenScaffold(title = "Add Account") {

    }
}

