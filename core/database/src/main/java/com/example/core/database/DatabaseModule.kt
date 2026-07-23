package com.example.core.database

import android.content.Context
import androidx.room3.Room
import com.example.core.database.dao.AccountDAO
import com.example.core.database.dao.CategoryDAO
import com.example.core.database.dao.TransactionDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "finances.db")
            .build()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDAO = db.transactionDao()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDAO = db.accountDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDAO = db.categoryDao()
}