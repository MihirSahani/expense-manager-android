package com.example.common.repository

import com.example.core.database.dao.AccountDAO
import jakarta.inject.Inject

class AccountRepository @Inject constructor(val dao: AccountDAO) {
    val accounts = dao.getAllAccountsFlow()
}