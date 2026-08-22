package com.example.core.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.example.core.database.dao.AccountDAO
import com.example.core.database.dao.CategoryDAO
import com.example.core.database.dao.DebitCardPreferenceDAO
import com.example.core.database.dao.LoanDAO
import com.example.core.database.dao.PayeeCategoryPreferenceDAO
import com.example.core.database.dao.TransactionDAO
import com.example.core.database.models.CategoryIcon
import com.example.core.database.models.CategoryType
import com.example.core.database.models.DefaultColors
import com.example.core.database.models.normalizeAccountIdentifier
import com.example.core.database.models.normalizePayee
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private data class DefaultCategory(
        val name: String,
        val type: CategoryType,
        val icon: CategoryIcon,
        val color: DefaultColors
    )

    private val DEFAULT_CATEGORIES = listOf(
        DefaultCategory("Food", CategoryType.EXPENSE, CategoryIcon.FOOD, DefaultColors.ORANGE),
        DefaultCategory("Transport", CategoryType.EXPENSE, CategoryIcon.TRANSPORT, DefaultColors.INDIGO),
        DefaultCategory("Groceries", CategoryType.EXPENSE, CategoryIcon.GROCERIES, DefaultColors.GREEN),
        DefaultCategory("Utilities", CategoryType.EXPENSE, CategoryIcon.UTILITIES, DefaultColors.TEAL),
        DefaultCategory("Shopping", CategoryType.EXPENSE, CategoryIcon.SHOPPING, DefaultColors.PINK),
        DefaultCategory("Friends & Family", CategoryType.EXPENSE, CategoryIcon.FRIENDS_AND_FAMILIES, DefaultColors.PURPLE),
        DefaultCategory("Salary", CategoryType.INCOME, CategoryIcon.SALARY, DefaultColors.EMERALD),
        DefaultCategory("Investment", CategoryType.EXPENSE, CategoryIcon.INVESTMENT, DefaultColors.GOLD),
    )

    private val seedCallback = object : RoomDatabase.Callback() {
        override suspend fun onCreate(connection: SQLiteConnection) {
            for (category in DEFAULT_CATEGORIES) {
                connection.execSQL(
                    "INSERT INTO categories (name, type, budget_per_cycle, color, icon) " +
                        "VALUES ('${category.name.replace("'", "''")}', " +
                        "'${category.type.name}', NULL, ${category.color.hexValue}, '${category.icon.name}')"
                )
            }
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "finances.db")
            .addCallback(seedCallback)
            .build()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDAO = db.transactionDao()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDAO = db.accountDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDAO = db.categoryDao()

    @Provides
    fun provideDebitCardPreferenceDao(
        db: AppDatabase
    ): DebitCardPreferenceDAO = db.debitCardPreferenceDao()

    @Provides
    fun provideLoanDao(db: AppDatabase): LoanDAO = db.loanDao()

    @Provides
    fun providePayeeCategoryPreferenceDao(db: AppDatabase): PayeeCategoryPreferenceDAO = db.payeeCategoryPreferenceDao()
}