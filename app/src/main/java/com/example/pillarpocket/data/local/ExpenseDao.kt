package com.example.pillarpocket.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getExpensesByUser(userId: Int): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    fun getTotalExpenses(userId: Int): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date LIKE :yearMonth || '%'")
    fun getMonthlyTotal(userId: Int, yearMonth: String): Flow<Double?>
}