package com.example.pillarpocket.data.repository

import com.example.pillarpocket.data.local.BudgetGoal
import com.example.pillarpocket.data.local.BudgetGoalDao
import kotlinx.coroutines.flow.Flow

class BudgetGoalRepository (private val budgetGoalDao: BudgetGoalDao){

    fun getGoalByMonthYear(userId: Int, month: Int, year: Int): Flow<BudgetGoal?> =
        budgetGoalDao.getGoalByMonthYear(userId, month, year)

    fun getAllGoals(userId: Int): Flow<List<BudgetGoal>> =
        budgetGoalDao.getAllGoals(userId)

    suspend fun setGoal(
        userId: Int,
        month: Int,
        year: Int,
        minimumGoal: Double,
        maximumGoal: Double
    ): Result<Unit> {
        return try {
            when {
                minimumGoal <= 0 -> return Result.failure(Exception("Minimum goal must be greater than zero"))
                maximumGoal <= 0 -> return Result.failure(Exception("Maximum goal must be greater than zero"))
                minimumGoal >= maximumGoal -> return Result.failure(Exception("Minimum goal must be less than maximum goal"))
            }
            budgetGoalDao.insertOrUpdateGoal(
                BudgetGoal(
                    userId = userId,
                    month = month,
                    year = year,
                    minimumGoal = minimumGoal,
                    maximumGoal = maximumGoal
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}