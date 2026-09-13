package com.example.login.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.login.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navigateToHome: () -> Unit,
    navigateToAccountDiscovery: () -> Unit,
    vm: LoginViewModel = hiltViewModel()
) {
    val isReadingSms by vm.isReadingSms.collectAsStateWithLifecycle(initialValue = true)
    val isUserSetupDone by vm.isUserSetupDone.collectAsStateWithLifecycle(initialValue = false)
    val isOnboardingDone by vm.isOnboardingDone.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(isOnboardingDone) {
        if (isOnboardingDone) navigateToHome()
    }

    LaunchedEffect(isUserSetupDone, isReadingSms) {
        if (isUserSetupDone && !isReadingSms) navigateToAccountDiscovery()
    }

    if (isUserSetupDone) {
        ReadingSmsContent()
    } else {
        LoginContent { firstName, lastName ->
            vm.setupUserName(firstName, lastName)
        }
    }
}

@Composable
fun ReadingSmsContent() {
    ScreenScaffold("Setup Your Profile") { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Your SMS are still being read\u2026")
        }
    }
}

@Composable
fun LoginContent(onSave: (String, String) -> Unit = { _, _ -> }) {
    var userFirstName by remember { mutableStateOf("") }
    var userLastName by remember { mutableStateOf("") }

    ScreenScaffold("Setup Your Profile") { paddingValues ->
        ListWrapper(paddingValues) {
            MyInput.TextField(
                value = userFirstName,
                onValueChange = { userFirstName = it },
                label = "First Name"
            )

            MyInput.TextField(
                value = userLastName,
                onValueChange = { userLastName = it },
                label = "Last Name"
            )

            MyInput.Button(
                "Save Profile",
                onClick = {
                    onSave(userFirstName.trim(), userLastName.trim())
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreviewDark() {
    FinancesTheme(true) {
        LoginContent()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    FinancesTheme {
        LoginContent()
    }
}

@Preview(showBackground = true)
@Composable
fun ReadingSmsContentPreview() {
    FinancesTheme {
        ReadingSmsContent()
    }
}

@Preview(showBackground = true)
@Composable
fun ReadingSmsContentPreviewDark() {
    FinancesTheme(true) {
        ReadingSmsContent()
    }
}