package com.example.gentlenudge.backup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.gentlenudge.GentleNudgeApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NudgeBackupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val app = context.applicationContext as? GentleNudgeApp ?: return
        val prefs = NudgeBackupScheduler.getPrefs(context)

        when (action) {
            NudgeBackupScheduler.ACTION_TRIGGER_AUTO_BACKUP -> {
                val isEnabled = prefs.getBoolean(NudgeBackupScheduler.KEY_AUTO_BACKUP_ENABLED, false)
                if (!isEnabled) return

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d("NudgeBackupReceiver", "Executing scheduled automatic local backup...")
                        val result = NudgeBackupManager.performAutomaticBackup(
                            context = context,
                            database = app.database,
                            prefs = prefs
                        )
                        result.onSuccess { file ->
                            Log.d("NudgeBackupReceiver", "Auto backup created successfully: ${file.name} (${file.length()} bytes)")
                        }.onFailure { error ->
                            Log.e("NudgeBackupReceiver", "Auto backup failed: ${error.message}", error)
                        }
                    } catch (e: Exception) {
                        Log.e("NudgeBackupReceiver", "Unexpected error in auto backup: ${e.message}", e)
                    } finally {
                        // Always schedule the next recurring automatic backup
                        NudgeBackupScheduler.scheduleAutoBackup(context)
                        pendingResult.finish()
                    }
                }
            }

            NudgeBackupScheduler.ACTION_TRIGGER_BACKUP_REMINDER -> {
                val isEnabled = prefs.getBoolean(NudgeBackupScheduler.KEY_BACKUP_REMINDER_ENABLED, false)
                if (!isEnabled) return

                val lastBackup = prefs.getLong(NudgeBackupScheduler.KEY_LAST_BACKUP_TIMESTAMP, 0L)
                val intervalDays = prefs.getInt(NudgeBackupScheduler.KEY_BACKUP_REMINDER_DAYS, 30)
                val intervalMillis = intervalDays * 24L * 60L * 60L * 1000L
                val now = System.currentTimeMillis()

                val shouldNotify = if (lastBackup > 0L) {
                    (now - lastBackup) >= intervalMillis
                } else {
                    val lastNotified = prefs.getLong(NudgeBackupScheduler.KEY_BACKUP_REMINDER_LAST_NOTIFIED, 0L)
                    if (lastNotified > 0L) {
                        (now - lastNotified) >= intervalMillis
                    } else {
                        true
                    }
                }

                if (shouldNotify) {
                    NudgeBackupScheduler.showBackupReminderNotification(context)
                    prefs.edit().putLong(NudgeBackupScheduler.KEY_BACKUP_REMINDER_LAST_NOTIFIED, now).apply()
                }

                // Reschedule for next reminder check
                NudgeBackupScheduler.scheduleBackupReminder(context)
            }

            NudgeBackupScheduler.ACTION_BACKUP_REMINDER_LATER -> {
                NudgeBackupScheduler.handleReminderLater(context)
            }
        }
    }
}
