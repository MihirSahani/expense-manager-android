package com.example.core.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.RoomDatabase
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.example.core.database.dao.AccountDAO
import com.example.core.database.dao.CategoryDAO
import com.example.core.database.dao.DebitCardPreferenceDAO
import com.example.core.database.dao.LoanDAO
import com.example.core.database.dao.PayeeCategoryPreferenceDAO
import com.example.core.database.dao.TransactionDAO
import com.example.core.database.entity.Account
import com.example.core.database.entity.Category
import com.example.core.database.entity.DebitCardPreference
import com.example.core.database.entity.Loan
import com.example.core.database.entity.PayeeCategoryPreference
import com.example.core.database.entity.Transaction
import com.example.core.database.typeconverters.AccountConverters
import com.example.core.database.typeconverters.CategoryConverters
import com.example.core.database.typeconverters.LoanConverters
import com.example.core.database.typeconverters.TransactionConverters

@Database(
    entities = [
        Transaction::class,
        Account::class,
        Category::class,
        DebitCardPreference::class,
        Loan::class,
        PayeeCategoryPreference::class
    ],
    version = 1,
    exportSchema = false
)
@ColumnTypeConverters(
    AccountConverters::class,
    CategoryConverters::class,
    TransactionConverters::class,
    LoanConverters::class
)
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun transactionDao(): TransactionDAO
    abstract fun accountDao(): AccountDAO
    abstract fun categoryDao(): CategoryDAO
    abstract fun debitCardPreferenceDao(): DebitCardPreferenceDAO
    abstract fun loanDao(): LoanDAO
    abstract fun payeeCategoryPreferenceDao(): PayeeCategoryPreferenceDAO
}