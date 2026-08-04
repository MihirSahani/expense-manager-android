package com.example.category.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.common.model.DefaultColors
import com.example.common.ui.component.IconAndRow
import com.example.common.utils.MyText
import com.example.core.database.entity.Category
import com.example.core.database.projection.CategoryWithRemainingBalance

@Composable
fun CategoryItem(
    categoryWithRemainingBalance: CategoryWithRemainingBalance,
    onClick: () -> Unit
) {
    IconAndRow(categoryWithRemainingBalance.category.icon.imageVector, categoryWithRemainingBalance.category.color) {
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
                MyText.RowHeader(categoryWithRemainingBalance.category.name)
                MyText.RowBody(categoryWithRemainingBalance.category.type.name)
            }

            if (categoryWithRemainingBalance.category.budgetPerCycle != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MyText.RowBody("Budget this cycle")
                    MyText.TransactionAmount(categoryWithRemainingBalance.remainingBalance!!)
                }
            }
        }
    }
}