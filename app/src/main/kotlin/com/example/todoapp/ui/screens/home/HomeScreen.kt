// File: ui/screens/home/HomeScreen.kt
package com.example.todoapp.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskType
import com.example.todoapp.domain.model.UserStats
import com.example.todoapp.presentation.task.TaskViewModel
import com.example.todoapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.HorizontalDivider

enum class HomeTab {
    TASKS,
    DASHBOARD
}

enum class TaskSubTab {
    DAILY,
    HABITS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    var activeTab by remember { mutableStateOf(HomeTab.TASKS) }
    var taskSubTab by remember { mutableStateOf(TaskSubTab.DAILY) }
    var selectedTaskForDetails by remember { mutableStateOf<Task?>(null) }

    val tasks by viewModel.tasks.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val weeklyLogs by viewModel.weeklyTaskLogs.collectAsState()

    val today = LocalDate.now()
    val greeting = greeting()

    Scaffold(
        floatingActionButton = {
            if (activeTab == HomeTab.TASKS) {
                FloatingActionButton(
                    onClick = onAddTask,
                    shape = PillShape,
                    containerColor = Primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier.padding(bottom = 72.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add task", modifier = Modifier.size(28.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = innerPadding.calculateBottomPadding(),
                    start = innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                )
        ) {
            // Main Content Area
            Column(modifier = Modifier.fillMaxSize()) {
                // Glassmorphic top app bar matching Stitch design
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TodoApp ✨",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Cài đặt",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                when (activeTab) {
                    HomeTab.TASKS -> {
                        // Segmented Control Tabs for Daily vs Habits
                        TabRow(
                            selectedTabIndex = taskSubTab.ordinal,
                            containerColor = Color.Transparent,
                            contentColor = Mint500,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[taskSubTab.ordinal]),
                                    color = Mint500
                                )
                            }
                        ) {
                            Tab(
                                selected = taskSubTab == TaskSubTab.DAILY,
                                onClick = { taskSubTab = TaskSubTab.DAILY },
                                text = { Text("Nhiệm vụ trong ngày", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = taskSubTab == TaskSubTab.HABITS,
                                onClick = { taskSubTab = TaskSubTab.HABITS },
                                text = { Text("Nhiệm vụ dài hạn", fontWeight = FontWeight.Bold) } // Sửa text theo yêu cầu
                            )
                        }

                        when (taskSubTab) {
                            TaskSubTab.DAILY -> {
                                val dailyTasks = tasks.filter { it.type == TaskType.DAILY }
                                val pending = dailyTasks.filter { !it.isDone }
                                val done = dailyTasks.filter { it.isDone }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // ── Greeting & Date (Greeting is scrollable inside list to avoid top blank space) ──
                                    item {
                                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                            Text(
                                                text = greeting,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = today.format(
                                                    DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
                                                ),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // Quote of the day banner
                                    item {
                                        val quotes = listOf(
                                            "Một ngày mới bắt đầu, hãy biến hôm nay trở nên tuyệt vời! ☀️",
                                            "Mỗi ngày là một cơ hội để tiến gần hơn tới mục tiêu của bạn. 🌟",
                                            "Đừng chờ đợi cơ hội, hãy tự tạo ra nó! 💪",
                                            "Kỷ luật là cầu nối giữa mục tiêu và thành tựu. 🎯",
                                            "Hãy làm những việc nhỏ bé với một tình yêu lớn lao. ❤️"
                                        )
                                        val quote = quotes[today.dayOfMonth % quotes.size]
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Mint100.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text("💡", fontSize = 24.sp)
                                                Text(
                                                    text = quote,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                    color = Mint500
                                                )
                                            }
                                        }
                                    }

                                    if (dailyTasks.isEmpty()) {
                                        item {
                                            EmptyState("Chưa có nhiệm vụ nào cho hôm nay. Bấm + để thêm nhiệm vụ mới! 🎉")
                                        }
                                    } else {
                                        if (pending.isNotEmpty()) {
                                            item { SectionHeader("🔥", "Đang đợi làm (${pending.size})") }
                                            itemsIndexed(pending, key = { _, task -> task.id }) { index, task ->
                                                SwipeableTaskCard(
                                                    task = task,
                                                    onToggle = { viewModel.toggleDone(task) },
                                                    onEdit = { onEditTask(task.id) },
                                                    onDelete = { viewModel.deleteTask(task.id) },
                                                    onClick = { selectedTaskForDetails = task }
                                                )
                                            }
                                        }
                                        if (done.isNotEmpty()) {
                                            item { Spacer(Modifier.height(8.dp)) }
                                            item { SectionHeader("✅", "Đã hoàn thành (${done.size})") }
                                            itemsIndexed(done, key = { _, task -> task.id }) { index, task ->
                                                SwipeableTaskCard(
                                                    task = task,
                                                    onToggle = { viewModel.toggleDone(task) },
                                                    onEdit = { onEditTask(task.id) },
                                                    onDelete = { viewModel.deleteTask(task.id) },
                                                    onClick = { selectedTaskForDetails = task }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            TaskSubTab.HABITS -> {
                                val habits = tasks.filter { it.type == TaskType.HABIT }
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (habits.isEmpty()) {
                                        item {
                                            EmptyState("Chưa có nhiệm vụ dài hạn nào. Thiết lập nhiệm vụ dài hạn để thắp lửa streak ngay! 🔥")
                                        }
                                    } else {
                                        itemsIndexed(habits, key = { _, task -> task.id }) { index, habit ->
                                            HabitCard(
                                                habit = habit,
                                                weeklyLogs = weeklyLogs,
                                                onToggle = { viewModel.toggleDone(habit) },
                                                onEdit = { onEditTask(habit.id) },
                                                onDelete = { viewModel.deleteTask(habit.id) },
                                                onClick = { selectedTaskForDetails = habit }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    HomeTab.DASHBOARD -> {
                        DashboardTabContent(
                            tasks = tasks,
                            userStats = userStats,
                            taskLogs = weeklyLogs
                        )
                    }
                }
            }

            // ── Floating Pill Bottom Navigation Bar — Stitch style ──
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                    .shadow(12.dp, FloatingNavShape)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
                        FloatingNavShape
                    )
                    .border(
                        BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.25f)),
                        FloatingNavShape
                    )
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(FloatingNavShape)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    NavigationTabItem(
                        selected = activeTab == HomeTab.TASKS,
                        onClick = { activeTab = HomeTab.TASKS },
                        icon = Icons.Rounded.CheckCircle,
                        label = "Nhiệm vụ",
                        modifier = Modifier.weight(1f)
                    )
                    NavigationTabItem(
                        selected = activeTab == HomeTab.DASHBOARD,
                        onClick = { activeTab = HomeTab.DASHBOARD },
                        icon = Icons.Rounded.BarChart,
                        label = "Thống kê",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (selectedTaskForDetails != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedTaskForDetails = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                TaskDetailsContent(
                    task = selectedTaskForDetails!!,
                    onEdit = { 
                        selectedTaskForDetails = null
                        onEditTask(it)
                    },
                    onDelete = { 
                        viewModel.deleteTask(it)
                        selectedTaskForDetails = null
                    }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SwipeableTaskCard — wraps TaskCard with swipe-to-delete gesture
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskCard(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {},
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ErrorRose, CardShape)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        content = {
            TaskCard(
                task = task,
                onToggle = onToggle,
                onEdit = onEdit,
                onClick = onClick,
            )
        }
    )
}

@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit = {},
) {
    val cardColor by animateColorAsState(
        targetValue = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(400),
        label = "card_color",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isDone) 0.55f else 1f,
        animationSpec = tween(400),
        label = "content_alpha",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (task.isDone) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "check_scale",
    )
    val isOverdue = remember(task) {
        if (task.isDone || task.dueDate.isBlank()) false
        else {
            runCatching {
                LocalDate.parse(task.dueDate).isBefore(LocalDate.now())
            }.getOrDefault(false)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isDone) 0.dp else 1.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(40.dp)
                    .scale(checkScale),
            ) {
                Icon(
                    imageVector = if (task.isDone) Icons.Rounded.CheckCircle
                    else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Toggle done",
                    tint = if (task.isDone) Primary else PrimaryContainer,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        textDecoration = if (task.isDone) TextDecoration.LineThrough
                        else TextDecoration.None
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                if (task.dueDate.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    DueDateChip(
                        dueDate = task.dueDate,
                        dueTime = task.dueTime,
                        isOverdue = isOverdue,
                        isDone = task.isDone,
                        alarmSet = task.alarmSet,
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  HabitCard — Gorgeous Habit layout with weekly status dots
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: Task,
    weeklyLogs: List<com.example.todoapp.domain.model.TaskLog>,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    val currentWeekDays = remember {
        val today = LocalDate.now()
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        (0..6).map { monday.plusDays(it.toLong()) }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ErrorRose, CardShape)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onClick() },
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox
                        IconButton(
                            onClick = onToggle,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (habit.isDone) Icons.Rounded.CheckCircle
                                              else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (habit.isDone) SuccessGreen else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (habit.description.isNotBlank()) {
                                Text(
                                    text = habit.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Streak Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("🔥", fontSize = 16.sp)
                            Text(
                                text = "${habit.streak}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFF6B4E)
                            )
                        }

                        IconButton(onClick = onEdit) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Weekly progress tracker (7 dots representing Mon-Sun)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lịch tuần này:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentWeekDays.forEach { date ->
                                val isCompleted = weeklyLogs.any { it.taskId == habit.id && it.completedDate == date.toString() }
                                val isToday = date == LocalDate.now()

                                val dayInit = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, Locale.getDefault())

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = if (isCompleted) Mint500 
                                                    else if (isToday) Mint100 
                                                    else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = PillShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayInit,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) Color.White 
                                                else if (isToday) Mint500 
                                                else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DueDateChip(
    dueDate: String,
    dueTime: String,
    isOverdue: Boolean,
    isDone: Boolean,
    alarmSet: Boolean,
) {
    val bg = when {
        isDone    -> MaterialTheme.colorScheme.surfaceVariant
        isOverdue -> MaterialTheme.colorScheme.errorContainer
        else      -> MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = when {
        isDone    -> MaterialTheme.colorScheme.onSurfaceVariant
        isOverdue -> MaterialTheme.colorScheme.error
        else      -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val label = buildString {
        if (dueDate.isNotBlank()) append(formatDate(dueDate))
        if (dueTime.isNotBlank()) append("  ·  $dueTime")
    }

    Row(
        modifier = Modifier
            .background(bg, PillShape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (alarmSet && !isDone) {
            Icon(
                Icons.Rounded.Notifications,
                contentDescription = "Alarm set",
                tint = textColor,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

@Composable
private fun SectionHeader(emoji: String, title: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Chào buổi sáng! ☀️"
        hour < 17 -> "Chào buổi chiều! 🌤️"
        else -> "Chào buổi tối! 🌙"
    }
}

private fun formatDate(dateStr: String): String = runCatching {
    val date = LocalDate.parse(dateStr)
    val today = LocalDate.now()
    when (date) {
        today -> "Hôm nay"
        today.plusDays(1) -> "Ngày mai"
        today.minusDays(1) -> "Hôm qua"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
}.getOrDefault(dateStr)

@Composable
private fun NavigationTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) SecondaryContainer else Color.Transparent,
        animationSpec = tween(300),
        label = "nav_item_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) OnSecondaryContainer else OnSurfaceVariant,
        animationSpec = tween(300),
        label = "nav_item_content"
    )

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            AnimatedVisibility(visible = selected) {
                Row {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDetailsContent(
    task: Task,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (task.description.isNotBlank()) {
            Text(text = task.description, style = MaterialTheme.typography.bodyLarge)
        }
        HorizontalDivider()
        
        // Due Date / Time
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (task.dueDate.isNotBlank()) {
                ChipInfo(emoji = "📅", text = formatDate(task.dueDate))
            }
            if (task.type == TaskType.HABIT) {
                if (task.frequencyType == com.example.todoapp.domain.model.FrequencyType.FIXED && task.fixedDays.isNotEmpty()) {
                    val daysMap = mapOf(1 to "T2", 2 to "T3", 3 to "T4", 4 to "T5", 5 to "T6", 6 to "T7", 7 to "CN")
                    val daysStr = task.fixedDays.sorted().joinToString(", ") { daysMap[it] ?: "" }
                    ChipInfo(emoji = "🔄", text = daysStr)
                } else if (task.frequencyType == com.example.todoapp.domain.model.FrequencyType.FLEXIBLE) {
                    val intervalStr = if (task.flexibleInterval == com.example.todoapp.domain.model.FlexibleInterval.WEEK) "tuần" else "tháng"
                    ChipInfo(emoji = "🎯", text = "${task.flexibleCount} lần/$intervalStr")
                }
            }
            if (task.dueTime.isNotBlank()) {
                ChipInfo(emoji = "⏰", text = task.dueTime)
            }
        }
        
        if (task.type == TaskType.HABIT) {
            Text(text = "🔥 Chuỗi hiện tại: ${task.streak} ngày", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFF6B4E))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { onDelete(task.id) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRose)
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Xóa")
            }
            Button(
                onClick = { onEdit(task.id) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Chỉnh sửa")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ChipInfo(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .background(SecondaryContainer, PillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji)
        Text(text, style = MaterialTheme.typography.labelMedium, color = Secondary)
    }
}
