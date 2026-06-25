// File: ui/screens/task/AddEditTaskScreen.kt
package com.example.todoapp.ui.screens.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskType
import com.example.todoapp.domain.model.FrequencyType
import com.example.todoapp.domain.model.FlexibleInterval
import com.example.todoapp.presentation.task.TaskUiState
import com.example.todoapp.presentation.task.TaskViewModel
import com.example.todoapp.ui.components.RoundedTextField
import com.example.todoapp.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: String?,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.taskUiState.collectAsState()
    val isEditing = taskId != null

    LaunchedEffect(taskId) {
        if (taskId != null) viewModel.loadTask(taskId)
        else viewModel.clearTaskForm()
    }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf("") }
    var dueTime by rememberSaveable { mutableStateOf("") }
    var setAlarm by rememberSaveable { mutableStateOf(true) }

    // --- Giai Đoạn 2 States ---
    var taskType by rememberSaveable { mutableStateOf(TaskType.DAILY) }
    var frequencyType by rememberSaveable { mutableStateOf(FrequencyType.FIXED) }
    var fixedDays by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    var flexibleCount by rememberSaveable { mutableStateOf(1) }
    var flexibleInterval by rememberSaveable { mutableStateOf(FlexibleInterval.WEEK) }

    val editingTask = (uiState as? TaskUiState.Editing)?.task
    LaunchedEffect(editingTask) {
        editingTask?.let { t ->
            title = t.title
            description = t.description
            dueDate = t.dueDate
            dueTime = t.dueTime
            setAlarm = t.alarmSet

            taskType = t.type
            frequencyType = t.frequencyType ?: FrequencyType.FIXED
            fixedDays = t.fixedDays
            flexibleCount = if (t.flexibleCount > 0) t.flexibleCount else 1
            flexibleInterval = t.flexibleInterval ?: FlexibleInterval.WEEK
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is TaskUiState.Saved) onNavigateBack()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val isSaving = uiState is TaskUiState.Loading

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate.toEpochMilliOrNull()
    )

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
                        text = if (isEditing) "Chỉnh sửa nhiệm vụ ✏️" else "Nhiệm vụ mới ✨",
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

            // ── Phân loại Task ─────────────────────────────────────────────────
            FormSectionLabel(emoji = "🏷️", label = "Loại nhiệm vụ")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = taskType == TaskType.DAILY,
                    onClick = { taskType = TaskType.DAILY },
                    label = { Text("Nhiệm vụ trong ngày") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryContainer,
                        selectedLabelColor = Secondary
                    )
                )
                FilterChip(
                    selected = taskType == TaskType.HABIT,
                    onClick = { taskType = TaskType.HABIT },
                    label = { Text("Nhiệm vụ dài hạn") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryContainer,
                        selectedLabelColor = Secondary
                    )
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Tiêu đề ────────────────────────────────────────────────────────
            FormSectionLabel(emoji = "📝", label = "Tiêu đề")
            RoundedTextField(
                value = title,
                onValueChange = { title = it },
                label = "Bạn cần thực hiện việc gì?",
                leadingIcon = { Icon(Icons.Rounded.Title, null, tint = Primary) },
                enabled = !isSaving,
            )

            // Gợi ý thông minh (Suggestions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.taskSuggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { title = suggestion },
                        label = { Text(suggestion) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Habit Configuration (Chỉ hiện khi chọn Habit) ────────────────
            if (taskType == TaskType.HABIT) {
                FormSectionLabel(emoji = "🔄", label = "Chu kỳ nhiệm vụ dài hạn")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = frequencyType == FrequencyType.FIXED,
                        onClick = { frequencyType = FrequencyType.FIXED },
                        label = { Text("Ngày cố định") }
                    )
                    FilterChip(
                        selected = frequencyType == FrequencyType.FLEXIBLE,
                        onClick = { frequencyType = FrequencyType.FLEXIBLE },
                        label = { Text("Số buổi linh hoạt") }
                    )
                }

                if (frequencyType == FrequencyType.FIXED) {
                    Text("Lặp lại vào các ngày:", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weekdays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                        weekdays.forEachIndexed { index, day ->
                            val dayVal = index + 1
                            val isSelected = fixedDays.contains(dayVal)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    fixedDays = if (isSelected) fixedDays - dayVal else fixedDays + dayVal
                                },
                                label = { Text(day) }
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Số lần cần làm: $flexibleCount")
                            Slider(
                                value = flexibleCount.toFloat(),
                                onValueChange = { flexibleCount = it.toInt() },
                                valueRange = 1f..7f,
                                steps = 5,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(
                                selected = flexibleInterval == FlexibleInterval.WEEK,
                                onClick = { flexibleInterval = FlexibleInterval.WEEK },
                                label = { Text("Mỗi tuần") }
                            )
                            FilterChip(
                                selected = flexibleInterval == FlexibleInterval.MONTH,
                                onClick = { flexibleInterval = FlexibleInterval.MONTH },
                                label = { Text("Mỗi tháng") }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // ── Mô tả ──────────────────────────────────────────────────────────
            FormSectionLabel(emoji = "💬", label = "Mô tả chi tiết")
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Thêm ghi chú cụ thể (không bắt buộc)") },
                leadingIcon = { Icon(Icons.Rounded.Description, null, tint = Primary) },
                minLines = 3,
                maxLines = 6,
                enabled = !isSaving,
                shape = TextAreaShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = Primary,
                    cursorColor = Primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // Gợi ý thời lượng thói quen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.durationSuggestions.forEach { duration ->
                    SuggestionChip(
                        onClick = {
                            if (description.isBlank()) description = duration
                            else description += " - $duration"
                        },
                        label = { Text(duration) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Ngày hạn chót (Chỉ cho Daily Task) ────────────────────────────
            if (taskType == TaskType.DAILY) {
                FormSectionLabel(emoji = "📅", label = "Ngày thực hiện")
                Text(
                    text = "Chọn ngày bạn sẽ thực hiện nhiệm vụ này (ví dụ: ngày hôm nay, ngày mai, hoặc một ngày cụ thể)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DateTimePickerField(
                    value = dueDate,
                    placeholder = "Chọn ngày thực hiện",
                    icon = { Icon(Icons.Rounded.CalendarMonth, null, tint = Primary) },
                    onClick = { showDatePicker = true },
                )

                // Gợi ý ngày nhanh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val todayStr = LocalDate.now().toString()
                    val tomorrowStr = LocalDate.now().plusDays(1).toString()
                    val weekendStr = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY)).toString()
                    val nextWeekStr = LocalDate.now().plusWeeks(1).toString()

                    SuggestionChip(onClick = { dueDate = todayStr }, label = { Text("Hôm nay") })
                    SuggestionChip(onClick = { dueDate = tomorrowStr }, label = { Text("Ngày mai") })
                    SuggestionChip(onClick = { dueDate = weekendStr }, label = { Text("Cuối tuần") })
                    SuggestionChip(onClick = { dueDate = nextWeekStr }, label = { Text("Tuần sau") })
                }

                Spacer(Modifier.height(4.dp))
            }

            // ── Giờ hạn chót ───────────────────────────────────────────────────
            FormSectionLabel(emoji = "⏰", label = "Thời gian")
            Text(
                text = "Chọn thời gian cụ thể trong ngày để thực hiện và nhận thông báo nhắc nhở từ hệ thống",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DateTimePickerField(
                value = dueTime,
                placeholder = "Chọn giờ",
                icon = { Icon(Icons.Rounded.AccessTime, null, tint = Primary) },
                onClick = { showTimePicker = true },
            )

            // Gợi ý giờ nhanh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(onClick = { dueTime = "07:00" }, label = { Text("07:00") })
                SuggestionChip(onClick = { dueTime = "09:00" }, label = { Text("09:00") })
                SuggestionChip(onClick = { dueTime = "14:00" }, label = { Text("14:00") })
                SuggestionChip(onClick = { dueTime = "20:00" }, label = { Text("20:00") })
            }

            Spacer(Modifier.height(4.dp))

            // ── Alarm toggle ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = (taskType == TaskType.HABIT && dueTime.isNotBlank()) || (dueDate.isNotBlank() && dueTime.isNotBlank()),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                AlarmToggleRow(checked = setAlarm, onChecked = { setAlarm = it })
            }

            Spacer(Modifier.height(16.dp))

            // ── Nút Lưu ────────────────────────────────────────────────────────
            Button(
                onClick = {
                    val finalFreqType = if (taskType == TaskType.HABIT) frequencyType else null
                    val finalFixedDays = if (taskType == TaskType.HABIT && frequencyType == FrequencyType.FIXED) fixedDays else emptyList()
                    val finalFlexCount = if (taskType == TaskType.HABIT && frequencyType == FrequencyType.FLEXIBLE) flexibleCount else 0
                    val finalFlexInterval = if (taskType == TaskType.HABIT && frequencyType == FrequencyType.FLEXIBLE) flexibleInterval else null

                    if (isEditing && editingTask != null) {
                        viewModel.updateTask(
                            editingTask.copy(
                                title = title.trim(),
                                description = description.trim(),
                                dueDate = if (taskType == TaskType.HABIT) "" else dueDate,
                                dueTime = dueTime,
                                alarmSet = setAlarm && ((taskType == TaskType.HABIT && dueTime.isNotBlank()) || (dueDate.isNotBlank() && dueTime.isNotBlank())),
                                type = taskType,
                                frequencyType = finalFreqType,
                                fixedDays = finalFixedDays,
                                flexibleCount = finalFlexCount,
                                flexibleInterval = finalFlexInterval
                            )
                        )
                    } else {
                        viewModel.addTask(
                            title = title.trim(),
                            description = description.trim(),
                            dueDate = if (taskType == TaskType.HABIT) "" else dueDate,
                            dueTime = dueTime,
                            setAlarm = setAlarm && ((taskType == TaskType.HABIT && dueTime.isNotBlank()) || (dueDate.isNotBlank() && dueTime.isNotBlank())),
                            type = taskType,
                            frequencyType = finalFreqType,
                            fixedDays = finalFixedDays,
                            flexibleCount = finalFlexCount,
                            flexibleInterval = finalFlexInterval
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White,
                ),
                enabled = !isSaving && title.isNotBlank() && !(taskType == TaskType.HABIT && frequencyType == FrequencyType.FIXED && fixedDays.isEmpty()),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = if (isEditing) "Lưu thay đổi" else "Thêm nhiệm vụ",
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
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dueDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
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
            initialHour = initHour,
            initialMinute = initMin,
            is24Hour = true,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = DialogShape, color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Chọn thời gian ⏰",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Hủy") }
                        TextButton(onClick = {
                            dueTime = "%02d:%02d".format(
                                timePickerState.hour,
                                timePickerState.minute,
                            )
                            showTimePicker = false
                        }) { Text("Xác nhận") }
                    }
                }
            }
        }
    }
}

// ── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun FormSectionLabel(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji)
        Spacer(Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DateTimePickerField(
    value: String,
    placeholder: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingIcon = icon,
            shape = PillShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
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
                color = if (checked) SecondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.large,
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = if (checked) Icons.Rounded.NotificationsActive
                else Icons.Rounded.NotificationsOff,
                contentDescription = "Alarm",
                tint = if (checked) Mint500 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Column {
                Text(
                    text = "Hẹn giờ nhắc nhở",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (checked) "Chuông sẽ báo vào thời gian đã chọn 🔔" else "Không báo chuông",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Mint500,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

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
