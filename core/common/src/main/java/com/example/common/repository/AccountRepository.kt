package com.example.common.repository

import com.example.core.database.dao.AccountDAO
import com.example.core.database.entity.Account
import jakarta.inject.Inject

class AccountRepository @Inject constructor(val dao: AccountDAO) {
    val accounts = dao.getAllAccountsFlow()

    fun getAccountById(id: Int) = dao.getAccountByIdFlow(id)

    suspend fun createAccount(account: Account) = dao.create(account)

    suspend fun updateAccount(account: Account) = dao.update(account)

    suspend fun deleteAccount(id: Int) = dao.deleteById(id)
}