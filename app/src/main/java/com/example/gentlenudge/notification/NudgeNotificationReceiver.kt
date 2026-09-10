package com.example.gentlenudge.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.gentlenudge.GentleNudgeApp
import com.example.gentlenudge.data.model.NudgeTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NudgeNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == NudgeNotificationHelper.ACTION_FIRE_EVENT_NOTIFICATION) {
            val eventId = intent.getStringExtra(NudgeNotificationHelper.EXTRA_EVENT_ID) ?: ""
            val eventName = intent.getStringExtra(NudgeNotificationHelper.EXTRA_EVENT_NAME) ?: "Calendar Event"
            val category = intent.getStringExtra(NudgeNotificationHelper.EXTRA_EVENT_CATEGORY) ?: ""
            val description = intent.getStringExtra(NudgeNotificationHelper.EXTRA_EVENT_DESCRIPTION) ?: ""
            val dateLabel = intent.getStringExtra(NudgeNotificationHelper.EXTRA_EVENT_DATE_LABEL) ?: ""
            val daysBefore = intent.getIntExtra(NudgeNotificationHelper.EXTRA_DAYS_BEFORE, 1)
            val year = intent.getIntExtra(NudgeNotificationHelper.EXTRA_EVENT_YEAR, 0)
            val month = intent.getIntExtra(NudgeNotificationHelper.EXTRA_EVENT_MONTH, 0)
            val day = intent.getIntExtra(NudgeNotificationHelper.EXTRA_EVENT_DAY, 0)

            val reminderKey = intent.getStringExtra(NudgeNotificationHelper.EXTRA_REMINDER_KEY)
                ?: NudgeEventNotificationScheduler.getEventReminderKey(eventId, year, month, day)

            synchronized(NudgeEventNotificationScheduler.deliveryLock) {
                if (NudgeEventNotificationScheduler.isEventReminderDelivered(context, reminderKey)) {
                    Log.d("NudgeNotificationReceiver", "Event reminder already delivered for $reminderKey, skipping duplicate.")
                    return
                }

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
                    NudgeEventNotificationScheduler.markEventReminderDelivered(context, reminderKey)
                }
            }
            return
        }

        if (action == com.example.gentlenudge.deepdive.DeepDiveManager.ACTION_FIRE_DEEP_DIVE) {
            val style = intent.getStringExtra("deep_dive_notification_style") ?: "One Shot"
            com.example.gentlenudge.deepdive.DeepDiveManager.onAlarmFired(context, style)
            return
        }

        if (action == com.example.gentlenudge.deepdive.DeepDiveManager.ACTION_DISMISS_DEEP_DIVE) {
            com.example.gentlenudge.deepdive.DeepDiveManager.dismissNotification(context)
            return
        }

        val taskId = intent.getLongExtra(NudgeNotificationHelper.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val app = context.applicationContext as? GentleNudgeApp ?: return
        val repository = app.repository
        val dao = app.database.nudgeTaskDao()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    NudgeNotificationHelper.ACTION_FIRE_NUDGE -> {
                        val task = dao.getTaskById(taskId)

                        // Strict check: Only notify for legitimate, active, non-deleted tasks
                        val taskToNotify = if (task != null && !task.isDone && !task.isDeleted) {
                            task
                        } else {
                            null
                        }

                        if (taskToNotify != null) {
                            val isRepeating = NudgeAlarmScheduler.isTaskRepeating(taskToNotify)

                            if (!isRepeating) {
                                val scheduledExtra = intent.getLongExtra(NudgeNotificationHelper.EXTRA_TASK_SCHEDULED_MILLIS, -1L)
                                val scheduledMillis = if (scheduledExtra > 0L) scheduledExtra else NudgeAlarmScheduler.getScheduledTriggerMillis(context, taskToNotify)
                                val occurrenceKey = NudgeAlarmScheduler.getTaskOccurrenceKey(taskToNotify.id, scheduledMillis)

                                if (!NudgeAlarmScheduler.tryClaimTaskReminderDelivery(context, occurrenceKey)) {
                                    Log.d("NudgeReceiver", "Task ${taskToNotify.id} occurrence $occurrenceKey already claimed or delivered. Skipping duplicate.")
                                    return@launch
                                }
                            }

                            NudgeNotificationHelper.showNudgeNotification(context, taskToNotify)

                            // Handle repetition if set
                            if (isRepeating) {
                                val scheduledExtra = intent.getLongExtra(NudgeNotificationHelper.EXTRA_TASK_SCHEDULED_MILLIS, -1L)
                                val firedOccurrenceMillis = if (scheduledExtra > 0L) scheduledExtra else NudgeAlarmScheduler.getScheduledTriggerMillis(context, taskToNotify)
                                val nextTask = calculateNextOccurrenceTask(taskToNotify, firedOccurrenceMillis)
                                if (nextTask != null) {
                                    dao.updateTask(nextTask)
                                    val baseCal = Calendar.getInstance().apply { timeInMillis = firedOccurrenceMillis }
                                    val advancedCal = calculateNextCalendar(baseCal, taskToNotify.repeat.trim())
                                    if (advancedCal != null) {
                                        NudgeAlarmScheduler.saveScheduledTriggerMillis(context, nextTask.id, advancedCal.timeInMillis)
                                    }
                                    NudgeAlarmScheduler.scheduleTask(context, nextTask, forceRecalculate = false)
                                }
                            } else {
                                // Mark non-repeating task completed/expired immediately upon notification delivery
                                val completedTask = taskToNotify.copy(
                                    isDone = true,
                                    completedAt = System.currentTimeMillis()
                                )
                                dao.updateTask(completedTask)
                            }
                        }
                    }

                    NudgeNotificationHelper.ACTION_MARK_DONE -> {
                        val task = dao.getTaskById(taskId)
                        if (task != null) {
                            if (!task.isDone) {
                                repository.toggleDone(task)
                            }
                        }
                        // Always dismiss notification even if task was already deleted/removed
                        NudgeNotificationHelper.dismissNotification(context, taskId)
                    }

                    NudgeNotificationHelper.ACTION_SNOOZE_NUDGE -> {
                        val task = dao.getTaskById(taskId)
                        if (task != null) {
                            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val snoozedTime = timeFormat.format(Date(System.currentTimeMillis() + (30 * 60 * 1000L)))
                            val updatedTask = task.copy(
                                timeLabel = snoozedTime,
                                section = "today"
                            )
                            dao.updateTask(updatedTask)
                            NudgeAlarmScheduler.scheduleTask(context, updatedTask, forceRecalculate = true)
                            NudgeNotificationHelper.dismissNotification(context, taskId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NudgeReceiver", "Error processing notification action: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun calculateNextOccurrenceTask(task: NudgeTask, baseOccurrenceMillis: Long = -1L): NudgeTask? {
            val repeatRule = task.repeat.trim()
            if (repeatRule.isBlank() || repeatRule.equals("Does not repeat", ignoreCase = true) || repeatRule.equals("Once", ignoreCase = true)) {
                return null
            }

            // 1. Calculate the base scheduled occurrence millis (NOT current time)
            val baseTriggerMillis = if (baseOccurrenceMillis > 0L) {
                baseOccurrenceMillis
            } else {
                val baseRef = if (task.createdAt > 0L) task.createdAt else System.currentTimeMillis()
                NudgeAlarmScheduler.calculateTriggerMillis(task.dateLabel, task.timeLabel, baseRef)
            }
            val baseCal = Calendar.getInstance().apply {
                timeInMillis = baseTriggerMillis
            }

            // 2. Advance baseCal according to repeat rule
            val advancedCal = calculateNextCalendar(baseCal, repeatRule) ?: return null

            // 3. Format next dateLabel and section
            val nextDateLabel = formatOccurrenceDateLabel(advancedCal)
            val nextSection = if (isCalendarToday(advancedCal)) "today" else "later"

            return task.copy(
                dateLabel = nextDateLabel,
                section = nextSection
            )
        }

        fun calculateNextCalendar(baseCal: Calendar, repeatRule: String): Calendar? {
            val nextCal = baseCal.clone() as Calendar
            val ruleClean = repeatRule.trim().lowercase()

            when {
                ruleClean == "every day" || ruleClean == "daily" -> {
                    nextCal.add(Calendar.DAY_OF_YEAR, 1)
                    return nextCal
                }
                ruleClean == "every week" || ruleClean == "weekly" -> {
                    nextCal.add(Calendar.DAY_OF_YEAR, 7)
                    return nextCal
                }
                ruleClean == "every month" || ruleClean == "monthly" -> {
                    nextCal.add(Calendar.MONTH, 1)
                    return nextCal
                }
                ruleClean == "every year" || ruleClean == "yearly" -> {
                    nextCal.add(Calendar.YEAR, 1)
                    return nextCal
                }
                ruleClean.startsWith("weekdays") -> {
                    // Advance to next Monday–Friday
                    do {
                        nextCal.add(Calendar.DAY_OF_YEAR, 1)
                        val dow = nextCal.get(Calendar.DAY_OF_WEEK)
                    } while (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY)
                    return nextCal
                }
                ruleClean.startsWith("weekends") -> {
                    // Advance to next Saturday or Sunday
                    do {
                        nextCal.add(Calendar.DAY_OF_YEAR, 1)
                        val dow = nextCal.get(Calendar.DAY_OF_WEEK)
                    } while (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY)
                    return nextCal
                }
                else -> {
                    // Parse "Every X days/weeks/months/years"
                    val customPattern = Regex("(?i)^every\\s+(\\d+)\\s*(days?|weeks?|months?|years?)$")
                    val match = customPattern.find(repeatRule.trim())
                    if (match != null) {
                        val count = match.groupValues[1].toIntOrNull() ?: return null
                        if (count <= 0) return null
                        val unit = match.groupValues[2].lowercase()
                        when {
                            unit.startsWith("day") -> nextCal.add(Calendar.DAY_OF_YEAR, count)
                            unit.startsWith("week") -> nextCal.add(Calendar.DAY_OF_YEAR, count * 7)
                            unit.startsWith("month") -> nextCal.add(Calendar.MONTH, count)
                            unit.startsWith("year") -> nextCal.add(Calendar.YEAR, count)
                            else -> return null
                        }
                        return nextCal
                    }
                    return null
                }
            }
        }

        fun formatOccurrenceDateLabel(targetCal: Calendar): String {
            val todayCal = Calendar.getInstance()
            val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

            if (isSameDay(targetCal, todayCal)) {
                return "Today"
            }
            if (isSameDay(targetCal, tomorrowCal)) {
                return "Tomorrow"
            }

            val currentYear = todayCal.get(Calendar.YEAR)
            val targetYear = targetCal.get(Calendar.YEAR)
            return if (targetYear == currentYear) {
                SimpleDateFormat("MMM d", Locale.US).format(targetCal.time)
            } else {
                SimpleDateFormat("MMM d, yyyy", Locale.US).format(targetCal.time)
            }
        }

        private fun isCalendarToday(cal: Calendar): Boolean {
            val now = Calendar.getInstance()
            return isSameDay(cal, now)
        }

        private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
            return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                    c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                    c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
        }
    }
}
