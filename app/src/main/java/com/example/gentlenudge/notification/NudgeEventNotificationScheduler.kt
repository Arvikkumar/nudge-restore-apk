package com.example.gentlenudge.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.gentlenudge.data.events.NudgeEventsRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class EventNotificationMode(val displayName: String, val defaultDays: Int) {
    OFF("Off", 0),
    ONE_DAY_BEFORE("1 day before", 1),
    TWO_DAYS_BEFORE("2 days before", 2),
    CUSTOM("Custom", 3);

    companion object {
        fun fromString(value: String?): EventNotificationMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OFF
        }
    }
}

object NudgeEventNotificationScheduler {

    private const val TAG = "EventNotificationSched"
    private const val PREFS_NAME = "gentle_nudge_prefs"
    const val KEY_EVENT_NOTIF_MODE = "event_notification_mode"
    const val KEY_EVENT_NOTIF_DAYS = "event_notification_days"
    const val KEY_DELIVERED_EVENT_REMINDERS = "delivered_event_reminders"

    // Base request code offset to avoid any collisions with task alarms (tasks use IDs e.g. 1..100000)
    private const val EVENT_ALARM_REQUEST_CODE_BASE = 500000

    val deliveryLock = Any()

    fun getEventReminderKey(eventId: String, year: Int, month: Int, day: Int): String {
        return "$eventId-$year-$month-$day"
    }

