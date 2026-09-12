package com.example.gentlenudge.ui.components

import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gentlenudge.R
import com.example.gentlenudge.deepdive.DeepDiveManager
import com.example.gentlenudge.ui.theme.NudgeBlue
import com.example.gentlenudge.ui.theme.NudgeBlueContainer
import com.example.gentlenudge.ui.theme.OnNudgeBlueContainer
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Permanent reusable Deep Dive quick-action card for the "Your Notes" screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepDiveCard(
    state: DeepDiveManager.DeepDiveState,
    onStartSession: (endTimeMillis: Long, style: String) -> Unit,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showConfigSheet by remember { mutableStateOf(false) }
    var showActiveDetailSheet by remember { mutableStateOf(false) }

    // Live tick for remaining time when active
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.isActive, state.endTimeMillis) {
        if (state.isActive && state.endTimeMillis > 0) {
            while (true) {
                currentTimeMillis = System.currentTimeMillis()
                if (currentTimeMillis >= state.endTimeMillis) {
                    break
                }
                delay(1000L)
            }
        }
    }

    val isActuallyActive = state.isActive && state.endTimeMillis > currentTimeMillis

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("deep_dive_card")
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                if (isActuallyActive) {
                    showActiveDetailSheet = true
                } else {
                    showConfigSheet = true
                }
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActuallyActive) {
                NudgeBlueContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isActuallyActive) {
            BorderStroke(1.5.dp, NudgeBlue)
        } else {
            CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(MaterialTheme.colorScheme.outline),
                width = 1.dp
            )
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Left vertical accent stripe matching Nudge task card design language
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 3.dp)
                    .width(4.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NudgeBlue)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Deep Dive",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isActuallyActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NudgeBlue)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (isActuallyActive) {
                        val formattedClock = remember(state.endTimeMillis) {
                            DeepDiveManager.formatClockTime(state.endTimeMillis)
                        }
                        val remainingStr = DeepDiveManager.formatTimeRemaining(state.endTimeMillis)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Pulsing / steady dot
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(NudgeBlue)
                            )
                            Text(
                                text = "In progress · until $formattedClock ($remainingStr)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = NudgeBlue
                            )
                        }
                    } else {
                        Text(
                            text = "Choose a moment, make it yours.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right circular Play button (Start Deep Dive action)
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = NudgeBlue.copy(alpha = 0.25f),
                            spotColor = NudgeBlue.copy(alpha = 0.35f)
                        )
                        .clip(CircleShape)
                        .background(
                            if (isActuallyActive) NudgeBlue.copy(alpha = 0.85f) else NudgeBlue
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActuallyActive) Icons.Outlined.HourglassBottom else Icons.Filled.PlayArrow,
                        contentDescription = "Start Deep Dive",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .then(
                                if (!isActuallyActive) Modifier.padding(start = 2.dp) else Modifier
                            )
                    )
                }
            }
        }
    }

    // Sheet to configure and start Deep Dive
    if (showConfigSheet) {
        DeepDiveConfigSheet(
            initialStyle = state.notificationStyle,
            onDismiss = { showConfigSheet = false },
            onConfirmStart = { endTimeMillis, selectedStyle ->
                showConfigSheet = false
                onStartSession(endTimeMillis, selectedStyle)
            }
        )
    }

    // Sheet to inspect active session or End Session
    if (showActiveDetailSheet && isActuallyActive) {
        DeepDiveActiveDetailSheet(
            state = state,
            onDismiss = { showActiveDetailSheet = false },
            onEndSession = {
                showActiveDetailSheet = false
                onEndSession()
            }
        )
    }
}

