package com.example.gentlenudge.backup

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gentlenudge.MainActivity
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NudgeBackupScheduler {

    private const val TAG = "NudgeBackupScheduler"

    const val PREFS_NAME = "gentle_nudge_prefs"
    const val KEY_LAST_BACKUP_TIMESTAMP = "last_successful_backup_timestamp"
    const val KEY_BACKUP_REMINDER_ENABLED = "backup_reminder_enabled"
    const val KEY_BACKUP_REMINDER_DAYS = "backup_reminder_interval_days"
    const val KEY_BACKUP_REMINDER_LAST_NOTIFIED = "backup_reminder_last_notified_timestamp"
    const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val KEY_AUTO_BACKUP_DAY = "auto_backup_day_of_week"
    const val KEY_AUTO_BACKUP_HOUR = "auto_backup_hour"
    const val KEY_AUTO_BACKUP_MINUTE = "auto_backup_minute"

    const val CHANNEL_ID = "nudge_backup_safety_channel"
    const val CHANNEL_NAME = "Backup & Data Safety"
    const val CHANNEL_DESC = "Gentle reminders to keep your notes and pursuits backed up safely"

    const val NOTIFICATION_ID_BACKUP_REMINDER = 9001

    const val ACTION_TRIGGER_AUTO_BACKUP = "com.example.gentlenudge.ACTION_TRIGGER_AUTO_BACKUP"
    const val ACTION_TRIGGER_BACKUP_REMINDER = "com.example.gentlenudge.ACTION_TRIGGER_BACKUP_REMINDER"
    const val ACTION_BACKUP_REMINDER_LATER = "com.example.gentlenudge.ACTION_BACKUP_REMINDER_LATER"

    const val EXTRA_TRIGGER_MANUAL_BACKUP = "extra_trigger_manual_backup"

    private const val REQUEST_CODE_AUTO_BACKUP = 8001
    private const val REQUEST_CODE_BACKUP_REMINDER = 8002
    private const val REQUEST_CODE_REMINDER_LATER = 8003
    private const val REQUEST_CODE_REMINDER_CREATE = 8004

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // ==========================================================
    // BACKUP REMINDER SCHEDULING
    // ==========================================================

    fun scheduleBackupReminder(context: Context) {
        val prefs = getPrefs(context)
        val enabled = prefs.getBoolean(KEY_BACKUP_REMINDER_ENABLED, false)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NudgeBackupReceiver::class.java).apply {
            action = ACTION_TRIGGER_BACKUP_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BACKUP_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            alarmManager.cancel(pendingIntent)
            dismissBackupReminderNotification(context)
            Log.d(TAG, "Backup reminder disabled, cancelled alarm.")
            return
        }

        val intervalDays = prefs.getInt(KEY_BACKUP_REMINDER_DAYS, 30)
        val intervalMillis = intervalDays * 24L * 60L * 60L * 1000L
        val lastBackup = prefs.getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L)
        val lastNotified = prefs.getLong(KEY_BACKUP_REMINDER_LAST_NOTIFIED, 0L)
        val now = System.currentTimeMillis()

        // Base calculation on the most recent backup timestamp, or when reminder was configured
        val baseTimestamp = if (lastBackup > 0L) lastBackup else lastNotified.takeIf { it > 0L } ?: now
        var targetMillis = baseTimestamp + intervalMillis

        if (targetMillis <= now) {
            // If already overdue, schedule gentle reminder for tomorrow morning or in 5 minutes
            targetMillis = now + (5 * 60 * 1000L)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled backup reminder for $targetMillis ($intervalDays days interval)")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling backup reminder: ${e.message}", e)
        }
    }

    fun showBackupReminderNotification(context: Context) {
        createNotificationChannel(context)

        // Action: Create Backup (Opens App and launches backup picker)
        val createIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TRIGGER_MANUAL_BACKUP, true)
        }
        val createPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_REMINDER_CREATE,
            createIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Later (Snoozes the reminder by 2 days without updating last backup timestamp)
        val laterIntent = Intent(context, NudgeBackupReceiver::class.java).apply {
            action = ACTION_BACKUP_REMINDER_LATER
        }
        val laterPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER_LATER,
            laterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("Your Nudge data hasn't been backed up recently.")
            .setContentText("Create a backup to keep your data safe.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Your Nudge data hasn't been backed up recently.\nCreate a backup to keep your notes, pursuits, and attachments safe."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent)
            .addAction(android.R.drawable.ic_menu_save, "Create Backup", createPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Later", laterPendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID_BACKUP_REMINDER, builder.build())
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted for backup reminder: ${e.message}")
        }
    }

    fun dismissBackupReminderNotification(context: Context) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(NOTIFICATION_ID_BACKUP_REMINDER)
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing backup reminder notification: ${e.message}", e)
        }
    }

    fun handleReminderLater(context: Context) {
        dismissBackupReminderNotification(context)
        // Record snooze timestamp and reschedule check for 2 days later without modifying last backup
        val prefs = getPrefs(context)
        val snoozeMillis = System.currentTimeMillis() + (2 * 24L * 60L * 60L * 1000L)
        prefs.edit().putLong(KEY_BACKUP_REMINDER_LAST_NOTIFIED, System.currentTimeMillis()).apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NudgeBackupReceiver::class.java).apply {
            action = ACTION_TRIGGER_BACKUP_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BACKUP_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeMillis, pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule snoozed reminder: ${e.message}", e)
        }
    }

    // ==========================================================
    // AUTOMATIC BACKUP SCHEDULING
    // ==========================================================

    fun calculateNextAutoBackupTime(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val now = System.currentTimeMillis()

        if (dayOfWeek == 0) { // Every day
            if (cal.timeInMillis <= now) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            // Specific day of week (Calendar.SUNDAY = 1, etc.)
            val currentDow = cal.get(Calendar.DAY_OF_WEEK)
            var daysDiff = (dayOfWeek - currentDow + 7) % 7
            if (daysDiff == 0 && cal.timeInMillis <= now) {
                daysDiff = 7
            }
            cal.add(Calendar.DAY_OF_YEAR, daysDiff)
        }

        return cal.timeInMillis
    }

    fun scheduleAutoBackup(context: Context) {
        val prefs = getPrefs(context)
        val enabled = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NudgeBackupReceiver::class.java).apply {
            action = ACTION_TRIGGER_AUTO_BACKUP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_AUTO_BACKUP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Auto backup disabled, cancelled alarm.")
            return
        }

        val dayOfWeek = prefs.getInt(KEY_AUTO_BACKUP_DAY, Calendar.SUNDAY)
        val hour = prefs.getInt(KEY_AUTO_BACKUP_HOUR, 23)
        val minute = prefs.getInt(KEY_AUTO_BACKUP_MINUTE, 0)

        val targetMillis = calculateNextAutoBackupTime(dayOfWeek, hour, minute)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled auto backup for $targetMillis (Day: $dayOfWeek, Time: $hour:$minute)")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling auto backup: ${e.message}", e)
        }
    }

    fun rescheduleAll(context: Context) {
        scheduleBackupReminder(context)
        scheduleAutoBackup(context)
    }

    // ==========================================================
    // FORMATTING HELPERS
    // ==========================================================

    fun formatLastBackupRelative(timestamp: Long): String {
        if (timestamp <= 0L) return "Never"
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        if (diff < 0L) return "Today"

        val days = diff / (24L * 60L * 60L * 1000L)
        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            else -> "$days days ago"
        }
    }

    fun formatNextAutoBackup(dayOfWeek: Int, hour: Int, minute: Int): String {
        val nextMillis = calculateNextAutoBackupTime(dayOfWeek, hour, minute)
        val targetCal = Calendar.getInstance().apply { timeInMillis = nextMillis }
        val nowCal = Calendar.getInstance()

        val timeString = formatTime(hour, minute)

        val isToday = targetCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                targetCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = targetCal.get(Calendar.YEAR) == tomorrowCal.get(Calendar.YEAR) &&
                targetCal.get(Calendar.DAY_OF_YEAR) == tomorrowCal.get(Calendar.DAY_OF_YEAR)

        return when {
            isToday -> "Today, $timeString"
            isTomorrow -> "Tomorrow, $timeString"
            else -> {
                val dayName = when (targetCal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> "Sunday"
                    Calendar.MONDAY -> "Monday"
                    Calendar.TUESDAY -> "Tuesday"
                    Calendar.WEDNESDAY -> "Wednesday"
                    Calendar.THURSDAY -> "Thursday"
                    Calendar.FRIDAY -> "Friday"
                    Calendar.SATURDAY -> "Saturday"
                    else -> "Sunday"
                }
                "$dayName, $timeString"
            }
        }
    }

    fun formatScheduleSummary(dayOfWeek: Int, hour: Int, minute: Int): String {
        val timeString = formatTime(hour, minute)
        val dayName = when (dayOfWeek) {
            0 -> "Every day"
            Calendar.SUNDAY -> "Every Sunday"
            Calendar.MONDAY -> "Every Monday"
            Calendar.TUESDAY -> "Every Tuesday"
            Calendar.WEDNESDAY -> "Every Wednesday"
            Calendar.THURSDAY -> "Every Thursday"
            Calendar.FRIDAY -> "Every Friday"
            Calendar.SATURDAY -> "Every Saturday"
            else -> "Every Sunday"
        }
        return "$dayName • $timeString"
    }

    fun formatTime(hour: Int, minute: Int): String {
        val isPm = hour >= 12
        val h12 = when (val h = hour % 12) {
            0 -> 12
            else -> h
        }
        val minStr = if (minute < 10) "0$minute" else "$minute"
        val amPm = if (isPm) "PM" else "AM"
        return "$h12:$minStr $amPm"
    }
}
