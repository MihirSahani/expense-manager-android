package com.example.permission.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.model.DefaultColors
import com.example.common.ui.component.LazyListOfItems
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyInput
import com.example.common.utils.MyText
import com.example.permission.ui.viewmodel.PermissionViewModel

@Composable
fun PermissionScreen(
    navigateToNextScreen: () -> Unit,
    vm: PermissionViewModel = hiltViewModel()
) {
    val mandatory by vm.mandatoryState.collectAsStateWithLifecycle()
    val optional by vm.optionalState.collectAsStateWithLifecycle()
    val isOnboardingDone by vm.isOnboardingDone.collectAsStateWithLifecycle()

    LaunchedEffect(isOnboardingDone) {
        if (isOnboardingDone) navigateToNextScreen()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refreshPermissionsStatus()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PermissionContent(
        mandatory = mandatory,
        optional = optional,
        grantablePermissions = { vm.grantablePermissions() },
        refreshPermissions = { vm.refreshPermissionsStatus() },
        navigateToNextScreen = navigateToNextScreen
    )
}

@Composable
fun PermissionContent(
    mandatory: List<PermissionViewModel.PermissionUiItem>,
    optional: List<PermissionViewModel.PermissionUiItem>,
    grantablePermissions: () -> Array<String> = { emptyArray() },
    refreshPermissions: () -> Unit = { },
    navigateToNextScreen: () -> Unit = { }
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        refreshPermissions()
        if (results.values.all { it }) {
            navigateToNextScreen()
        }
    }

    ScreenScaffold("Permissions") { paddingValues ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LazyListOfItems(mandatory, "Mandatory Permissions") { item ->
                PermissionRow(item)
            }

            LazyListOfItems(optional, "Optional Permissions") { item ->
                PermissionRow(item)
            }

            MyInput.Button(
                "Grant All Permissions",
                onClick = { launcher.launch(grantablePermissions()) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionRow(item: PermissionViewModel.PermissionUiItem) {
    var showDescription by remember(item.showRational) { mutableStateOf(item.showRational) }
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { showDescription = !showDescription }
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MyText.RowHeader(item.info.header)
            if (item.isGranted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = Color(DefaultColors.GREEN.hexValue)
                )
            } else {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Not Granted",
                    tint = Color(DefaultColors.RED.hexValue)
                )
            }
        }
        if (showDescription) {
            MyText.RowBody(item.info.description)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    FinancesTheme {
        PermissionContent(
            mandatory = previewMandatory(),
            optional = previewOptional()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreviewDark() {
    FinancesTheme(true) {
        PermissionContent(
            mandatory = previewMandatory(),
            optional = previewOptional()
        )
    }
}

private fun previewMandatory() = listOf(
    PermissionViewModel.PermissionUiItem(
        PermissionViewModel.PermissionWithInfo("android.permission.READ_SMS", "Read SMS messages", "Read SMS messages to extract transaction data."),
        isGranted = true
    ),
    PermissionViewModel.PermissionUiItem(
        PermissionViewModel.PermissionWithInfo("android.permission.RECEIVE_SMS", "Receive SMS messages", "Receive SMS messages to extract transaction data."),
        isGranted = false,
        showRational = true
    )
)

private fun previewOptional() = listOf(
    PermissionViewModel.PermissionUiItem(
        PermissionViewModel.PermissionWithInfo("android.permission.POST_NOTIFICATIONS", "Post Notifications", "Allow the app to send you notifications about your transactions and account activity."),
        isGranted = false,
        showRational = true
    )
)