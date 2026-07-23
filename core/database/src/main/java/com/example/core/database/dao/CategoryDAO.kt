package com.example.core.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Category

@Dao
abstract class CategoryDAO {
    // ----------------------------- Fetching Categories -----------------------------
    @Query("SELECT * FROM categories WHERE id = :id")
    abstract suspend fun getCategory(id: Int): Category?

    // TODO: Add pagination for categories

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

    // ----------------------------- Delete -------------------------------------------
    @Delete
    abstract suspend fun delete(category: Category)
}