/**
 * Clean modal sheet for selecting preset or custom time, plus notification style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepDiveConfigSheet(
    initialStyle: String,
    onDismiss: () -> Unit,
    onConfirmStart: (Long, String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    // Exactly 3 choices: 30 minutes, 1 hour, Custom
    val durationOptions = listOf("30 minutes", "1 hour", "Custom")
    var selectedOption by remember { mutableStateOf("30 minutes") }

    // Custom time state
    var customTargetMillis by remember { mutableLongStateOf(0L) }
    var customTimeText by remember { mutableStateOf<String?>(null) }

    // Notification Style: "One Shot" or "Full Ringtone"
    var selectedNotificationStyle by remember {
        mutableStateOf(if (initialStyle.equals("Full Ringtone", ignoreCase = true)) "Full Ringtone" else "One Shot")
    }

    // Reminders State
    data class ReminderItem(
        val id: String,
        val triggerMillis: Long,
        val label: String
    )
    var selectedReminders by remember { mutableStateOf<List<ReminderItem>>(emptyList()) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    // Function to calculate target time based on selected duration
    fun calculateTargetMillis(): Long {
        val now = System.currentTimeMillis()
        return when (selectedOption) {
            "30 minutes" -> now + (30 * 60 * 1000L)
            "1 hour" -> now + (60 * 60 * 1000L)
            "Custom" -> {
                if (customTargetMillis > now) customTargetMillis
                else now + (30 * 60 * 1000L) // fallback
            }
            else -> now + (30 * 60 * 1000L)
        }
    }

    // Prune reminders that would be at or after the end time if the duration changes
    LaunchedEffect(selectedOption, customTargetMillis) {
        val currentEnd = calculateTargetMillis()
        if (selectedReminders.any { it.triggerMillis >= currentEnd }) {
            selectedReminders = selectedReminders.filter { it.triggerMillis < currentEnd }
        }
    }

    fun openCustomReminderTimePicker(targetEnd: Long) {
        val now = System.currentTimeMillis()
        if (targetEnd <= now + 60_000L) {
            Toast.makeText(context, "No valid reminder time available before the Deep Dive ends.", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = Calendar.getInstance()
        val halfway = (now + targetEnd) / 2
        cal.timeInMillis = halfway

        val is24Hour = DateFormat.is24HourFormat(context)
        val endFormatted = timeFormatter.format(Date(targetEnd)).lowercase(Locale.getDefault())

        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val currentNow = System.currentTimeMillis()
                val currentEnd = calculateTargetMillis()
                val pickedMillis = DeepDiveManager.resolveCustomReminderMillis(
                    hourOfDay = hourOfDay,
                    minute = minute,
                    nowMillis = currentNow,
                    targetEndMillis = currentEnd
                )

                if (pickedMillis == null) {
                    val formattedLimit = timeFormatter.format(Date(currentEnd)).lowercase(Locale.getDefault())
                    Toast.makeText(
                        context,
                        "Reminder must be before the Deep Dive ends (before $formattedLimit).",
                        Toast.LENGTH_LONG
                    ).show()
                    openCustomReminderTimePicker(currentEnd)
                    return@TimePickerDialog
                }

                // Final safety net validation (Requirement 9)
                if (pickedMillis <= currentNow) {
                    Toast.makeText(context, "Reminder must be in the future.", Toast.LENGTH_SHORT).show()
                    openCustomReminderTimePicker(currentEnd)
                    return@TimePickerDialog
                }
                if (pickedMillis >= currentEnd) {
                    Toast.makeText(context, "Reminder must be before the Deep Dive ends.", Toast.LENGTH_SHORT).show()
                    openCustomReminderTimePicker(currentEnd)
                    return@TimePickerDialog
                }

                // Duplicate prevention (Requirement 8)
                val isDuplicate = selectedReminders.any { Math.abs(it.triggerMillis - pickedMillis) < 60_000L }
                if (isDuplicate) {
                    Toast.makeText(context, "A reminder for this time already exists.", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                val formatted = timeFormatter.format(Date(pickedMillis)).lowercase(Locale.getDefault())
                selectedReminders = (selectedReminders + ReminderItem(
                    id = "custom_${pickedMillis}",
                    triggerMillis = pickedMillis,
                    label = "Custom"
                )).sortedBy { it.triggerMillis }
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            is24Hour
        )
        timePickerDialog.setTitle("Reminder (before $endFormatted)")
        timePickerDialog.show()
    }

    fun openCustomTimePicker() {
        val cal = Calendar.getInstance()
        if (customTargetMillis > System.currentTimeMillis()) {
            cal.timeInMillis = customTargetMillis
        } else {
            cal.add(Calendar.MINUTE, 30)
        }

        val is24Hour = DateFormat.is24HourFormat(context)
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val targetCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If chosen time is earlier today, schedule for next occurrence (tomorrow)
                if (targetCal.timeInMillis <= System.currentTimeMillis()) {
                    targetCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                customTargetMillis = targetCal.timeInMillis
                customTimeText = timeFormatter.format(targetCal.time).lowercase(Locale.getDefault())
                selectedOption = "Custom"
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            is24Hour
        )
        timePickerDialog.show()
    }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFD4D0C8))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp)
                .padding(top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "DEEP DIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = NudgeBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose a moment",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color(0xFF161513)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Make it yours.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF756F67)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF161513),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Duration Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DURATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        fontSize = 11.5.sp
                    ),
                    color = Color(0xFF756F67)
                )

                // 3 choices: 30 minutes, 1 hour, Custom
                durationOptions.forEach { option ->
                    val isSelected = selectedOption == option
                    val now = System.currentTimeMillis()
                    val calculatedTime = when (option) {
                        "30 minutes" -> "until " + timeFormatter.format(Date(now + 30 * 60 * 1000L)).lowercase(Locale.getDefault())
                        "1 hour" -> "until " + timeFormatter.format(Date(now + 60 * 60 * 1000L)).lowercase(Locale.getDefault())
                        "Custom" -> {
                            if (customTargetMillis > now) {
                                "until " + timeFormatter.format(Date(customTargetMillis)).lowercase(Locale.getDefault())
                            } else null
                        }
                        else -> null
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (option == "Custom") {
                                    openCustomTimePicker()
                                } else {
                                    selectedOption = option
                                }
                            }
                            .testTag("deep_dive_option_${option.replace(" ", "_")}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFF3F7FF) else Color(0xFFFAFAFA),
                        border = if (isSelected) {
                            BorderStroke(1.2.dp, NudgeBlue)
                        } else {
                            BorderStroke(1.dp, Color(0xFFECEAE4))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    imageVector = if (option == "Custom") Icons.Outlined.Tune else Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF161513),
                                    modifier = Modifier.size(20.dp)
                                )

                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = Color(0xFF161513)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (calculatedTime != null) {
                                    Text(
                                        text = calculatedTime,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = Color(0xFF756F67)
                                    )
                                }

                                Icon(
                                    imageVector = if (isSelected) Icons.Outlined.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) NudgeBlue else Color(0xFFD4D0C8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ADD REMINDERS (OPTIONAL) Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "ADD REMINDERS (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            fontSize = 11.5.sp
                        ),
                        color = Color(0xFF756F67)
                    )
                    Text(
                        text = "Set one or more reminders during this Deep Dive.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = Color(0xFF756F67)
                    )
                }

                // Removable rows of selected reminder points
                if (selectedReminders.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedReminders.forEach { reminder ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .testTag("selected_reminder_${reminder.id}"),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF3F7FF),
                                border = BorderStroke(1.dp, Color(0xFFD0E1FD))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Alarm,
                                            contentDescription = null,
                                            tint = NudgeBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = reminder.label,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.5.sp
                                                ),
                                                color = Color(0xFF1E3A8A)
                                            )
                                            Text(
                                                text = "At ${timeFormatter.format(Date(reminder.triggerMillis)).lowercase(Locale.getDefault())}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.5.sp
                                                ),
                                                color = Color(0xFF756F67)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedReminders = selectedReminders.filter { it.id != reminder.id }
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("remove_reminder_${reminder.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove reminder",
                                            tint = Color(0xFF756F67),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add reminder button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showAddReminderDialog = true }
                        .testTag("add_reminder_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, Color(0xFFECEAE4))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = NudgeBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Add reminder",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            ),
                            color = NudgeBlue
                        )
                    }
                }
            }

            // Dialog for choosing a reminder point
            if (showAddReminderDialog) {
                val now = System.currentTimeMillis()
                val targetEnd = calculateTargetMillis()
                val durationMinutes = ((targetEnd - now) / 60000).toInt()

                AlertDialog(
                    onDismissRequest = { showAddReminderDialog = false },
                    title = {
                        Text(
                            text = "Add reminder",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color(0xFF161513)
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Choose when you want to be reminded before your session ends:",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = Color(0xFF756F67)
                            )

                            // 15 min option: only valid if duration > 15 min
                            if (durationMinutes > 15) {
                                val isAdded = selectedReminders.any { it.label == "15 min" }
                                if (!isAdded) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                val trigger = now + (15 * 60 * 1000L)
                                                selectedReminders = (selectedReminders + ReminderItem(
                                                    id = "15m",
                                                    triggerMillis = trigger,
                                                    label = "15 min"
                                                )).sortedBy { it.triggerMillis }
                                                showAddReminderDialog = false
                                            }
                                            .testTag("reminder_option_15_min"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF3F7FF),
                                        border = BorderStroke(1.dp, Color(0xFFD0E1FD))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Alarm,
                                                    contentDescription = null,
                                                    tint = NudgeBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "15 min",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = Color(0xFF1E3A8A)
                                                )
                                            }
                                            Text(
                                                text = timeFormatter.format(Date(now + 15 * 60 * 1000L)).lowercase(Locale.getDefault()),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF756F67)
                                            )
                                        }
                                    }
                                }
                            }

                            // 30 min option: only valid if duration > 30 min
                            if (durationMinutes > 30) {
                                val isAdded = selectedReminders.any { it.label == "30 min" }
                                if (!isAdded) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                val trigger = now + (30 * 60 * 1000L)
                                                selectedReminders = (selectedReminders + ReminderItem(
                                                    id = "30m",
                                                    triggerMillis = trigger,
                                                    label = "30 min"
                                                )).sortedBy { it.triggerMillis }
                                                showAddReminderDialog = false
                                            }
                                            .testTag("reminder_option_30_min"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF3F7FF),
                                        border = BorderStroke(1.dp, Color(0xFFD0E1FD))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Alarm,
                                                    contentDescription = null,
                                                    tint = NudgeBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "30 min",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = Color(0xFF1E3A8A)
                                                )
                                            }
                                            Text(
                                                text = timeFormatter.format(Date(now + 30 * 60 * 1000L)).lowercase(Locale.getDefault()),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF756F67)
                                            )
                                        }
                                    }
                                }
                            }

                            // 45 min option: only valid if duration > 45 min
                            if (durationMinutes > 45) {
                                val isAdded = selectedReminders.any { it.label == "45 min" }
                                if (!isAdded) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                val trigger = now + (45 * 60 * 1000L)
                                                selectedReminders = (selectedReminders + ReminderItem(
                                                    id = "45m",
                                                    triggerMillis = trigger,
                                                    label = "45 min"
                                                )).sortedBy { it.triggerMillis }
                                                showAddReminderDialog = false
                                            }
                                            .testTag("reminder_option_45_min"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF3F7FF),
                                        border = BorderStroke(1.dp, Color(0xFFD0E1FD))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Alarm,
                                                    contentDescription = null,
                                                    tint = NudgeBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "45 min",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = Color(0xFF1E3A8A)
                                                )
                                            }
                                            Text(
                                                text = timeFormatter.format(Date(now + 45 * 60 * 1000L)).lowercase(Locale.getDefault()),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF756F67)
                                            )
                                        }
                                    }
                                }
                            }

                            // Custom option
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val currentTargetEnd = calculateTargetMillis()
                                        val currentNow = System.currentTimeMillis()
                                        if (currentTargetEnd <= currentNow + 60_000L) {
                                            Toast.makeText(context, "No valid reminder time available before the Deep Dive ends.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showAddReminderDialog = false
                                            openCustomReminderTimePicker(currentTargetEnd)
                                        }
                                    }
                                    .testTag("reminder_option_custom"),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFAFAFA),
                                border = BorderStroke(1.dp, Color(0xFFECEAE4))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = Color(0xFF756F67),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Custom",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            ),
                                            color = Color(0xFF161513)
                                        )
                                    }
                                    val endFormatted = timeFormatter.format(Date(targetEnd)).lowercase(Locale.getDefault())
                                    Text(
                                        text = "Before $endFormatted",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF756F67)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(
                            onClick = { showAddReminderDialog = false },
                            modifier = Modifier.testTag("cancel_add_reminder_button")
                        ) {
                            Text("Cancel", color = Color(0xFF756F67))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Notification Style Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "NOTIFICATION STYLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        fontSize = 11.5.sp
                    ),
                    color = Color(0xFF756F67)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option 1: One Shot
                    val isOneShot = selectedNotificationStyle == "One Shot"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedNotificationStyle = "One Shot" }
                            .testTag("deep_dive_style_one_shot"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOneShot) Color(0xFFF3F7FF) else Color(0xFFFAFAFA),
                        border = if (isOneShot) {
                            BorderStroke(1.2.dp, NudgeBlue)
                        } else {
                            BorderStroke(1.dp, Color(0xFFECEAE4))
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = if (isOneShot) NudgeBlue else Color(0xFF756F67),
                                    modifier = Modifier.size(20.dp)
                                )
                                Icon(
                                    imageVector = if (isOneShot) Icons.Outlined.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isOneShot) NudgeBlue else Color(0xFFD4D0C8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "One Shot",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = if (isOneShot) NudgeBlue else Color(0xFF161513)
                            )
                            Text(
                                text = "One short beep, then stop.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = Color(0xFF756F67)
                            )
                        }
                    }

                    // Option 2: Full Ringtone
                    val isFullRingtone = selectedNotificationStyle == "Full Ringtone"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedNotificationStyle = "Full Ringtone" }
                            .testTag("deep_dive_style_full_ringtone"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFullRingtone) Color(0xFFF3F7FF) else Color(0xFFFAFAFA),
                        border = if (isFullRingtone) {
                            BorderStroke(1.2.dp, NudgeBlue)
                        } else {
                            BorderStroke(1.dp, Color(0xFFECEAE4))
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (isFullRingtone) NudgeBlue else Color(0xFF756F67),
                                    modifier = Modifier.size(20.dp)
                                )
                                Icon(
                                    imageVector = if (isFullRingtone) Icons.Outlined.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isFullRingtone) NudgeBlue else Color(0xFFD4D0C8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Full Ringtone",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = if (isFullRingtone) NudgeBlue else Color(0xFF161513)
                            )
                            Text(
                                text = "Full alarm until dismissed.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = Color(0xFF756F67)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Bottom action button: Soft Blue Tint "Begin" button (#EFF6FF, #1E3A8A text, 20dp corners, soft shadow)
            val beginInteractionSource = remember { MutableInteractionSource() }
            val isBeginPressed by beginInteractionSource.collectIsPressedAsState()

            val beginElevation by animateDpAsState(
                targetValue = if (isBeginPressed) 2.dp else 6.dp,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "begin_elevation"
            )

            val beginTranslationY by animateDpAsState(
                targetValue = if (isBeginPressed) 1.dp else 0.dp,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "begin_translation_y"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer {
                        translationY = beginTranslationY.toPx()
                    }
                    .shadow(
                        elevation = beginElevation,
                        shape = RoundedCornerShape(20.dp),
                        clip = false,
                        ambientColor = Color(0x14000000),
                        spotColor = Color(0x1F000000)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEFF6FF))
                    .clickable(
                        interactionSource = beginInteractionSource,
                        indication = null,
                        role = Role.Button
                    ) {
                        if (selectedOption == "Custom" && customTargetMillis <= System.currentTimeMillis()) {
                            openCustomTimePicker()
                        } else {
                            val targetMillis = calculateTargetMillis()
                            val now = System.currentTimeMillis()
                            val points = selectedReminders
                                .filter { it.triggerMillis > now && it.triggerMillis < targetMillis }
                                .sortedBy { it.triggerMillis }
                                .map { DeepDiveManager.ReminderPoint(it.triggerMillis, it.label) }
                            DeepDiveManager.setPendingReminders(points)
                            onConfirmStart(targetMillis, selectedNotificationStyle)
                        }
                    }
                    .testTag("start_deep_dive_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Begin",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF1E3A8A)
                )
            }
        }
    }
}

/**
 * Modal bottom sheet displayed when the user taps on an active Deep Dive card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepDiveActiveDetailSheet(
    state: DeepDiveManager.DeepDiveState,
    onDismiss: () -> Unit,
    onEndSession: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val formattedClock = remember(state.endTimeMillis) {
        DeepDiveManager.formatClockTime(state.endTimeMillis)
    }

    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.endTimeMillis) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val remainingStr = remember(currentTimeMillis, state.endTimeMillis) {
        DeepDiveManager.formatTimeRemaining(state.endTimeMillis)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DEEP DIVE IN PROGRESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = NudgeBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Until $formattedClock",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = NudgeBlueContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, NudgeBlue.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassBottom,
                            contentDescription = null,
                            tint = NudgeBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Remaining: $remainingStr",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = OnNudgeBlueContainer
                        )
                    }

                    Text(
                        text = "Notification: ${state.notificationStyle}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (state.reminderPoints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Scheduled reminders:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = NudgeBlue
                        )
                        state.reminderPoints.forEach { reminder ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = null,
                                    tint = NudgeBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${reminder.label} · ${DeepDiveManager.formatClockTime(reminder.triggerTimeMillis)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onEndSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("end_deep_dive_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.StopCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "End Session",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                )
            }
        }
    }
}
