package com.example.gentlenudge.ui.components

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

    // Custom duration state (in minutes)
    var customDurationMinutes by remember { mutableIntStateOf(0) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }

    fun formatDurationLabel(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        val hText = when {
            h == 1 -> "1 hour"
            h > 1 -> "$h hours"
            else -> ""
        }
        val mText = when {
            m == 1 -> "1 minute"
            m > 1 -> "$m minutes"
            else -> ""
        }
        return when {
            h > 0 && m > 0 -> "$hText $mText"
            h > 0 -> hText
            m > 0 -> mText
            else -> "Custom"
        }
    }

    // Notification Style: "One Shot" or "Full Ringtone"
    var selectedNotificationStyle by remember {
        mutableStateOf(if (initialStyle.equals("Full Ringtone", ignoreCase = true)) "Full Ringtone" else "One Shot")
    }

    // Reminders State
    data class ReminderItem(
        val id: String,
        val triggerMillis: Long,
        val label: String,
        val subLabel: String? = null,
        val offsetMinutes: Int? = null
    )
    var selectedReminders by remember { mutableStateOf<List<ReminderItem>>(emptyList()) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showCustomReminderDurationDialog by remember { mutableStateOf(false) }
    var tempReminderHours by remember { mutableIntStateOf(0) }
    var tempReminderMinutes by remember { mutableIntStateOf(15) }

    // Function to calculate target time based on selected duration
    fun calculateTargetMillis(): Long {
        val now = System.currentTimeMillis()
        return when (selectedOption) {
            "30 minutes" -> now + (30 * 60 * 1000L)
            "1 hour" -> now + (60 * 60 * 1000L)
            "Custom" -> {
                if (customDurationMinutes > 0) now + (customDurationMinutes * 60 * 1000L)
                else now + (30 * 60 * 1000L) // fallback
            }
            else -> now + (30 * 60 * 1000L)
        }
    }

    // Prune and recalculate reminders if the duration changes
    LaunchedEffect(selectedOption, customDurationMinutes) {
        val currentEnd = calculateTargetMillis()
        val now = System.currentTimeMillis()
        selectedReminders = selectedReminders.mapNotNull { reminder ->
            if (reminder.offsetMinutes != null) {
                val newTrigger = currentEnd - (reminder.offsetMinutes * 60 * 1000L)
                if (newTrigger > now && newTrigger < currentEnd) {
                    reminder.copy(triggerMillis = newTrigger)
                } else {
                    null // prune if it would fall in the past or at/after session end
                }
            } else {
                if (reminder.triggerMillis < currentEnd && reminder.triggerMillis > now) {
                    reminder
                } else {
                    null
                }
            }
        }
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
                            if (customDurationMinutes > 0) {
                                "until " + timeFormatter.format(Date(now + customDurationMinutes * 60 * 1000L)).lowercase(Locale.getDefault())
                            } else null
                        }
                        else -> null
                    }

                    val displayTitle = if (option == "Custom") {
                        if (customDurationMinutes > 0) formatDurationLabel(customDurationMinutes) else "Custom"
                    } else {
                        option
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (option == "Custom") {
                                    showCustomDurationDialog = true
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
                                    text = displayTitle,
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
                                            if (!reminder.subLabel.isNullOrEmpty()) {
                                                Text(
                                                    text = reminder.subLabel,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = Color(0xFF1E3A8A).copy(alpha = 0.85f)
                                                )
                                            }
                                            Text(
                                                text = if (reminder.subLabel != null) {
                                                    timeFormatter.format(Date(reminder.triggerMillis)).lowercase(Locale.getDefault())
                                                } else {
                                                    "At ${timeFormatter.format(Date(reminder.triggerMillis)).lowercase(Locale.getDefault())}"
                                                },
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
                                            val availableMinutes = ((currentTargetEnd - currentNow) / 60000).toInt()
                                            val initialOffset = when {
                                                availableMinutes > 30 -> 30
                                                availableMinutes > 15 -> 15
                                                availableMinutes > 5 -> 5
                                                else -> 1
                                            }
                                            tempReminderHours = initialOffset / 60
                                            tempReminderMinutes = initialOffset % 60
                                            showCustomReminderDurationDialog = true
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

            // Dialog for choosing custom reminder relative duration
            if (showCustomReminderDurationDialog) {
                val currentTargetEnd = calculateTargetMillis()
                val currentNow = System.currentTimeMillis()
                val offsetMinutes = tempReminderHours * 60 + tempReminderMinutes
                val reminderMillis = currentTargetEnd - (offsetMinutes * 60 * 1000L)
                val isAfterNow = reminderMillis > currentNow
                val isBeforeEnd = offsetMinutes > 0 && reminderMillis < currentTargetEnd
                val isValid = isAfterNow && isBeforeEnd
                val reminderTimeFormatted = timeFormatter.format(Date(reminderMillis)).lowercase(Locale.getDefault())

                val heroDurationText = when {
                    tempReminderHours == 1 && tempReminderMinutes == 0 -> "1 hour"
                    tempReminderHours > 1 && tempReminderMinutes == 0 -> "${tempReminderHours} hours"
                    tempReminderHours > 0 && tempReminderMinutes > 0 -> "${tempReminderHours}h ${tempReminderMinutes}m"
                    tempReminderMinutes > 0 -> "${tempReminderMinutes}m"
                    else -> "0m"
                }

                val subText = when {
                    offsetMinutes <= 0 -> "Choose how long before session ends"
                    !isAfterNow -> "Offset is longer than session remaining"
                    else -> "at $reminderTimeFormatted"
                }

                val reminderQuickPresets = listOf(
                    "15m" to 15,
                    "30m" to 30,
                    "45m" to 45,
                    "1h" to 60,
                    "1h 30m" to 90,
                    "2h" to 120
                )

                AlertDialog(
                    onDismissRequest = { showCustomReminderDurationDialog = false },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "DEEP DIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp,
                                        fontSize = 11.sp
                                    ),
                                    color = NudgeBlue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Set reminder",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 20.sp
                                    ),
                                    color = Color(0xFF161513)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "How long before the session ends?",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF756F67)
                                )
                            }

                            IconButton(
                                onClick = { showCustomReminderDurationDialog = false },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("close_custom_reminder_dialog_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF756F67),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Duration Hero (The visual focus)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = heroDurationText,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 36.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = if (isValid) Color(0xFF161513) else Color(0xFF9E9A92),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = if (isValid) Color(0xFF756F67) else Color(0xFFDC2626),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Unified Stepper Component (Single clean container for Hours + Minutes)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFBFBF9),
                                border = BorderStroke(1.dp, Color(0xFFECEAE4))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Hours Column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "HOURS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                fontSize = 10.5.sp
                                            ),
                                            color = Color(0xFF756F67)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (tempReminderHours > 0) Color(0xFFEFF6FF)
                                                        else Color(0xFFF2F1ED)
                                                    )
                                                    .clickable(enabled = tempReminderHours > 0) {
                                                        tempReminderHours--
                                                    }
                                                    .testTag("decrease_reminder_hours"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Decrease hours",
                                                    tint = if (tempReminderHours > 0) NudgeBlue else Color(0xFFB8B3AB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Text(
                                                text = "$tempReminderHours",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 20.sp
                                                ),
                                                color = Color(0xFF161513),
                                                modifier = Modifier.widthIn(min = 22.dp),
                                                textAlign = TextAlign.Center
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (tempReminderHours < 12) Color(0xFFEFF6FF)
                                                        else Color(0xFFF2F1ED)
                                                    )
                                                    .clickable(enabled = tempReminderHours < 12) {
                                                        tempReminderHours++
                                                    }
                                                    .testTag("increase_reminder_hours"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Increase hours",
                                                    tint = if (tempReminderHours < 12) NudgeBlue else Color(0xFFB8B3AB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Subtle vertical divider between Hours and Minutes
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(36.dp)
                                            .background(Color(0xFFE5E2DA))
                                    )

                                    // Minutes Column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "MINUTES",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                fontSize = 10.5.sp
                                            ),
                                            color = Color(0xFF756F67)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (tempReminderMinutes > 0) Color(0xFFEFF6FF)
                                                        else Color(0xFFF2F1ED)
                                                    )
                                                    .clickable(enabled = tempReminderMinutes > 0) {
                                                        tempReminderMinutes = (tempReminderMinutes - 5).coerceAtLeast(0)
                                                    }
                                                    .testTag("decrease_reminder_minutes"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Decrease minutes",
                                                    tint = if (tempReminderMinutes > 0) NudgeBlue else Color(0xFFB8B3AB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Text(
                                                text = "$tempReminderMinutes",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 20.sp
                                                ),
                                                color = Color(0xFF161513),
                                                modifier = Modifier.widthIn(min = 28.dp),
                                                textAlign = TextAlign.Center
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (tempReminderMinutes < 55) Color(0xFFEFF6FF)
                                                        else Color(0xFFF2F1ED)
                                                    )
                                                    .clickable(enabled = tempReminderMinutes < 55) {
                                                        tempReminderMinutes = (tempReminderMinutes + 5).coerceAtMost(55)
                                                    }
                                                    .testTag("increase_reminder_minutes"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Increase minutes",
                                                    tint = if (tempReminderMinutes < 55) NudgeBlue else Color(0xFFB8B3AB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Quick choices
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "QUICK CHOICES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.9.sp,
                                        fontSize = 10.sp
                                    ),
                                    color = Color(0xFF8C867D)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    reminderQuickPresets.take(3).forEach { (label, mins) ->
                                        val isPresetSelected = offsetMinutes == mins
                                        val isOptionAllowed = (currentTargetEnd - (mins * 60 * 1000L)) > currentNow
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable(enabled = isOptionAllowed) {
                                                    tempReminderHours = mins / 60
                                                    tempReminderMinutes = mins % 60
                                                }
                                                .testTag("quick_reminder_$mins"),
                                            shape = RoundedCornerShape(20.dp),
                                            color = when {
                                                isPresetSelected -> Color(0xFFEFF6FF)
                                                !isOptionAllowed -> Color(0xFFF7F6F2).copy(alpha = 0.5f)
                                                else -> Color(0xFFF7F6F2)
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when {
                                                    isPresetSelected -> NudgeBlue
                                                    !isOptionAllowed -> Color(0xFFE8E5DF).copy(alpha = 0.5f)
                                                    else -> Color(0xFFE8E5DF)
                                                }
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isPresetSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = when {
                                                        isPresetSelected -> NudgeBlue
                                                        !isOptionAllowed -> Color(0xFFB8B3AB)
                                                        else -> Color(0xFF4A463F)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    reminderQuickPresets.drop(3).forEach { (label, mins) ->
                                        val isPresetSelected = offsetMinutes == mins
                                        val isOptionAllowed = (currentTargetEnd - (mins * 60 * 1000L)) > currentNow
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable(enabled = isOptionAllowed) {
                                                    tempReminderHours = mins / 60
                                                    tempReminderMinutes = mins % 60
                                                }
                                                .testTag("quick_reminder_$mins"),
                                            shape = RoundedCornerShape(20.dp),
                                            color = when {
                                                isPresetSelected -> Color(0xFFEFF6FF)
                                                !isOptionAllowed -> Color(0xFFF7F6F2).copy(alpha = 0.5f)
                                                else -> Color(0xFFF7F6F2)
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when {
                                                    isPresetSelected -> NudgeBlue
                                                    !isOptionAllowed -> Color(0xFFE8E5DF).copy(alpha = 0.5f)
                                                    else -> Color(0xFFE8E5DF)
                                                }
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isPresetSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = when {
                                                        isPresetSelected -> NudgeBlue
                                                        !isOptionAllowed -> Color(0xFFB8B3AB)
                                                        else -> Color(0xFF4A463F)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isValid) {
                                    // Duplicate prevention
                                    val isDuplicate = selectedReminders.any { Math.abs(it.triggerMillis - reminderMillis) < 60_000L }
                                    if (isDuplicate) {
                                        Toast.makeText(context, "A reminder for this time already exists.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        selectedReminders = (selectedReminders + ReminderItem(
                                            id = "custom_${reminderMillis}",
                                            triggerMillis = reminderMillis,
                                            label = "Custom",
                                            subLabel = "$heroDurationText before",
                                            offsetMinutes = offsetMinutes
                                        )).sortedBy { it.triggerMillis }
                                        showCustomReminderDurationDialog = false
                                    }
                                }
                            },
                            enabled = isValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NudgeBlue,
                                disabledContainerColor = Color(0xFFD4D0C8)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("custom_reminder_set_button")
                        ) {
                            Text(
                                text = "Set reminder",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = Color.White
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCustomReminderDurationDialog = false },
                            modifier = Modifier.testTag("custom_reminder_cancel_button")
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = Color(0xFF756F67)
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            if (showCustomDurationDialog) {
                var tempHours by remember {
                    mutableIntStateOf(if (customDurationMinutes > 0) customDurationMinutes / 60 else 1)
                }
                var tempMinutes by remember {
                    mutableIntStateOf(if (customDurationMinutes > 0) customDurationMinutes % 60 else 30)
                }

                val totalMinutes = tempHours * 60 + tempMinutes
                val previewEndMillis = System.currentTimeMillis() + (totalMinutes * 60 * 1000L)
                val previewEndFormatted = timeFormatter.format(Date(previewEndMillis)).lowercase(Locale.getDefault())

                val quickPresets = listOf(
                    "1h 30m" to 90,
                    "2h" to 120,
                    "2h 30m" to 150,
                    "3h" to 180
                )

                val heroDurationText = when {
                    tempHours > 0 && tempMinutes > 0 -> "${tempHours}h ${tempMinutes}m"
                    tempHours > 0 -> "${tempHours}h"
                    tempMinutes > 0 -> "${tempMinutes}m"
                    else -> "0m"
                }

                AlertDialog(
                    onDismissRequest = { showCustomDurationDialog = false },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "DEEP DIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp,
                                        fontSize = 11.sp
                                    ),
                                    color = NudgeBlue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Set duration",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 20.sp
                                    ),
                                    color = Color(0xFF161513)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "How long do you want to stay with this?",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF756F67)
                                )
                            }

                            IconButton(
                                onClick = { showCustomDurationDialog = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF756F67),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Duration Hero (The visual focus)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = heroDurationText,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 36.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color(0xFF161513),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (totalMinutes > 0) "until $previewEndFormatted" else "Select a duration",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = Color(0xFF756F67),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Unified Hours & Minutes Stepper Container
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFBFBF9),
                                border = BorderStroke(1.dp, Color(0xFFECEAE4))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Hours Column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "HOURS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                fontSize = 10.5.sp
                                            ),
                                            color = Color(0xFF756F67)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (tempHours > 0) Color(0xFFEFF6FF)
                                                        else Color(0xFFF2F1ED)
                                                    )
                                                    .clickable(enabled = tempHours > 0) {
                                                        tempHours--
                                                    }
                                                    .testTag("custom_duration_hours_minus"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Decrease hours",
                                                    tint = if (tempHours > 0) NudgeBlue else Color(0xFFB8B3AB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Text(
                                                text = "$tempHours",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 20.sp
                                                ),
                                                color = Color(0xFF161513),
                                                modifier = Modifier.widthIn(min = 22.dp),
                                                textAlign = TextAlign.Center
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (tempHours < 12) Color(0xFFEFF6FF)
                                                        else Color(0xFFF2F1ED)
                                                    )
                                                    .clickable(enabled = tempHours < 12) {
                                                        tempHours++
                                                    }
                                                    .testTag("custom_duration_hours_plus"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Increase hours",
                                                    tint = if (tempHours < 12) NudgeBlue else Color(0xFFB8B3AB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Subtle vertical divider between Hours and Minutes
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(36.dp)
                                            .background(Color(0xFFE5E2DA))
                                    )

                                    // Minutes Column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "MINUTES",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                fontSize = 10.5.sp
                                            ),
                                            color = Color(0xFF756F67)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (tempMinutes > 0) Color(0xFFEFF6FF)
                                                        else Color(0xFFF2F1ED)
                                                    )
                                                    .clickable(enabled = tempMinutes > 0) {
                                                        tempMinutes = (tempMinutes - 5).coerceAtLeast(0)
                                                    }
                                                    .testTag("custom_duration_minutes_minus"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Decrease minutes",
                                                    tint = if (tempMinutes > 0) NudgeBlue else Color(0xFFB8B3AB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Text(
                                                text = "$tempMinutes",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 20.sp
                                                ),
                                                color = Color(0xFF161513),
                                                modifier = Modifier.widthIn(min = 28.dp),
                                                textAlign = TextAlign.Center
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (tempMinutes < 55) Color(0xFFEFF6FF)
                                                        else Color(0xFFF2F1ED)
                                                    )
                                                    .clickable(enabled = tempMinutes < 55) {
                                                        tempMinutes = (tempMinutes + 5).coerceAtMost(55)
                                                    }
                                                    .testTag("custom_duration_minutes_plus"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Increase minutes",
                                                    tint = if (tempMinutes < 55) NudgeBlue else Color(0xFFB8B3AB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Quick Choices
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "QUICK CHOICES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.9.sp,
                                        fontSize = 10.sp
                                    ),
                                    color = Color(0xFF8C867D)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    quickPresets.take(2).forEach { (label, mins) ->
                                        val isPresetSelected = totalMinutes == mins
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable {
                                                    tempHours = mins / 60
                                                    tempMinutes = mins % 60
                                                },
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isPresetSelected) Color(0xFFEFF6FF) else Color(0xFFF7F6F2),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isPresetSelected) NudgeBlue else Color(0xFFE8E5DF)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isPresetSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = if (isPresetSelected) NudgeBlue else Color(0xFF4A463F)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    quickPresets.drop(2).forEach { (label, mins) ->
                                        val isPresetSelected = totalMinutes == mins
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable {
                                                    tempHours = mins / 60
                                                    tempMinutes = mins % 60
                                                },
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isPresetSelected) Color(0xFFEFF6FF) else Color(0xFFF7F6F2),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isPresetSelected) NudgeBlue else Color(0xFFE8E5DF)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isPresetSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = if (isPresetSelected) NudgeBlue else Color(0xFF4A463F)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (totalMinutes > 0) {
                                    customDurationMinutes = totalMinutes
                                    selectedOption = "Custom"
                                    showCustomDurationDialog = false
                                }
                            },
                            enabled = totalMinutes > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NudgeBlue,
                                disabledContainerColor = Color(0xFFD4D0C8)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("custom_duration_set_button")
                        ) {
                            Text(
                                text = "Set duration",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = Color.White
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCustomDurationDialog = false },
                            modifier = Modifier.testTag("custom_duration_cancel_button")
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = Color(0xFF756F67)
                            )
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
                        if (selectedOption == "Custom" && customDurationMinutes <= 0) {
                            showCustomDurationDialog = true
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
