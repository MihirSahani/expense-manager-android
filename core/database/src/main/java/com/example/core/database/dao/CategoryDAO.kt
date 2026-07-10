package com.example.core.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.core.database.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDAO {
    // ----------------------------- Fetching Categories -----------------------------
    @Query("SELECT * FROM categories WHERE id = :id")
    abstract suspend fun getCategory(id: Int): Category?

    // TODO: Add pagination for categories

    @Query("SELECT * FROM categories WHERE ")
    abstract suspend fun categoriesAtRisk(): Flow<List<Category>>

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