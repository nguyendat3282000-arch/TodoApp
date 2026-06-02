package com.example.todoapp.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.domain.model.Task
import com.example.todoapp.presentation.task.TaskViewModel
import com.example.todoapp.ui.theme.CardShape
import com.example.todoapp.ui.theme.Coral100
import com.example.todoapp.ui.theme.Coral500
import com.example.todoapp.ui.theme.ErrorRose
import com.example.todoapp.ui.theme.Lemon100
import com.example.todoapp.ui.theme.Mint100
import com.example.todoapp.ui.theme.Mint500
import com.example.todoapp.ui.theme.Neutral200
import com.example.todoapp.ui.theme.PillShape
import com.example.todoapp.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JtStyle
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════════════
//  HomeScreen
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    val tasks   by viewModel.tasks.collectAsState()
    val pending  = tasks.filter { !it.isDone }
    val done     = tasks.filter { it.isDone }
    val today    = LocalDate.now()
    val greeting = greeting()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text  = greeting,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text  = today.format(
                                DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.Rounded.Logout,
                            contentDescription = "Sign out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick            = onAddTask,
                shape              = PillShape,
                containerColor     = Mint500,
                contentColor       = Color.White,
                elevation          = FloatingActionButtonDefaults.elevation(8.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add task", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        if (tasks.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding      = PaddingValues(
                    start  = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {

                // ── Section: Pending ──────────────────────────────────────────
                if (pending.isNotEmpty()) {
                    item {
                        SectionHeader(
                            emoji = "🔥",
                            title = "Pending  (${pending.size})",
                        )
                    }
                    itemsIndexed(
                        items = pending,
                        key   = { _, task -> task.id },
                    ) { index, task ->
                        AnimatedVisibility(
                            visible = true,
                            enter   = fadeIn(tween(200, delayMillis = index * 40)) +
                                      slideInVertically(tween(250, delayMillis = index * 40)) { it / 2 },
                        ) {
                            SwipeableTaskCard(
                                task      = task,
                                onToggle  = { viewModel.toggleDone(task) },
                                onEdit    = { onEditTask(task.id) },
                                onDelete  = { viewModel.deleteTask(task.id) },
                            )
                        }
                    }
                }

                // ── Section: Completed ────────────────────────────────────────
                if (done.isNotEmpty()) {
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        SectionHeader(
                            emoji = "✅",
                            title = "Completed  (${done.size})",
                        )
                    }
                    itemsIndexed(
                        items = done,
                        key   = { _, task -> task.id },
                    ) { index, task ->
                        SwipeableTaskCard(
                            task      = task,
                            onToggle  = { viewModel.toggleDone(task) },
                            onEdit    = { onEditTask(task.id) },
                            onDelete  = { viewModel.deleteTask(task.id) },
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
        state             = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Red delete reveal
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
                    tint   = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        content = {
            TaskCard(
                task     = task,
                onToggle = onToggle,
                onEdit   = onEdit,
            )
        }
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  TaskCard
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    // ── Animated state variables ──────────────────────────────────────────────
    val cardColor by animateColorAsState(
        targetValue = if (task.isDone) Lemon100 else MaterialTheme.colorScheme.surface,
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
            stiffness    = Spring.StiffnessMedium,
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
        shape    = CardShape,
        colors   = CardDefaults.cardColors(containerColor = cardColor),
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
            // ── Checkmark toggle ──────────────────────────────────────────────
            IconButton(
                onClick  = onToggle,
                modifier = Modifier
                    .size(40.dp)
                    .scale(checkScale),
            ) {
                Icon(
                    imageVector = if (task.isDone) Icons.Rounded.CheckCircle
                                  else             Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Toggle done",
                    tint   = if (task.isDone) SuccessGreen else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.width(10.dp))

            // ── Task details ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha),
            ) {
                Text(
                    text  = task.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        textDecoration = if (task.isDone) TextDecoration.LineThrough
                                         else             TextDecoration.None
                    ),
                    maxLines  = 2,
                    overflow  = TextOverflow.Ellipsis,
                    color     = MaterialTheme.colorScheme.onSurface,
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text      = task.description,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                        modifier  = Modifier.padding(top = 2.dp),
                    )
                }
                if (task.dueDate.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    DueDateChip(
                        dueDate  = task.dueDate,
                        dueTime  = task.dueTime,
                        isOverdue = isOverdue,
                        isDone    = task.isDone,
                        alarmSet  = task.alarmSet,
                    )
                }
            }

            // ── Edit button ───────────────────────────────────────────────────
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit task",
                    tint   = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  DueDateChip
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun DueDateChip(
    dueDate: String,
    dueTime: String,
    isOverdue: Boolean,
    isDone: Boolean,
    alarmSet: Boolean,
) {
    val bg = when {
        isDone    -> Neutral200
        isOverdue -> Coral100
        else      -> Mint100
    }
    val textColor = when {
        isDone    -> MaterialTheme.colorScheme.onSurfaceVariant
        isOverdue -> Coral500
        else      -> Mint500
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
                tint     = textColor,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SectionHeader
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(emoji: String, title: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(
            text  = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  EmptyState
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier.fillMaxSize(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center,
    ) {
        Text("🎉", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "All clear!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text  = "Tap + to add your first task",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Helpers
// ══════════════════════════════════════════════════════════════════════════════

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning ☀️"
        hour < 17 -> "Good afternoon 🌤️"
        else      -> "Good evening 🌙"
    }
}

private fun formatDate(dateStr: String): String = runCatching {
    val date  = LocalDate.parse(dateStr)
    val today = LocalDate.now()
    when (date) {
        today            -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
}.getOrDefault(dateStr)
