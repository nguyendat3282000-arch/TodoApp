// File: ui/screens/task/AddEditTaskScreen.kt
package com.example.todoapp.ui.screens.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.todoapp.presentation.task.TaskUiState
import com.example.todoapp.presentation.task.TaskViewModel
import com.example.todoapp.ui.components.RoundedTextField
import com.example.todoapp.ui.theme.DialogShape
import com.example.todoapp.ui.theme.Mint100
import com.example.todoapp.ui.theme.Mint500
import com.example.todoapp.ui.theme.PillShape
import com.example.todoapp.ui.theme.TextFieldShape
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ══════════════════════════════════════════════════════════════════════════════
//  AddEditTaskScreen
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: String?,
    onNavigateBack: () -> Unit,
) {
    val uiState   by viewModel.taskUiState.collectAsState()
    val isEditing  = taskId != null

    LaunchedEffect(taskId) {
        if (taskId != null) viewModel.loadTask(taskId)
        else                viewModel.clearTaskForm()
    }

    var title       by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var dueDate     by rememberSaveable { mutableStateOf("") }
    var dueTime     by rememberSaveable { mutableStateOf("") }
    var setAlarm    by rememberSaveable { mutableStateOf(true) }

    val editingTask = (uiState as? TaskUiState.Editing)?.task
    LaunchedEffect(editingTask) {
        editingTask?.let { t ->
            title       = t.title
            description = t.description
            dueDate     = t.dueDate
            dueTime     = t.dueTime
            setAlarm    = t.alarmSet
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is TaskUiState.Saved) onNavigateBack()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val isSaving = uiState is TaskUiState.Loading

    // Hoisted outside the dialog conditional so the state is created once and
    // reused — creating DatePickerState inside an `if` block causes an
    // expensive re-allocation + full layout pass every time the dialog opens.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate.toEpochMilliOrNull()
    )

    // ── Fix: Prevent Infinite Recomposition & Focus Glitch ─────────────────────
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(datePickerState.displayMode) {
        if (datePickerState.displayMode == DisplayMode.Picker) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = if (isEditing) "Edit Task ✏️" else "New Task ✨",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Title ──────────────────────────────────────────────────────────
            FormSectionLabel(emoji = "📝", label = "Title")
            RoundedTextField(
                value         = title,
                onValueChange = { title = it },
                label         = "What do you need to do?",
                leadingIcon   = { Icon(Icons.Rounded.Title, null, tint = Mint500) },
                enabled       = !isSaving,
            )

            Spacer(Modifier.height(4.dp))

            // ── Description ────────────────────────────────────────────────────
            FormSectionLabel(emoji = "💬", label = "Description")
            OutlinedTextField(
                value            = description,
                onValueChange    = { description = it },
                label            = { Text("Add more details (optional)") },
                leadingIcon      = { Icon(Icons.Rounded.Description, null, tint = Mint500) },
                minLines         = 3,
                maxLines         = 6,
                enabled          = !isSaving,
                shape            = TextFieldShape,
                colors           = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Mint500,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                    focusedLabelColor       = Mint500,
                    cursorColor             = Mint500,
                    focusedContainerColor   = Mint100.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))

            // ── Due Date ───────────────────────────────────────────────────────
            FormSectionLabel(emoji = "📅", label = "Due Date")
            DateTimePickerField(
                value       = dueDate,
                placeholder = "Pick a date",
                icon        = { Icon(Icons.Rounded.CalendarMonth, null, tint = Mint500) },
                onClick     = { showDatePicker = true },
            )

            // ── Due Time ───────────────────────────────────────────────────────
            FormSectionLabel(emoji = "⏰", label = "Due Time")
            DateTimePickerField(
                value       = dueTime,
                placeholder = "Pick a time",
                icon        = { Icon(Icons.Rounded.AccessTime, null, tint = Mint500) },
                onClick     = { showTimePicker = true },
            )

            Spacer(Modifier.height(4.dp))

            // ── Alarm toggle (only when date + time are set) ───────────────────
            AnimatedVisibility(
                visible = dueDate.isNotBlank() && dueTime.isNotBlank(),
                enter   = fadeIn(),
                exit    = fadeOut(),
            ) {
                AlarmToggleRow(checked = setAlarm, onChecked = { setAlarm = it })
            }

            Spacer(Modifier.height(16.dp))

            // ── Save button ────────────────────────────────────────────────────
            Button(
                onClick = {
                    if (isEditing && editingTask != null) {
                        viewModel.updateTask(
                            editingTask.copy(
                                title       = title.trim(),
                                description = description.trim(),
                                dueDate     = dueDate,
                                dueTime     = dueTime,
                                alarmSet    = setAlarm && dueDate.isNotBlank() && dueTime.isNotBlank(),
                            )
                        )
                    } else {
                        viewModel.addTask(
                            title       = title.trim(),
                            description = description.trim(),
                            dueDate     = dueDate,
                            dueTime     = dueTime,
                            setAlarm    = setAlarm && dueDate.isNotBlank() && dueTime.isNotBlank(),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = PillShape,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Mint500,
                    contentColor   = Color.White,
                ),
                enabled = !isSaving && title.isNotBlank(),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = Color.White,
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text  = if (isEditing) "Save Changes" else "Add Task",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Date Picker Dialog ─────────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dueDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
            shape = DialogShape,
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Time Picker Dialog ─────────────────────────────────────────────────────
    if (showTimePicker) {
        val (initHour, initMin) = dueTime.parseHourMin()
        val timePickerState = rememberTimePickerState(
            initialHour   = initHour,
            initialMinute = initMin,
            is24Hour      = true,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = DialogShape, color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Pick a time ⏰",
                        style    = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            dueTime = "%02d:%02d".format(
                                timePickerState.hour,
                                timePickerState.minute,
                            )
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Sub-components
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun FormSectionLabel(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji)
        Spacer(Modifier.size(6.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * A read-only field that opens a picker dialog when tapped.
 * Uses a transparent [Box] overlay with [clickable] over a [readOnly] TextField —
 * the correct pattern for non-keyboard input fields in Compose.
 */
@Composable
private fun DateTimePickerField(
    value: String,
    placeholder: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value            = value,
            onValueChange    = {},
            readOnly         = true,
            placeholder      = {
                Text(
                    text  = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingIcon      = icon,
            shape            = TextFieldShape,
            colors           = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Mint500,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                focusedContainerColor   = Mint100.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        // Transparent clickable overlay — captures tap without stealing text input focus
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .clickable(onClick = onClick),
        )
    }
}

@Composable
private fun AlarmToggleRow(checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (checked) Mint100 else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.large,
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector        = if (checked) Icons.Rounded.NotificationsActive
                                     else         Icons.Rounded.NotificationsOff,
                contentDescription = "Alarm",
                tint               = if (checked) Mint500 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(24.dp),
            )
            Column {
                Text(
                    text  = "Set Alarm",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = if (checked) "Will ring at due time 🔔" else "No alarm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked         = checked,
            onCheckedChange = onChecked,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = Mint500,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Helpers
// ══════════════════════════════════════════════════════════════════════════════

private fun String.toEpochMilliOrNull(): Long? = runCatching {
    LocalDate.parse(this)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}.getOrNull()

private fun String.parseHourMin(): Pair<Int, Int> = runCatching {
    val parts = split(":")
    Pair(parts[0].toInt(), parts[1].toInt())
}.getOrDefault(Pair(9, 0))
