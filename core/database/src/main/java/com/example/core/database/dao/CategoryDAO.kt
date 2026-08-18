package com.example.core.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Category
import com.example.core.database.projection.CategoryWithInfo
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDAO {
    // ----------------------------- Fetching Categories -----------------------------
    @Query("SELECT * FROM categories WHERE id = :id")
    abstract suspend fun getCategory(id: Int): Category?

    @Query("SELECT * FROM categories WHERE id = :id")
    abstract fun getCategoryFlow(id: Int): Flow<Category?>

    @Query("SELECT * FROM categories")
    abstract fun getAllCategoriesFlow(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    abstract fun getAllCategories(): PagingSource<Int, Category>

    @Query("""
        SELECT c.*, 
               (c.budget_per_cycle - IFNULL(SUM(t.amount), 0)) AS remaining_balance,
               SUM(t.amount) AS spent
        FROM categories c
        LEFT JOIN transactions t ON t.category_id = c.id AND t.datetime BETWEEN :start AND :end
        GROUP BY c.id
    """)
    abstract fun getCategoriesWithInfoFlow(start: Long = 0L, end: Long = Long.MAX_VALUE): Flow<List<CategoryWithInfo>>

    // TODO: implement "categories at risk" — categories whose spend this cycle exceeds
    //  budget_per_cycle. Needs a JOIN on transactions + the cycle window (start, end):
    //  SELECT c.* FROM categories c JOIN transactions t ON t.category_id = c.id
    //  WHERE t.datetime BETWEEN :start AND :end
    //  GROUP BY c.id HAVING SUM(t.amount) > c.budget_per_cycle
    //  Return Flow<List<Category>> (not suspend).

    // ---------------------------- Create --------------------------------------------
    @Insert
    abstract suspend fun create(category: Category)

    // ---------------------------- Update --------------------------------------------
    @Update
    abstract suspend fun update(category: Category)

    @Query("UPDATE categories SET budget_per_cycle = :budget WHERE id = :id")
    abstract suspend fun updateBudget(id: Int, budget: Long?)

    // ----------------------------- Delete -------------------------------------------
    @Delete
    abstract suspend fun delete(category: Category)
}