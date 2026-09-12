package com.example.gentlenudge.backup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NudgeBackupScheduler {

    private const val TAG = "NudgeBackupScheduler"

    const val PREFS_NAME = "gentle_nudge_prefs"
    const val KEY_LAST_BACKUP_TIMESTAMP = "last_successful_backup_timestamp"
    const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val KEY_AUTO_BACKUP_DAY = "auto_backup_day_of_week"
    const val KEY_AUTO_BACKUP_HOUR = "auto_backup_hour"
    const val KEY_AUTO_BACKUP_MINUTE = "auto_backup_minute"

    const val ACTION_TRIGGER_AUTO_BACKUP = "com.example.gentlenudge.ACTION_TRIGGER_AUTO_BACKUP"

    private const val REQUEST_CODE_AUTO_BACKUP = 8001

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
