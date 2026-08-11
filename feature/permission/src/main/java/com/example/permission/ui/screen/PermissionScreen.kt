package com.example.permission.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.common.ui.component.LazyListOfItems
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.example.permission.ui.viewmodel.PermissionViewModel

@Composable
fun PermissionScreen(
    navigateToNextScreen: () -> Unit,
    vm: PermissionViewModel = hiltViewModel()
) {
    PermissionContent(vm.mandatoryPermissions)
}

@Composable
fun PermissionContent(permissions: List<PermissionViewModel.PermissionsWithDescription>) {
    ScreenScaffold("Permissions") { paddingValues ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LazyListOfItems(permissions) { permission ->
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

                }
                MyText.RowHeader(permission.header)
                MyText.RowBody(permission.description)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    FinancesTheme {
        PermissionContent(
            listOf(
                PermissionViewModel.PermissionsWithDescription("android.permission.READ_SMS", "Read SMS messages", "Read SMS messages to extract transaction data."),
                PermissionViewModel.PermissionsWithDescription("android.permission.RECEIVE_SMS", "Receive SMS messages", "Receive SMS messages to extract transaction data.")
            )
        )
    }
}