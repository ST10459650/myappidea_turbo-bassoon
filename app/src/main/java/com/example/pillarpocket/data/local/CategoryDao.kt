package com.example.pillarpocket.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/*
 - Data Access Object (DAO) for category-related database operations.
 - Provides CRUD (Create, Read, Update, Delete) operations for the
  "categories" table. All queries are scoped to a specific user to
  ensure data isolation between accounts.
 - Queries that return lists use [Flow] so the UI automatically
  reacts to any changes in the database.
 */
@Dao
interface CategoryDao {

    /*
     - Inserts a new category or replaces an existing one with the same
       primary key. Used for both creating and updating categories.
     - @param category The [Category] entity to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    /*
     * Updates an existing category record in the database.
     * Matches the record by the category's primary key.
     * @param category The [Category] entity with updated values.
     */
    @Update
    suspend fun updateCategory(category: Category)

    /*
     * Deletes a specific category from the database.
     * Matches the record by the category's primary key.
     * @param category The [Category] entity to delete.
     */
    @Delete
    suspend fun deleteCategory(category: Category)

    /*
     * Retrieves all categories belonging to a specific user,
     * ordered alphabetically by name.

     * Returns a [Flow] so the UI automatically updates whenever
     * a category is added, edited, or deleted.

     * @param userId The ID of the user whose categories to retrieve.
     * @return A [Flow] emitting the updated list of [Category] objects.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesByUser(userId: Int): Flow<List<Category>>

    /*
     * Retrieves a single category by its unique ID.
     * Returns null if no matching category is found.

     * @param categoryId The ID of the category to retrieve.
     * @return The matching [Category], or null if not found.
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Int): Category?

    /*
     * Checks whether a category with the given name already exists
     * for a specific user.

     * Used during creation and editing to enforce unique category
     * names per user. Returns a count — 0 means the name is available.

     * @param userId The ID of the user to check against.
     * @param name The category name to look up.
     * @return The number of matching categories (0 or 1).
     */
    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId AND name = :name")
    suspend fun categoryNameExists(userId: Int, name: String): Int
}