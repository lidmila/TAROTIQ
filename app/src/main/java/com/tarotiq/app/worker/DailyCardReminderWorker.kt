package com.tarotiq.app.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tarotiq.app.MainActivity
import com.tarotiq.app.R
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.utils.AstroUtils
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyCardReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settingsManager = SettingsManager(context)
        val enabled = settingsManager.notificationEnabledFlow.first()
        if (!enabled) return Result.success()

        val (title, text) = pickNotificationContent(settingsManager)

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "daily")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    private suspend fun pickNotificationContent(settingsManager: SettingsManager): Pair<String, String> {
        val lastActive = settingsManager.lastActiveDateFlow.first()
        val currentStreak = settingsManager.currentStreakFlow.first()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dayBeforeYesterday = LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val moonPhase = AstroUtils.getCurrentMoonPhase()

        // Priority 1: Streak at risk (last active was day before yesterday)
        if (lastActive == dayBeforeYesterday && currentStreak > 1) {
            return context.getString(R.string.notif_streak_risk_title) to
                    context.getString(R.string.notif_streak_risk_text, currentStreak)
        }

        // Priority 2: Comeback (inactive 3+ days)
        if (lastActive != null && lastActive != today && lastActive != yesterday && lastActive != dayBeforeYesterday) {
            return context.getString(R.string.notif_comeback_title) to
                    context.getString(R.string.notif_comeback_text)
        }

        // Priority 3: Streak continuation (active yesterday, good streak going)
        if (lastActive == yesterday && currentStreak >= 2) {
            // Check if close to milestone
            val daysToWeek = 7 - currentStreak
            val daysToMonth = 30 - currentStreak
            if (daysToWeek in 1..2 && currentStreak < 7) {
                return context.getString(R.string.notif_milestone_title) to
                        context.getString(R.string.notif_milestone_week_text, daysToWeek)
            }
            if (daysToMonth in 1..3 && currentStreak < 30) {
                return context.getString(R.string.notif_milestone_title) to
                        context.getString(R.string.notif_milestone_month_text, daysToMonth)
            }
            return context.getString(R.string.notif_streak_title, currentStreak) to
                    context.getString(R.string.notif_streak_text)
        }

        // Priority 4: Moon phase events
        if (moonPhase == AstroUtils.MoonPhase.FULL_MOON) {
            return context.getString(R.string.notif_full_moon_title) to
                    context.getString(R.string.notif_full_moon_text)
        }
        if (moonPhase == AstroUtils.MoonPhase.NEW_MOON) {
            return context.getString(R.string.notif_new_moon_title) to
                    context.getString(R.string.notif_new_moon_text)
        }

        // Priority 5: General variety (rotate based on day of year)
        val dayOfYear = LocalDate.now().dayOfYear
        return when (dayOfYear % 3) {
            0 -> context.getString(R.string.notif_general_1_title) to
                    context.getString(R.string.notif_general_1_text)
            1 -> context.getString(R.string.notif_general_2_title) to
                    context.getString(R.string.notif_general_2_text)
            else -> context.getString(R.string.notif_general_3_title) to
                    context.getString(R.string.notif_general_3_text)
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_card_reminder"
        const val NOTIFICATION_ID = 1001
    }
}
