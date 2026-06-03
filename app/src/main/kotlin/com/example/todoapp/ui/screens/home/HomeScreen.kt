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

    val tasks by viewModel.tasks.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val weeklyLogs by viewModel.weeklyTaskLogs.collectAsState()

    val today = LocalDate.now()
    val greeting = greeting()

    Scaffold(
        topBar = {
            // Standard small transparent TopAppBar - removes large white space at top
            TopAppBar(
                title = {
                    Text(
                        text = "TodoApp ✨",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // Settings button at top right
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Cài đặt",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (activeTab == HomeTab.TASKS) {
                FloatingActionButton(
                    onClick = onAddTask,
                    shape = PillShape,
                    containerColor = Mint500,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier.padding(bottom = 60.dp) // Move up slightly to avoid overlapping floating bar
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
                .padding(innerPadding)
        ) {
            // Main Content Area
            Column(modifier = Modifier.fillMaxSize()) {
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
                                            EmptyState("Chưa có task nào cho hôm nay. Bấm + để thêm mục tiêu! 🎉")
                                        }
                                    } else {
                                        if (pending.isNotEmpty()) {
                                            item { SectionHeader("🔥", "Đang đợi làm (${pending.size})") }
                                            itemsIndexed(pending, key = { _, task -> task.id }) { index, task ->
                                                SwipeableTaskCard(
                                                    task = task,
                                                    onToggle = { viewModel.toggleDone(task) },
                                                    onEdit = { onEditTask(task.id) },
                                                    onDelete = { viewModel.deleteTask(task.id) }
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
                                                    onDelete = { viewModel.deleteTask(task.id) }
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
                                            EmptyState("Chưa có thói quen nào. Thiết lập thói quen để thắp lửa streak ngay! 🔥")
                                        }
                                    } else {
                                        itemsIndexed(habits, key = { _, task -> task.id }) { index, habit ->
                                            HabitCard(
                                                habit = habit,
                                                weeklyLogs = weeklyLogs,
                                                onToggle = { viewModel.toggleDone(habit) },
                                                onEdit = { onEditTask(habit.id) },
                                                onDelete = { viewModel.deleteTask(habit.id) }
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

            // ── Floating Pill Bottom Navigation Bar (More subtle and elegant) ──
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .shadow(12.dp, RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(30.dp))
                    .border(BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)), RoundedCornerShape(30.dp))
                    .width(260.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(30.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val activeColor = Mint500
                    val inactiveColor = Color.Gray

                    // Item 1: Goals
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { activeTab = HomeTab.TASKS },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Goals",
                            tint = if (activeTab == HomeTab.TASKS) activeColor else inactiveColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Mục tiêu",
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == HomeTab.TASKS) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == HomeTab.TASKS) activeColor else inactiveColor
                        )
                    }

                    // Item 2: Dashboard
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { activeTab = HomeTab.DASHBOARD },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = "Dashboard",
                            tint = if (activeTab == HomeTab.DASHBOARD) activeColor else inactiveColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Thống kê",
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == HomeTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == HomeTab.DASHBOARD) activeColor else inactiveColor
                        )
                    }
                }
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
            )
        }
    )
}

@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    val cardColor by animateColorAsState(
        targetValue = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
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
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isDone) 0.dp else 2.dp,
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
                    tint = if (task.isDone) SuccessGreen else MaterialTheme.colorScheme.outline,
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
    onDelete: () -> Unit
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
                modifier = Modifier.fillMaxWidth(),
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
        isDone -> Neutral200
        isOverdue -> Coral100
        else -> Mint100
    }
    val textColor = when {
        isDone -> MaterialTheme.colorScheme.onSurfaceVariant
        isOverdue -> Coral500
        else -> Mint500
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