    fun isEventReminderDelivered(context: Context, reminderKey: String): Boolean {
        synchronized(deliveryLock) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val delivered = prefs.getStringSet(KEY_DELIVERED_EVENT_REMINDERS, emptySet()) ?: emptySet()
            return delivered.contains(reminderKey)
        }
    }

    fun markEventReminderDelivered(context: Context, reminderKey: String) {
        synchronized(deliveryLock) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val delivered = HashSet(prefs.getStringSet(KEY_DELIVERED_EVENT_REMINDERS, emptySet()) ?: emptySet())
            if (!delivered.contains(reminderKey)) {
                delivered.add(reminderKey)
                prefs.edit().putStringSet(KEY_DELIVERED_EVENT_REMINDERS, delivered).apply()
            }
        }
    }

    fun clearDeliveredEventReminders(context: Context) {
        synchronized(deliveryLock) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_DELIVERED_EVENT_REMINDERS).apply()
        }
    }

    fun deliverMissedReminder(
        context: Context,
        eventId: String,
        eventName: String,
        category: String,
        description: String,
        year: Int,
        month: Int,
        day: Int,
        daysBefore: Int
    ): Boolean {
        val reminderKey = getEventReminderKey(eventId, year, month, day)
        synchronized(deliveryLock) {
            if (isEventReminderDelivered(context, reminderKey)) {
                return false
            }

            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            val eventCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
            }
            val dateLabel = dateFormat.format(eventCal.time)

            val shown = NudgeNotificationHelper.showEventNotification(
                context = context,
                eventId = eventId,
                eventName = eventName,
                category = category,
                description = description,
                dateLabel = dateLabel,
                daysBefore = daysBefore,
                eventYear = year,
                eventMonth = month,
                eventDay = day
            )

            if (shown) {
                markEventReminderDelivered(context, reminderKey)
            }
            return shown
        }
    }

    fun getEventNotificationMode(context: Context): EventNotificationMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeStr = prefs.getString(KEY_EVENT_NOTIF_MODE, EventNotificationMode.OFF.name)
        return EventNotificationMode.fromString(modeStr)
    }

    fun getEventNotificationDays(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val mode = getEventNotificationMode(context)
        return when (mode) {
            EventNotificationMode.OFF -> 0
            EventNotificationMode.ONE_DAY_BEFORE -> 1
            EventNotificationMode.TWO_DAYS_BEFORE -> 2
            EventNotificationMode.CUSTOM -> {
                val custom = prefs.getInt(KEY_EVENT_NOTIF_DAYS, 3)
                if (custom < 1) 3 else custom
            }
        }
    }

    fun setEventNotificationSetting(context: Context, mode: EventNotificationMode, customDays: Int = 3) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val safeCustomDays = if (customDays < 1) 3 else customDays
        val finalDays = when (mode) {
            EventNotificationMode.OFF -> 0
            EventNotificationMode.ONE_DAY_BEFORE -> 1
            EventNotificationMode.TWO_DAYS_BEFORE -> 2
            EventNotificationMode.CUSTOM -> safeCustomDays
        }

        prefs.edit()
            .putString(KEY_EVENT_NOTIF_MODE, mode.name)
            .putInt(KEY_EVENT_NOTIF_DAYS, finalDays)
            .apply()

        // Always cancel previous alarms first to avoid duplicate or orphaned schedules
        cancelAllEventNotifications(context)

        if (mode != EventNotificationMode.OFF) {
            scheduleAllEventNotifications(context, finalDays)
        }
    }

    fun rescheduleIfEnabled(context: Context) {
        val mode = getEventNotificationMode(context)
        if (mode != EventNotificationMode.OFF) {
            val days = getEventNotificationDays(context)
            scheduleAllEventNotifications(context, days)
        } else {
            cancelAllEventNotifications(context)
        }
    }

    fun scheduleAllEventNotifications(
        context: Context,
        daysBefore: Int = getEventNotificationDays(context),
        lookaheadDays: Int = 90
    ) {
        if (daysBefore <= 0) {
            cancelAllEventNotifications(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nowMillis = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

        for (dayOffset in 0..lookaheadDays) {
            val eventDateCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            val year = eventDateCal.get(Calendar.YEAR)
            val month = eventDateCal.get(Calendar.MONTH) + 1
            val day = eventDateCal.get(Calendar.DAY_OF_MONTH)

            // Calculate the end of this event day (23:59:59.999) in local time
            val endOfEventDayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endOfEventDayMillis = endOfEventDayCal.timeInMillis

            val events = NudgeEventsRepository.getEventsForDate(year, month, day)
            for (event in events) {
                val reminderKey = getEventReminderKey(event.id, year, month, day)

                // If already delivered, do not show or schedule again (Duplicate Protection)
                if (isEventReminderDelivered(context, reminderKey)) {
                    continue
                }

                // Calculate reminder trigger time: (eventDate - daysBefore) at 9:00 AM local time
                val reminderCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, day)
                    add(Calendar.DAY_OF_YEAR, -daysBefore)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val triggerMillis = reminderCal.timeInMillis

                if (triggerMillis > nowMillis) {
                    // Future reminder -> schedule alarm with AlarmManager
                    val requestCode = getEventRequestCode(event.id, year, month, day)
                    val dateLabel = dateFormat.format(eventDateCal.time)

                    val intent = Intent(context, NudgeNotificationReceiver::class.java).apply {
                        action = NudgeNotificationHelper.ACTION_FIRE_EVENT_NOTIFICATION
                        putExtra(NudgeNotificationHelper.EXTRA_EVENT_ID, event.id)
                        putExtra(NudgeNotificationHelper.EXTRA_EVENT_NAME, event.name)
                        putExtra(NudgeNotificationHelper.EXTRA_EVENT_CATEGORY, event.category.displayName)
                        putExtra(NudgeNotificationHelper.EXTRA_EVENT_DESCRIPTION, event.description)
                        putExtra(NudgeNotificationHelper.EXTRA_EVENT_DATE_LABEL, dateLabel)
                        putExtra(NudgeNotificationHelper.EXTRA_DAYS_BEFORE, daysBefore)
                        putExtra(NudgeNotificationHelper.EXTRA_EVENT_YEAR, year)
                        putExtra(NudgeNotificationHelper.EXTRA_EVENT_MONTH, month)
                        putExtra(NudgeNotificationHelper.EXTRA_EVENT_DAY, day)
                        putExtra(NudgeNotificationHelper.EXTRA_REMINDER_KEY, reminderKey)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                            } else {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                            }
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                        }
                    } catch (e: Exception) {
                        try {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                        } catch (e2: Exception) {
                            Log.e(TAG, "Failed to schedule event alarm for ${event.name}: ${e2.message}")
                        }
                    }
                } else {
                    // Reminder time has already passed!
                    // Check if it is still within the valid recovery window (event day has not completely passed)
                    if (nowMillis <= endOfEventDayMillis) {
                        Log.i(TAG, "Recovering missed event reminder: ${event.name} (trigger: $triggerMillis, now: $nowMillis)")
                        deliverMissedReminder(
                            context = context,
                            eventId = event.id,
                            eventName = event.name,
                            category = event.category.displayName,
                            description = event.description,
                            year = year,
                            month = month,
                            day = day,
                            daysBefore = daysBefore
                        )
                    } else {
                        // Event day has completely ended; mark handled so it won't be processed again
                        markEventReminderDelivered(context, reminderKey)
                    }
                }
            }
        }
    }

    fun cancelAllEventNotifications(context: Context, lookaheadDays: Int = 90) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nowMillis = System.currentTimeMillis()

        for (dayOffset in 0..lookaheadDays) {
            val eventDateCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val year = eventDateCal.get(Calendar.YEAR)
            val month = eventDateCal.get(Calendar.MONTH) + 1
            val day = eventDateCal.get(Calendar.DAY_OF_MONTH)

            val events = NudgeEventsRepository.getEventsForDate(year, month, day)
            for (event in events) {
                val requestCode = getEventRequestCode(event.id, year, month, day)
                val intent = Intent(context, NudgeNotificationReceiver::class.java).apply {
                    action = NudgeNotificationHelper.ACTION_FIRE_EVENT_NOTIFICATION
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    try {
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to cancel event alarm: ${e.message}")
                    }
                }
            }
        }
    }

    fun getEventRequestCode(eventId: String, year: Int, month: Int, day: Int): Int {
        val key = "$eventId-$year-$month-$day"
        return (Math.abs(key.hashCode()) % 400000) + EVENT_ALARM_REQUEST_CODE_BASE
    }
}
