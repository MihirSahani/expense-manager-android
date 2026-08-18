package com.example.category.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.common.ui.component.IconAndRow
import com.example.common.utils.MyText
import com.example.core.database.models.TransactionType
import com.example.core.database.projection.CategoryWithInfo

@Composable
fun CategoryItem(
    categoryWithInfo: CategoryWithInfo,
    onClick: () -> Unit
) {
    IconAndRow(categoryWithInfo.category.icon.imageVector, categoryWithInfo.category.color) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyText.RowHeader(categoryWithInfo.category.name)
                MyText.RowBody(categoryWithInfo.category.type.name)
            }

            if (categoryWithInfo.category.budgetPerCycle != null && categoryWithInfo.remainingBalance != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        categoryWithInfo.remainingBalance!! > 0 -> {
                            MyText.RowBody("Remaining Balance")
                            MyText.TransactionAmount(categoryWithInfo.remainingBalance!!)
                        }
                        categoryWithInfo.remainingBalance!! <= 0 -> {
                            MyText.RowBody("Overspent this cycle (Total)")
                            MyText.TransactionAmount(categoryWithInfo.spent ?: 0L, TransactionType.DEBIT)
                        }
                    }
                }
            }
            else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MyText.RowBody("Spent this cycle")
                    MyText.TransactionAmount(categoryWithInfo.spent?: 0L)
                }
            }
        }
    }
}