package com.example.pillarpocket.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.data.preferences.UserPreferences
import com.example.pillarpocket.notifications.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.Calendar

/*
 * A background WorkManager worker that runs daily to check the
 * user's spending status and send notifications as needed.
 *
 * Checks performed:
 * 1. Budget status — whether spending is approaching or has
 *    exceeded the maximum monthly goal.
 * 2. Consistency — whether the user hasn't logged any expenses
 *    for 3 or more days.
 *
 * Uses [UserPreferences] to retrieve the currently logged-in
 * user's ID so it can query the right data.
 */
class SpendingCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app    = applicationContext as PillarPocketApp
        val userId = UserPreferences(applicationContext).getUserId()

        // No user logged in — nothing to check
        if (userId == -1) return Result.success()

        checkBudgetStatus(app, userId)
        checkConsistency(app, userId)

        return Result.success()
    }

    // Budget Check

    /*
     * Compares current month's spending to the saved budget goal
     * and fires the appropriate notification.
     */
    private suspend fun checkBudgetStatus(app: PillarPocketApp, userId: Int) {
        val calendar = Calendar.getInstance()
        val month    = calendar.get(Calendar.MONTH) + 1
        val year     = calendar.get(Calendar.YEAR)

        val goal = app.budgetGoalRepository
            .getGoalByMonthYear(userId, month, year)
            .first()

        // No goal set — nothing to compare against
        if (goal == null) return

        val yearMonth = "%04d-%02d".format(year, month)
        val total     = app.expenseRepository
            .getMonthlyTotal(userId, yearMonth)
            .first() ?: 0.0

        val percentage = (total / goal.maximumGoal * 100).toInt()

        when {
            total > goal.maximumGoal -> {
                val overspent = total - goal.maximumGoal
                NotificationHelper.showExceededBudget(applicationContext, overspent)
            }
            percentage >= 80 -> {
                val remaining = goal.maximumGoal - total
                NotificationHelper.showApproachingBudget(
                    applicationContext, percentage, remaining
                )
            }
            else -> {
                // Spending is healthy — clear any previous budget alerts
                NotificationHelper.cancelBudgetNotifications(applicationContext)
            }
        }
    }

    // Consistency Check

    /*
     * Checks when the last expense was logged and sends a reminder
     * if no expenses have been recorded for 3 or more days.
     */
    private suspend fun checkConsistency(app: PillarPocketApp, userId: Int) {
        val expenses = app.expenseRepository
            .getExpensesByUser(userId)
            .first()

        if (expenses.isEmpty()) return

        val mostRecent = expenses.maxByOrNull { it.createdAt } ?: return

        val millisInDay = 1000L * 60 * 60 * 24
        val daysSince   = ((System.currentTimeMillis() - mostRecent.createdAt) /
                millisInDay).toInt()

        if (daysSince >= 3) {
            NotificationHelper.showConsistencyReminder(applicationContext, daysSince)
        }
    }
}