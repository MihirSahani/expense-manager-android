package com.example.setting.ui.screen

import androidx.compose.foundation.clickable
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
import com.example.common.ui.component.ItemAndDivider
import com.example.common.ui.component.ListOfItems
import com.example.common.ui.component.ListWrapper
import com.example.common.ui.component.PickerDialog
import com.example.common.ui.component.ScreenScaffold
import com.example.common.ui.theme.FinancesTheme
import com.example.common.utils.MyText
import com.example.common.utils.toDateTimeString
import com.example.datastore.model.Currency
import com.example.datastore.model.CycleType
import com.example.setting.ui.viewmodel.SettingViewModel
import kotlin.time.Clock

@Composable
fun SettingsScreen(
    onUserProfileClick: () -> Unit,
    vm: SettingViewModel = hiltViewModel(),
) {
    val firstName by vm.firstName.collectAsStateWithLifecycle("")
    val lastName by vm.lastName.collectAsStateWithLifecycle(null)
    val cycleType by vm.cycleType.collectAsStateWithLifecycle(CycleType.entries.first())
    val currency by vm.currency.collectAsStateWithLifecycle(Currency.entries.first())
    val salaryCreditTime by vm.salaryCreditTime.collectAsStateWithLifecycle(0L)

    SettingsScreenContent(
        firstName = firstName,
        lastName = lastName,
        cycleType = cycleType,
        currency = currency,
        salaryCreditTime = salaryCreditTime,
        onUserProfileClick = onUserProfileClick,
        onCycleTypeUpdate = { vm.updateCycleType(it) }
    )
}

@Composable
private fun SettingsScreenContent(
    firstName: String,
    lastName: String?,
    cycleType: CycleType,
    currency: Currency = Currency.entries.first(),
    salaryCreditTime: Long = Clock.System.now().epochSeconds,
    onUserProfileClick: () -> Unit,
    onCycleTypeUpdate: (CycleType) -> Unit = { _ ->  },
    onCurrencyUpdate: (Currency) -> Unit = { _ ->  }
) {
    var showCycleTypeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    PickerDialog(
        title = "Cycle Type",
        show = showCycleTypeDialog,
        items = CycleType.entries.toList(),
        onDismiss = { showCycleTypeDialog = false },
        onItemSelected = { onCycleTypeUpdate(it); showCycleTypeDialog = false }
    )

    PickerDialog(
        title = "Currency",
        show = showCurrencyDialog,
        items = Currency.entries.toList(),
        onDismiss = { showCurrencyDialog = false },
        onItemSelected = { onCurrencyUpdate(it); showCurrencyDialog = false }
    )

    ScreenScaffold("Settings") { padding ->
        ListWrapper(padding) {
            ListOfItems {
                ItemAndDivider(true, modifier = Modifier.clickable { onUserProfileClick() }) {
                    MyText.RowBody("User Profile")
                    MyText.RowHeader("$firstName ${lastName.orEmpty()}")
                }
            }

            ListOfItems {
                ItemAndDivider(true, modifier = Modifier.clickable { showCurrencyDialog = true }) {
                    MyText.RowBody("Currency")
                    MyText.RowHeader(currency.name)
                }

                ItemAndDivider(modifier = Modifier.clickable { showCycleTypeDialog = true }) {
                    MyText.RowBody("Cycle Type")
                    MyText.RowHeader(cycleType.display())
                }

                ItemAndDivider {
                    MyText.RowBody("Salary Credit Time")
                    MyText.RowHeader(salaryCreditTime.toDateTimeString())
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FinancesTheme(true) {
        SettingsScreenContent(
            onUserProfileClick = {},
            firstName = "Jane",
            lastName = null,
            cycleType = CycleType.entries.first(),
        )
    }
}