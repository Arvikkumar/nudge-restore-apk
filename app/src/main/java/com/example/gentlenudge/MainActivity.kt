package com.example.gentlenudge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.gentlenudge.notification.NudgeNotificationHelper
import com.example.gentlenudge.ui.MainScreen
import com.example.gentlenudge.ui.theme.GentleNudgeTheme
import com.example.gentlenudge.ui.viewmodel.NudgeViewModel
import com.example.gentlenudge.ui.viewmodel.NudgeViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: NudgeViewModel by viewModels {
        val app = application as GentleNudgeApp
        NudgeViewModelFactory(app, app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            // Create notification channels for nudges and backups
            NudgeNotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to create notification channels: ${e.message}")
        }

        setContent {
            GentleNudgeTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            com.example.gentlenudge.notification.NudgeEventNotificationScheduler.rescheduleIfEnabled(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to re-evaluate event reminders: ${e.message}")
        }
    }
}
