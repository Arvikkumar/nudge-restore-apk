package com.example.gentlenudge.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gentlenudge.backup.NudgeBackupScheduler
import com.example.gentlenudge.ui.components.GentleNudgeLogo
import com.example.gentlenudge.ui.theme.NudgeBlue
import com.example.gentlenudge.ui.theme.NudgeBlueContainer
import java.util.Calendar

@Composable
fun SettingsScreen(
    notificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    nudgeAgainEnabled: Boolean,
    onToggleNudgeAgain: () -> Unit,
    defaultSnooze: String,
    onSelectSnooze: (String) -> Unit,
    deletedTasksCount: Int = 0,
    onOpenRecentlyDeleted: () -> Unit = {},
    onExportPdf: () -> Unit = {},
    onCreateBackup: () -> Unit = {},
    onRestoreBackup: () -> Unit = {},
    // Backup & Data Safety state & actions
    lastBackupTimestamp: Long = 0L,
    backupReminderEnabled: Boolean = false,
    onToggleBackupReminder: () -> Unit = {},
    backupReminderDays: Int = 30,
    onSetBackupReminderDays: (Int) -> Unit = {},
    autoBackupEnabled: Boolean = false,
    onToggleAutoBackup: () -> Unit = {},
    autoBackupDay: Int = Calendar.SUNDAY,
    autoBackupHour: Int = 23,
    autoBackupMinute: Int = 0,
    onSetAutoBackupSchedule: (dayOfWeek: Int, hour: Int, minute: Int) -> Unit = { _, _, _ -> },
    onResetSampleData: () -> Unit = {},
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showReminderFreqDialog by remember { mutableStateOf(false) }
    var showAutoScheduleDialog by remember { mutableStateOf(false) }
    var isAlertsExpanded by rememberSaveable { mutableStateOf(false) }
    var isBackupExpanded by rememberSaveable { mutableStateOf(false) }
    var isHistoryExpanded by rememberSaveable { mutableStateOf(false) }

    val lastBackupRelative = remember(lastBackupTimestamp) {
        NudgeBackupScheduler.formatLastBackupRelative(lastBackupTimestamp)
    }

    val scheduleSummary = remember(autoBackupDay, autoBackupHour, autoBackupMinute) {
        NudgeBackupScheduler.formatScheduleSummary(autoBackupDay, autoBackupHour, autoBackupMinute)
    }

    val nextAutoBackupFormatted = remember(autoBackupDay, autoBackupHour, autoBackupMinute) {
        NudgeBackupScheduler.formatNextAutoBackup(autoBackupDay, autoBackupHour, autoBackupMinute)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Heading
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "MAKE IT YOURS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = NudgeBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Settings that",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "fit your rhythm.",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp
                    ),
                    color = NudgeBlue
                )
            }
        }

        // Clean, minimal, full-width accordion sections: Alerts, Backup, History
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("minimal_accordion_container")
            ) {
                // 1. Alerts Section
                MinimalAccordionSection(
                    title = "Alerts",
                    isExpanded = isAlertsExpanded,
                    onToggle = { isAlertsExpanded = !isAlertsExpanded },
                    testTag = "alerts_accordion_section"
                ) {
                    SettingToggleRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "A little tap when something matters",
                        checked = notificationsEnabled,
                        onCheckedChange = { onToggleNotifications() }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                    SettingActionRow(
                        icon = Icons.Outlined.Schedule,
                        title = "Default snooze",
                        description = "A little breathing room",
                        actionText = "$defaultSnooze ⌵",
                        onClick = {
                            val next = when (defaultSnooze) {
                                "15 minutes" -> "30 minutes"
                                "30 minutes" -> "1 hour"
                                "1 hour" -> "Tomorrow"
                                else -> "15 minutes"
                            }
                            onSelectSnooze(next)
                            onShowToast("Default snooze set to $next.")
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                    SettingToggleRow(
                        icon = Icons.Outlined.Tune,
                        title = "Nudge again",
                        description = "Ask again, softly, if I miss it",
                        checked = nudgeAgainEnabled,
                        onCheckedChange = { onToggleNudgeAgain() }
                    )
                }

                // 2. Backup Section
                MinimalAccordionSection(
                    title = "Backup",
                    isExpanded = isBackupExpanded,
                    onToggle = { isBackupExpanded = !isBackupExpanded },
                    testTag = "backup_accordion_section"
                ) {
                    SettingInfoRow(
                        icon = Icons.Outlined.History,
                        title = "Last backup",
                        description = "Confirmed timestamp of your latest backup",
                        valueText = lastBackupRelative
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                    SettingToggleRow(
                        icon = Icons.Outlined.NotificationsActive,
                        title = "Backup Reminder",
                        description = "Remind me when I haven't backed up recently",
                        checked = backupReminderEnabled,
                        onCheckedChange = { onToggleBackupReminder() }
                    )

                    if (backupReminderEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                        SettingActionRow(
                            icon = Icons.Outlined.AccessTime,
                            title = "Reminder frequency",
                            description = "How often Nudge gently reminds you",
                            actionText = "$backupReminderDays days ⌵",
                            onClick = { showReminderFreqDialog = true }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                    SettingToggleRow(
                        icon = Icons.Outlined.Autorenew,
                        title = "Automatic Backup",
                        description = "Automatically create a local backup of my Nudge data",
                        checked = autoBackupEnabled,
                        onCheckedChange = { onToggleAutoBackup() }
                    )

                    if (autoBackupEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                        SettingActionRow(
                            icon = Icons.Outlined.Event,
                            title = "Backup schedule",
                            description = "Recurring local backup schedule",
                            actionText = "$scheduleSummary ⌵",
                            onClick = { showAutoScheduleDialog = true }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                        SettingInfoRow(
                            icon = Icons.Outlined.Today,
                            title = "Next backup",
                            description = "Scheduled automatic local backup",
                            valueText = nextAutoBackupFormatted
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                    SettingActionRow(
                        icon = Icons.Outlined.FileUpload,
                        title = "Create Local Backup",
                        description = "Export all your notes, pursuits, settings, and attachments to a safe backup file",
                        actionText = "Backup",
                        onClick = onCreateBackup
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                    SettingActionRow(
                        icon = Icons.Outlined.FileDownload,
                        title = "Restore Local Backup",
                        description = "Recover notes, pursuits, and attachments from a previously saved backup file",
                        actionText = "Restore",
                        onClick = onRestoreBackup
                    )
                }

                // 3. History Section
                MinimalAccordionSection(
                    title = "History",
                    isExpanded = isHistoryExpanded,
                    onToggle = { isHistoryExpanded = !isHistoryExpanded },
                    testTag = "history_accordion_section"
                ) {
                    SettingActionRow(
                        icon = Icons.Outlined.DeleteOutline,
                        title = "Recently Deleted",
                        description = "Recover deleted reminders within 30 days",
                        actionText = if (deletedTasksCount > 0) "$deletedTasksCount items ›" else "View ›",
                        onClick = onOpenRecentlyDeleted
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                    SettingActionRow(
                        icon = Icons.Outlined.PictureAsPdf,
                        title = "Export History (PDF)",
                        description = "Download a complete, offline PDF copy of all your completed & pending notes",
                        actionText = "Export PDF",
                        onClick = onExportPdf
                    )
                }
            }
        }

        // Brand signature footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GentleNudgeLogo(
                    size = 36.dp,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = "Nudge",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "A jack of all trades is master of none,\nbut often better than a master of one.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Dialog: Select Backup Reminder Frequency
    if (showReminderFreqDialog) {
        val frequencies = listOf(7, 14, 30, 60, 90)
        var selectedFreq by remember { mutableStateOf(backupReminderDays) }

        AlertDialog(
            onDismissRequest = { showReminderFreqDialog = false },
            title = {
                Text(
                    text = "Backup Reminder Frequency",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Choose how often you would like Nudge to remind you if no recent backup has been created:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    frequencies.forEach { days ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .selectable(
                                    selected = (selectedFreq == days),
                                    onClick = { selectedFreq = days },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedFreq == days),
                                onClick = { selectedFreq = days },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NudgeBlue,
                                    unselectedColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$days days" + if (days == 30) " (Default)" else "",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selectedFreq == days) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetBackupReminderDays(selectedFreq)
                        showReminderFreqDialog = false
                    }
                ) {
                    Text("Save", color = NudgeBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderFreqDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Dialog: Select Automatic Backup Schedule (Day & Time)
    if (showAutoScheduleDialog) {
        val daysOfWeek = listOf(
            0 to "Every day",
            Calendar.SUNDAY to "Every Sunday",
            Calendar.MONDAY to "Every Monday",
            Calendar.TUESDAY to "Every Tuesday",
            Calendar.WEDNESDAY to "Every Wednesday",
            Calendar.THURSDAY to "Every Thursday",
            Calendar.FRIDAY to "Every Friday",
            Calendar.SATURDAY to "Every Saturday"
        )
        var selectedDay by remember { mutableStateOf(autoBackupDay) }
        var selectedHour by remember { mutableStateOf(autoBackupHour) }
        var selectedMinute by remember { mutableStateOf(autoBackupMinute) }

        AlertDialog(
            onDismissRequest = { showAutoScheduleDialog = false },
            title = {
                Text(
                    text = "Automatic Backup Schedule",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select recurring day and time to safely generate a local backup:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Day of Week selection
                    Text(
                        text = "Frequency / Day:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = NudgeBlue
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        daysOfWeek.forEach { (dayVal, dayLabel) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .selectable(
                                        selected = (selectedDay == dayVal),
                                        onClick = { selectedDay = dayVal },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedDay == dayVal),
                                    onClick = { selectedDay = dayVal },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = NudgeBlue,
                                        unselectedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedDay == dayVal) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    // Time Picker Trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NudgeBlueContainer)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        selectedHour = h
                                        selectedMinute = m
                                    },
                                    selectedHour,
                                    selectedMinute,
                                    false
                                ).show()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = NudgeBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Backup Time",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = NudgeBackupScheduler.formatTime(selectedHour, selectedMinute) + " ✎",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NudgeBlue
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetAutoBackupSchedule(selectedDay, selectedHour, selectedMinute)
                        showAutoScheduleDialog = false
                    }
                ) {
                    Text("Save Schedule", color = NudgeBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoScheduleDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun SettingInfoRow(
    icon: ImageVector,
    title: String,
    description: String,
    valueText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(NudgeBlueContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NudgeBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(NudgeBlueContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NudgeBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NudgeBlue,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(NudgeBlueContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NudgeBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(NudgeBlueContainer)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = NudgeBlue
            )
        }
    }
}

@Composable
fun MinimalAccordionSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "chevronRotation_$title"
    )

    val underlineProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "underlineProgress_$title"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        // Transparent clickable header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 2.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse $title" else "Expand $title",
                modifier = Modifier
                    .size(22.dp)
                    .rotate(chevronRotation),
                tint = if (isExpanded) NudgeBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Underline divider: 1px thin light-gray divider when closed, animated subtle blue underline from LEFT to RIGHT when opened
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            val strokeWidthPx = 1.dp.toPx()
            val activeStrokeWidthPx = 2.dp.toPx()

            // 1px base subtle light gray line
            drawLine(
                color = Color(0xFFE2DDD7).copy(alpha = 0.85f),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = strokeWidthPx
            )

            // Animated blue underline expanding from left to right
            if (underlineProgress > 0.001f) {
                drawLine(
                    color = NudgeBlue,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width * underlineProgress, size.height / 2),
                    strokeWidth = activeStrokeWidthPx
                )
            }
        }

        // Expanded settings content
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(250, easing = FastOutSlowInEasing))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 10.dp)
            ) {
                content()
            }
        }
    }
}

