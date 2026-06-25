// File: ui/screens/home/DashboardTab.kt
package com.example.todoapp.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.domain.model.FrequencyType
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskType
import com.example.todoapp.domain.model.UserStats
import com.example.todoapp.domain.model.TaskLog
import com.example.todoapp.ui.theme.Primary
import com.example.todoapp.ui.theme.Secondary
import com.example.todoapp.ui.theme.SecondaryContainer
import com.example.todoapp.ui.theme.SurfaceContainerLowest
import com.example.todoapp.ui.theme.SuccessGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class PeriodFilter {
    DAY,
    WEEK,
    MONTH
}

@Composable
fun DashboardTabContent(
    tasks: List<Task>,
    userStats: UserStats?,
    taskLogs: List<TaskLog>,
    modifier: Modifier = Modifier
) {
    var periodFilter by remember { mutableStateOf(PeriodFilter.WEEK) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val score = userStats?.healthScore ?: 100
    val streak = userStats?.totalStreak ?: 0

    // Calendar Days (Monday to Sunday of selected week)
    val calendarDates = remember(selectedDate) {
        val monday = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
        (0..6).map { monday.plusDays(it.toLong()) }
    }

    val today = LocalDate.now()
    val startOfWeek = remember(today) { today.minusDays((today.dayOfWeek.value - 1).toLong()) }
    val endOfWeek = remember(startOfWeek) { startOfWeek.plusDays(6) }

    val startOfMonth = remember(today) { today.withDayOfMonth(1) }
    val endOfMonth = remember(today) { today.plusMonths(1).withDayOfMonth(1).minusDays(1) }

    // Compute weekly completed/pending lists
    val weeklyTasks = remember(tasks, taskLogs, startOfWeek, endOfWeek) {
        val dailyCompleted = tasks.filter { 
            it.type == TaskType.DAILY && 
            it.isDone && 
            runCatching { LocalDate.parse(it.dueDate) }.getOrNull()?.let { d -> d in startOfWeek..endOfWeek } == true 
        }
        val dailyPending = tasks.filter { 
            it.type == TaskType.DAILY && 
            !it.isDone && 
            runCatching { LocalDate.parse(it.dueDate) }.getOrNull()?.let { d -> d in startOfWeek..endOfWeek } == true 
        }

        val habitCompleted = tasks.filter { task ->
            task.type == TaskType.HABIT &&
            taskLogs.any { log -> 
                log.taskId == task.id && 
                runCatching { LocalDate.parse(log.completedDate) }.getOrNull()?.let { d -> d in startOfWeek..endOfWeek } == true 
            }
        }
        val habitPending = tasks.filter { task ->
            task.type == TaskType.HABIT &&
            taskLogs.none { log -> 
                log.taskId == task.id && 
                runCatching { LocalDate.parse(log.completedDate) }.getOrNull()?.let { d -> d in startOfWeek..endOfWeek } == true 
            }
        }

        Pair(dailyCompleted + habitCompleted, dailyPending + habitPending)
    }

    val weeklyCompleted = weeklyTasks.first
    val weeklyPending = weeklyTasks.second

    // Compute monthly completed/pending lists
    val monthlyTasks = remember(tasks, startOfMonth, endOfMonth) {
        val dailyCompleted = tasks.filter { 
            it.type == TaskType.DAILY && 
            it.isDone && 
            runCatching { LocalDate.parse(it.dueDate) }.getOrNull()?.let { d -> d in startOfMonth..endOfMonth } == true 
        }
        val dailyPending = tasks.filter { 
            it.type == TaskType.DAILY && 
            !it.isDone && 
            runCatching { LocalDate.parse(it.dueDate) }.getOrNull()?.let { d -> d in startOfMonth..endOfMonth } == true 
        }

        val habitCompleted = tasks.filter { 
            it.type == TaskType.HABIT && 
            runCatching { LocalDate.parse(it.lastCompletedDate) }.getOrNull()?.let { d -> d in startOfMonth..endOfMonth } == true 
        }
        val habitPending = tasks.filter { 
            it.type == TaskType.HABIT && 
            runCatching { LocalDate.parse(it.lastCompletedDate) }.getOrNull()?.let { d -> d in startOfMonth..endOfMonth } != true 
        }

        Pair(dailyCompleted + habitCompleted, dailyPending + habitPending)
    }

    val monthlyCompleted = monthlyTasks.first
    val monthlyPending = monthlyTasks.second

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Tab Row Filter Period ---
        TabRow(
            selectedTabIndex = periodFilter.ordinal,
            containerColor = Color.Transparent,
            contentColor = Primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[periodFilter.ordinal]),
                    color = Primary
                )
            }
        ) {
            Tab(
                selected = periodFilter == PeriodFilter.DAY,
                onClick = { periodFilter = PeriodFilter.DAY },
                text = { Text("Theo Ngày", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = periodFilter == PeriodFilter.WEEK,
                onClick = { periodFilter = PeriodFilter.WEEK },
                text = { Text("Theo Tuần", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = periodFilter == PeriodFilter.MONTH,
                onClick = { periodFilter = PeriodFilter.MONTH },
                text = { Text("Theo Tháng", fontWeight = FontWeight.Bold) }
            )
        }

        when (periodFilter) {
            PeriodFilter.DAY -> {
                // --- CALENDAR WIDGET ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.getDefault())).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            calendarDates.forEach { date ->
                                val isSelected = date == selectedDate
                                val dayOfWeekStr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                val isDone = taskLogs.any { it.completedDate == date.toString() }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                        .clickable { selectedDate = date },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Primary else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = dayOfWeekStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else Color.Gray
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "${date.dayOfMonth}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        // Green dot if something was completed
                                        if (isDone) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        if (isSelected) Color.White else SuccessGreen,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            )
                                        } else {
                                            Spacer(Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- TASKS LIST FOR THE SELECTED DAY ---
                val dayTasks = remember(tasks, selectedDate, taskLogs) {
                    val dailyForDay = tasks.filter { it.type == TaskType.DAILY && it.dueDate == selectedDate.toString() }
                    val logsForDay = taskLogs.filter { it.completedDate == selectedDate.toString() }
                    val completedHabitIds = logsForDay.map { it.taskId }.toSet()
                    
                    // Habits completed on this day
                    val completedHabits = tasks.filter { it.type == TaskType.HABIT && it.id in completedHabitIds }
                    
                    // Pending Fixed Habits for this day
                    val pendingHabits = tasks.filter { task ->
                        task.type == TaskType.HABIT && 
                        task.frequencyType == FrequencyType.FIXED &&
                        task.fixedDays.contains(selectedDate.dayOfWeek.value) &&
                        task.id !in completedHabitIds
                    }
                    
                    // Daily completed/pending + Habits completed
                    val done = dailyForDay.filter { it.isDone } + completedHabits.map { it.copy(isDone = true) }
                    val pending = dailyForDay.filter { !it.isDone } + pendingHabits.map { it.copy(isDone = false) }
                    
                    Pair(done, pending)
                }

                val completedList = dayTasks.first
                val pendingList = dayTasks.second

                Text(
                    text = "Nhiệm vụ ngày ${selectedDate.format(DateTimeFormatter.ofPattern("d/M", Locale.getDefault()))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

                if (completedList.isEmpty() && pendingList.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Không có nhiệm vụ nào trong ngày này ☕", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                } else {
                    if (pendingList.isNotEmpty()) {
                        Text("Chưa hoàn thành (${pendingList.size})", style = MaterialTheme.typography.titleSmall, color = Color.Red.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.Start))
                        pendingList.forEach { task ->
                            TaskSimpleRow(task = task, isDone = false)
                        }
                    }

                    if (completedList.isNotEmpty()) {
                        Text("Đã hoàn thành (${completedList.size})", style = MaterialTheme.typography.titleSmall, color = SuccessGreen, modifier = Modifier.align(Alignment.Start))
                        completedList.forEach { task ->
                            TaskSimpleRow(task = task, isDone = true)
                        }
                    }
                }
            }

            PeriodFilter.WEEK -> {
                // --- Weekly Stats Circle ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Health Score Tuần",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                            CircularProgressIndicator(
                                progress = { score / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = Primary,
                                strokeWidth = 12.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(text = "Chuỗi: $streak🔥", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFF6B4E))
                            }
                        }
                    }
                }

                // --- Canvas Bar Chart ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Thống kê hoàn thành tuần này",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))

                        val last7Days = remember(taskLogs) {
                            (0..6).map { i ->
                                val date = LocalDate.now().minusDays((6 - i).toLong())
                                val count = taskLogs.count { it.completedDate == date.toString() }
                                date to count
                            }
                        }

                        val maxCount = maxOf(1, last7Days.maxOf { it.second })
                        val chartColor = Primary
                        val textMeasurer = rememberTextMeasurer()

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val barWidth = 20.dp.toPx()
                            val padding = 10.dp.toPx()
                            val bottomTextSpace = 24.dp.toPx()
                            val chartHeight = canvasHeight - bottomTextSpace
                            val stepX = (canvasWidth - padding * 2) / 7

                            last7Days.forEachIndexed { index, (date, count) ->
                                val x = padding + index * stepX + (stepX - barWidth) / 2
                                val barHeight = (count.toFloat() / maxCount) * (chartHeight * 0.8f)
                                val y = chartHeight - barHeight

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(chartColor, chartColor.copy(alpha = 0.5f))
                                    ),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, maxOf(4.dp.toPx(), barHeight)),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )

                                val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                val textLayoutResult = textMeasurer.measure(
                                    text = dayLabel,
                                    style = androidx.compose.ui.text.TextStyle(color = Color.Gray, fontSize = 10.sp)
                                )
                                drawText(
                                    textLayoutResult = textLayoutResult,
                                    topLeft = Offset(
                                        x + (barWidth - textLayoutResult.size.width) / 2,
                                        chartHeight + 4.dp.toPx()
                                    )
                                )

                                if (count > 0) {
                                    val countLayoutResult = textMeasurer.measure(
                                        text = "$count",
                                        style = androidx.compose.ui.text.TextStyle(color = chartColor, fontSize = 11.sp)
                                    )
                                    drawText(
                                        textLayoutResult = countLayoutResult,
                                        topLeft = Offset(
                                            x + (barWidth - countLayoutResult.size.width) / 2,
                                            y - countLayoutResult.size.height - 2.dp.toPx()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Nhiệm vụ tuần này",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

                if (weeklyCompleted.isEmpty() && weeklyPending.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Không có nhiệm vụ nào trong tuần này ☕", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                } else {
                    if (weeklyPending.isNotEmpty()) {
                        Text("Chưa hoàn thành (${weeklyPending.size})", style = MaterialTheme.typography.titleSmall, color = Color.Red.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.Start))
                        weeklyPending.forEach { task ->
                            TaskSimpleRow(task = task, isDone = false)
                        }
                    }

                    if (weeklyCompleted.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Đã hoàn thành (${weeklyCompleted.size})", style = MaterialTheme.typography.titleSmall, color = SuccessGreen, modifier = Modifier.align(Alignment.Start))
                        weeklyCompleted.forEach { task ->
                            TaskSimpleRow(task = task, isDone = true)
                        }
                    }
                }
            }

            PeriodFilter.MONTH -> {
                // --- Monthly Stats Card ---
                val monthlyCompletedCount = remember(taskLogs) {
                    val startOfMonth = LocalDate.now().withDayOfMonth(1).toString()
                    val endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1).toString()
                    taskLogs.count { it.completedDate in startOfMonth..endOfMonth }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tiến độ tháng này",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "$monthlyCompletedCount",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nhiệm vụ đã hoàn thành trong tháng",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        // Progress representation (Simple bar representing progress of the current month)
                        val dayOfMonth = LocalDate.now().dayOfMonth
                        val daysInMonth = LocalDate.now().lengthOfMonth()
                        val progress = dayOfMonth.toFloat() / daysInMonth

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Đã đi qua $dayOfMonth/$daysInMonth ngày của tháng",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Nhiệm vụ tháng này",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

                if (monthlyCompleted.isEmpty() && monthlyPending.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Không có nhiệm vụ nào trong tháng này ☕", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                } else {
                    if (monthlyPending.isNotEmpty()) {
                        Text("Chưa hoàn thành (${monthlyPending.size})", style = MaterialTheme.typography.titleSmall, color = Color.Red.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.Start))
                        monthlyPending.forEach { task ->
                            TaskSimpleRow(task = task, isDone = false)
                        }
                    }

                    if (monthlyCompleted.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Đã hoàn thành (${monthlyCompleted.size})", style = MaterialTheme.typography.titleSmall, color = SuccessGreen, modifier = Modifier.align(Alignment.Start))
                        monthlyCompleted.forEach { task ->
                            TaskSimpleRow(task = task, isDone = true)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(60.dp))
    }
}

@Composable
fun TaskSimpleRow(task: Task, isDone: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) SuccessGreen.copy(alpha = 0.05f) else SurfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isDone) SuccessGreen else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
