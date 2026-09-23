package com.example.pillarpocket.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_goals",
    indices = [Index(value = ["userId", "month", "year"], unique = true)]
)
data class BudgetGoal (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val month: Int,
    val year: Int,
    val minimumGoal: Double,
    val maximumGoal: Double
)
