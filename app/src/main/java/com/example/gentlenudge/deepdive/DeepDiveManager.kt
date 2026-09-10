package com.example.gentlenudge.deepdive

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gentlenudge.MainActivity
import com.example.gentlenudge.notification.NudgeNotificationHelper
import com.example.gentlenudge.notification.NudgeNotificationReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Manages the state, persistence, alarms, and notifications for the reusable Deep Dive feature.
 */
object DeepDiveManager {

    private const val TAG = "DeepDiveManager"
    private const val PREFS_NAME = "deep_dive_prefs"
    private const val KEY_END_TIME_MILLIS = "deep_dive_end_time_millis"
    private const val KEY_STYLE = "deep_dive_notification_style"
    private const val DEFAULT_STYLE = "One Shot" // "One Shot" or "Full Ringtone"

    const val DEEP_DIVE_ALARM_REQUEST_CODE = 998877
    const val DEEP_DIVE_NOTIFICATION_ID = 998877

    const val ACTION_FIRE_DEEP_DIVE = "com.example.gentlenudge.ACTION_FIRE_DEEP_DIVE"
    const val ACTION_DISMISS_DEEP_DIVE = "com.example.gentlenudge.ACTION_DISMISS_DEEP_DIVE"

    data class DeepDiveState(
        val isActive: Boolean = false,
        val endTimeMillis: Long = 0L,
        val notificationStyle: String = DEFAULT_STYLE
    )

    private val _state = MutableStateFlow(DeepDiveState())
    val state: StateFlow<DeepDiveState> = _state.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Initialize/sync state from SharedPreferences (called on app startup or UI mount).
     */
    fun init(context: Context) {
        val prefs = getPrefs(context)
        val endTime = prefs.getLong(KEY_END_TIME_MILLIS, 0L)
        val style = prefs.getString(KEY_STYLE, DEFAULT_STYLE) ?: DEFAULT_STYLE
        val now = System.currentTimeMillis()

        if (endTime > now) {
            _state.value = DeepDiveState(
                isActive = true,
                endTimeMillis = endTime,
                notificationStyle = style
            )
        } else {
            if (endTime != 0L) {
                // Was active in past, reset
                prefs.edit().remove(KEY_END_TIME_MILLIS).apply()
            }
            _state.value = DeepDiveState(
                isActive = false,
                endTimeMillis = 0L,
                notificationStyle = style
            )
        }
    }

    fun getSavedNotificationStyle(context: Context): String {
        return getPrefs(context).getString(KEY_STYLE, DEFAULT_STYLE) ?: DEFAULT_STYLE
    }

    /**
     * Start a new Deep Dive session ending at targetEndTimeMillis.
     */
    fun startSession(context: Context, targetEndTimeMillis: Long, notificationStyle: String) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putLong(KEY_END_TIME_MILLIS, targetEndTimeMillis)
            .putString(KEY_STYLE, notificationStyle)
            .apply()

        _state.value = DeepDiveState(
            isActive = true,
            endTimeMillis = targetEndTimeMillis,
            notificationStyle = notificationStyle
        )

        scheduleAlarm(context, targetEndTimeMillis, notificationStyle)
    }

    /**
     * End session manually (or cancel).
     */
    fun endSession(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().remove(KEY_END_TIME_MILLIS).apply()

        cancelAlarm(context)
        dismissNotification(context)

        _state.value = _state.value.copy(
            isActive = false,
            endTimeMillis = 0L
        )
    }

    private fun scheduleAlarm(context: Context, triggerAtMillis: Long, style: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NudgeNotificationReceiver::class.java).apply {
            action = ACTION_FIRE_DEEP_DIVE
            putExtra(KEY_STYLE, style)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEEP_DIVE_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled Deep Dive alarm for $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "Permission denied for exact alarm, falling back: ${e.message}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule Deep Dive alarm: ${e.message}", e)
        }
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NudgeNotificationReceiver::class.java).apply {
            action = ACTION_FIRE_DEEP_DIVE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEEP_DIVE_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Reschedule alarm on phone reboot if session is still in the future.
     */
    fun rescheduleOnBoot(context: Context) {
        val prefs = getPrefs(context)
        val endTime = prefs.getLong(KEY_END_TIME_MILLIS, 0L)
        val style = prefs.getString(KEY_STYLE, DEFAULT_STYLE) ?: DEFAULT_STYLE
        val now = System.currentTimeMillis()

        if (endTime > now) {
            scheduleAlarm(context, endTime, style)
            Log.d(TAG, "Rescheduled active Deep Dive alarm on boot for $endTime")
        } else if (endTime != 0L) {
            prefs.edit().remove(KEY_END_TIME_MILLIS).apply()
        }
    }

    /**
     * Triggered when the Deep Dive alarm fires.
     */
    fun onAlarmFired(context: Context, style: String) {
        // Reset active state in prefs & state flow so card returns to reusable
        val prefs = getPrefs(context)
        prefs.edit().remove(KEY_END_TIME_MILLIS).apply()
        _state.value = _state.value.copy(isActive = false, endTimeMillis = 0L)

        // Show the notification according to selected style
        showNotification(context, style)
    }

    private fun showNotification(context: Context, style: String) {
        NudgeNotificationHelper.createNotificationChannels(context)

        val isFullRingtone = style.equals("Full Ringtone", ignoreCase = true)
        val channelId = if (isFullRingtone) {
            NudgeNotificationHelper.FULL_RINGTONE_CHANNEL_ID
        } else {
            NudgeNotificationHelper.GENTLE_CHANNEL_ID
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            DEEP_DIVE_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, NudgeNotificationReceiver::class.java).apply {
            action = ACTION_DISMISS_DEEP_DIVE
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            DEEP_DIVE_NOTIFICATION_ID + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Deep Dive Complete")
            .setContentText("Your session has finished.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Deep Dive complete · Take a gentle breath and return to your rhythm."
                )
            )
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)

        if (isFullRingtone) {
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVibrate(longArrayOf(0, 700, 300, 700, 300, 700))
        } else {
            // One short sound / beep then stop
            val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setSound(notificationSound)
                .setVibrate(longArrayOf(0, 250))
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(DEEP_DIVE_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post Deep Dive notification: ${e.message}", e)
        }
    }

    fun dismissNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(DEEP_DIVE_NOTIFICATION_ID)
    }

    fun formatTimeRemaining(endTimeMillis: Long): String {
        val remainingMillis = endTimeMillis - System.currentTimeMillis()
        if (remainingMillis <= 0) return "Finishing now"
        val minutes = (remainingMillis / (1000 * 60)).toInt()
        val hours = minutes / 60
        val remMinutes = minutes % 60

        return when {
            hours > 0 && remMinutes > 0 -> "$hours hr $remMinutes min left"
            hours > 0 -> "$hours hr left"
            remMinutes > 0 -> "$remMinutes min left"
            else -> "< 1 min left"
        }
    }

    fun formatClockTime(timeMillis: Long): String {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(Date(timeMillis))
    }
}
