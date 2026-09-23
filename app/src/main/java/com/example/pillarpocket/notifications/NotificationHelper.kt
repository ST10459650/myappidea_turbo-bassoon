package com.example.pillarpocket.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pillarpocket.MainActivity
import com.example.pillarpocket.R

/**
 * Centralised helper for all Pillar Pocket notifications.
 *
 * Handles channel creation (required for Android 8.0+) and provides
 * methods to show each notification type:
 * - Budget approaching (at 80% or more of max goal)
 * - Budget exceeded (over max goal)
 * - Consistency reminder (no expenses logged for 3+ days)
 */
object NotificationHelper {

    // Channel IDs
    const val CHANNEL_BUDGET      = "pillar_budget_alerts"
    const val CHANNEL_CONSISTENCY = "pillar_consistency_reminders"

    // Notification IDs — using fixed IDs ensures that a new
    // notification of the same type replaces the previous one
    private const val NOTIF_APPROACHING  = 1001
    private const val NOTIF_EXCEEDED     = 1002
    private const val NOTIF_CONSISTENCY  = 1003

    /**
     * Creates the notification channels required on Android 8.0+.
     * Safe to call multiple times — channels are only created once.
     * Should be called in [Application.onCreate].
     */
    fun createChannels(context: Context) {
        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BUDGET,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you are approaching or have exceeded your budget goal."
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_CONSISTENCY,
                "Consistency Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to keep logging expenses regularly."
            }
        )
    }

    /**
     * Shows a warning notification when spending reaches 80%+ of
     * the maximum budget goal for the current month.
     *
     * @param percentage How much of the max goal has been spent (0–100).
     * @param remaining  The remaining amount before exceeding the max goal.
     */
    fun showApproachingBudget(
        context: Context,
        percentage: Int,
        remaining: Double
    ) {
        show(
            context      = context,
            channelId    = CHANNEL_BUDGET,
            notifId      = NOTIF_APPROACHING,
            title        = "⚠️ Approaching Budget Limit",
            message      = "You've used $percentage% of your monthly budget. " +
                    "Only R${"%.2f".format(remaining)} remaining.",
            priority     = NotificationCompat.PRIORITY_HIGH
        )
    }

    /**
     * Shows an urgent notification when the user has exceeded their
     * maximum monthly budget goal.
     *
     * @param overspent The amount by which the budget has been exceeded.
     */
    fun showExceededBudget(context: Context, overspent: Double) {
        show(
            context   = context,
            channelId = CHANNEL_BUDGET,
            notifId   = NOTIF_EXCEEDED,
            title     = "🚨 Budget Exceeded!",
            message   = "You've gone over your monthly budget by " +
                    "R${"%.2f".format(overspent)}. Consider reviewing your expenses.",
            priority  = NotificationCompat.PRIORITY_HIGH
        )
    }

    /*
     * Shows a gentle reminder when the user hasn't logged any
     * expenses for 3 or more days.
     *
     * @param daysSince Number of days since the last expense was logged.
     */
    fun showConsistencyReminder(context: Context, daysSince: Int) {
        show(
            context   = context,
            channelId = CHANNEL_CONSISTENCY,
            notifId   = NOTIF_CONSISTENCY,
            title     = "📝 Don't forget to log your expenses!",
            message   = "You haven't logged any expenses in $daysSince days. " +
                    "Stay on top of your budget!",
            priority  = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    /*
     * Cancels both budget notifications — called when the user's
     * spending returns to a healthy range.
     */
    fun cancelBudgetNotifications(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            cancel(NOTIF_APPROACHING)
            cancel(NOTIF_EXCEEDED)
        }
    }

    // Private helper

    private fun show(
        context: Context,
        channelId: String,
        notifId: Int,
        title: String,
        message: String,
        priority: Int
    ) {
        // Tapping the notification opens the app
        val pendingIntent = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            // Permission not granted — notification is silently skipped
        }
    }
}