package com.example.pillarpocket.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: BudgetGoal)

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getGoalByMonthYear(userId: Int, month: Int, year: Int): Flow<BudgetGoal?>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getAllGoals(userId: Int): Flow<List<BudgetGoal>>
}