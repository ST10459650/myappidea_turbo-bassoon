package com.example.pillarpocket.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int,
    val amount: Double,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)