package com.example.pillarpocket.data.repository

import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.data.local.CategoryDao
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getCategoriesByUser(userId: Int): Flow<List<Category>> =
        categoryDao.getCategoriesByUser(userId)

    suspend fun addCategory(
        userId: Int,
        name: String,
        colorHex: String,
        iconName: String
    ): Result<Unit> {
        return try {
            if (name.isBlank()) return Result.failure(Exception("Category name cannot be empty"))
            val exists = categoryDao.categoryNameExists(userId, name.trim())
            if (exists > 0) return Result.failure(Exception("Category \"${name.trim()}\" already exists"))
            categoryDao.insertCategory(
                Category(userId = userId, name = name.trim(), colorHex = colorHex, iconName = iconName)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(
        category: Category,
        newName: String,
        newColorHex: String,
        newIconName: String
    ): Result<Unit> {
        return try {
            if (newName.isBlank()) return Result.failure(Exception("Category name cannot be empty"))
            val exists = categoryDao.categoryNameExists(category.userId, newName.trim())
            if (exists > 0 && newName.trim() != category.name)
                return Result.failure(Exception("Category \"${newName.trim()}\" already exists"))
            categoryDao.updateCategory(
                category.copy(name = newName.trim(), colorHex = newColorHex, iconName = newIconName)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            categoryDao.deleteCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}