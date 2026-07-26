package com.example.core.database.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AccountIcon(val imageVector: ImageVector) {
    SAVINGS(Icons.Filled.Savings),
    CHECKING(Icons.Filled.AccountBalance),
    CREDIT_CARD(Icons.Filled.CreditCard),
    CASH(Icons.Filled.AttachMoney),
    INVESTMENT(Icons.AutoMirrored.Filled.TrendingUp),
    OTHER(Icons.Filled.AccountBalance),
}