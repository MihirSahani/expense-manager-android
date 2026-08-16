package com.example.setting.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.setting.ui.viewmodel.SettingViewModel

@Composable
fun UserProfileScreen(afterSave: () -> Unit = {}) {
    val vm: SettingViewModel = hiltViewModel()
    val firstName by vm.firstName.collectAsStateWithLifecycle("")
    val lastName by vm.lastName.collectAsStateWithLifecycle(null)

    UserProfileContent(
        firstName,
        lastName,
        { firstName, lastName ->
            vm.updateUserName(firstName, lastName)
            afterSave()
        }
    )
}

@Composable
fun UserProfileContent(firstName: String = "", lastName: String? = null, onSave: (String, String?) -> Unit) {
    var firstNameState by remember { mutableStateOf(firstName) }
    var lastNameState by remember { mutableStateOf(lastName) }
    ScreenScaffold("User Profile") { paddingValues ->
        ListWrapper(paddingValues) {
            MyInput.TextField(
                value = firstNameState,
                onValueChange = { firstNameState = it },
                label = "First Name",
                modifier = Modifier.fillMaxWidth()
            )

            MyInput.TextField(
                value = lastNameState ?: "",
                onValueChange = { lastNameState = it },
                label = "Last Name",
                modifier = Modifier.fillMaxWidth()
            )

            MyInput.Button(
                text = "Save",
                onClick = { onSave(firstNameState, lastNameState) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    FinancesTheme(true) {
        UserProfileContent("Jane", null, onSave = { _, _ -> })
    }
}