package com.example.pillarpocket

import android.app.Application
import androidx.work.*
import com.example.pillarpocket.data.local.PillarPocketDatabase
import com.example.pillarpocket.data.repository.*
import com.example.pillarpocket.notifications.NotificationHelper
import com.example.pillarpocket.workers.SpendingCheckWorker
import java.util.concurrent.TimeUnit

class PillarPocketApp : Application() {

    val database by lazy { PillarPocketDatabase.getDatabase(this) }
    val userRepository by lazy { UserRepository(database.userDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val budgetGoalRepository by lazy { BudgetGoalRepository(database.budgetGoalDao()) }
    val badgeRepository by lazy { BadgeRepository(database.earnedBadgeDao()) }

    override fun onCreate() {
        super.onCreate()

        // Create notification channels (required for Android 8.0+)
        NotificationHelper.createChannels(this)

        // Schedule the daily spending check worker
        scheduleSpendingCheck()
    }

    /*
     * Schedules a periodic WorkManager task that runs once every 24 hours
     * to check spending and send notifications if needed.
     *
     * Uses [ExistingPeriodicWorkPolicy.KEEP] to avoid rescheduling
     * if the worker is already queued.
     */
    private fun scheduleSpendingCheck() {
        val request = PeriodicWorkRequestBuilder<SpendingCheckWorker>(
            repeatInterval         = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "pillar_spending_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}