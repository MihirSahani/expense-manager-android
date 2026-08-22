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

/**
 * Data Access Object for the `categories` table.
 */
@Dao
abstract class CategoryDAO {
    // ----------------------------- Fetching Categories -----------------------------
    /**
     * Fetches a single category by its primary key.
     *
     * @param id the category id.
     * @return the matching [Category], or `null` if none exists.
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    abstract suspend fun getCategory(id: Int): Category?

    /**
     * Observes a single category by its primary key.
     *
     * @param id the category id.
     * @return a [Flow] emitting the matching [Category] (or `null`) whenever it changes.
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    abstract fun getCategoryFlow(id: Int): Flow<Category?>

    /**
     * Observes every category in the table.
     *
     * @return a [Flow] emitting the full category list whenever it changes.
     */
    @Query("SELECT * FROM categories")
    abstract fun getAllCategoriesFlow(): Flow<List<Category>>

    /**
     * Fetches every category in the table as a paged source, for use with Paging 3.
     *
     * @return a [PagingSource] over all categories.
     */
    @Query("SELECT * FROM categories")
    abstract fun getAllCategories(): PagingSource<Int, Category>

    /**
     * Observes every category together with its remaining budget and amount spent within a
     * cycle window.
     *
     * @param start inclusive start of the cycle window, in epoch seconds. Defaults to the
     * beginning of time.
     * @param end inclusive end of the cycle window, in epoch seconds. Defaults to the end of
     * time.
     * @return a [Flow] emitting the [CategoryWithInfo] list whenever the underlying data changes.
     */
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
    /**
     * Inserts a new category.
     *
     * @param category the category to insert.
     */
    @Insert
    abstract suspend fun create(category: Category)

    // ---------------------------- Update --------------------------------------------
    /**
     * Updates an existing category.
     *
     * @param category the category with updated field values.
     */
    @Update
    abstract suspend fun update(category: Category)

    /**
     * Updates only the per-cycle budget of a category.
     *
     * @param id the category id.
     * @param budget the new budget per cycle, or `null` to clear it.
     */
    @Query("UPDATE categories SET budget_per_cycle = :budget WHERE id = :id")
    abstract suspend fun updateBudget(id: Int, budget: Long?)

    // ----------------------------- Delete -------------------------------------------
    /**
     * Deletes a category. Related transaction and preference rows react according to their
     * foreign key rules (`SET_NULL`/`CASCADE`).
     *
     * @param category the category to delete.
     */
    @Delete
    abstract suspend fun delete(category: Category)
}