package com.example.pillarpocket.data.repository

import com.example.pillarpocket.data.local.Expense
import com.example.pillarpocket.data.local.ExpenseDao
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getExpensesByUser(userId: Int): Flow<List<Expense>> =
        expenseDao.getExpensesByUser(userId)

    fun getTotalExpenses(userId: Int): Flow<Double?> =
        expenseDao.getTotalExpenses(userId)

    fun getMonthlyTotal(userId: Int, yearMonth: String): Flow<Double?> =
        expenseDao.getMonthlyTotal(userId, yearMonth)

    suspend fun addExpense(
        userId: Int,
        categoryId: Int,
        amount: Double,
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        photoUri: String? = null
    ): Result<Unit> {
        return try {
            when {
                amount <= 0 -> return Result.failure(Exception("Amount must be greater than zero"))
                date.isBlank() -> return Result.failure(Exception("Please select a date"))
                startTime.isBlank() -> return Result.failure(Exception("Please select a start time"))
                endTime.isBlank() -> return Result.failure(Exception("Please select an end time"))
                description.isBlank() -> return Result.failure(Exception("Description cannot be empty"))
                categoryId == 0 -> return Result.failure(Exception("Please select a category"))
            }
            expenseDao.insertExpense(
                Expense(
                    userId = userId,
                    categoryId = categoryId,
                    amount = amount,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    description = description.trim(),
                    photoUri = photoUri
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}