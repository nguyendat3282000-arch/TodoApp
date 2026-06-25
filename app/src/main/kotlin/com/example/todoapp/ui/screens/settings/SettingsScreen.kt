// File: ui/screens/settings/SettingsScreen.kt
package com.example.todoapp.ui.screens.settings

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.domain.model.UserStats
import com.example.todoapp.notification.MorningNotificationReceiver
import com.example.todoapp.presentation.task.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val prefs = remember { context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) }
    var notificationEnabled by remember {
        mutableStateOf(prefs.getBoolean("morning_notif_enabled", true))
    }
    var notificationHour by remember {
        mutableStateOf(prefs.getInt("morning_hour", 7))
    }
    var notificationMinute by remember {
        mutableStateOf(prefs.getInt("morning_minute", 0))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt hệ thống ⚙️", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Account Section ---
            Text(
                text = "Tài khoản",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Column {
                            Text(
                                text = user?.displayName ?: "User",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = user?.email ?: "email@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Divider()
                    Button(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Đăng xuất")
                    }
                }
            }

            // --- 2. Customizations Section ---
            Text(
                text = "Nhắc nhở & Cá nhân hóa",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow(
                        icon = Icons.Rounded.NotificationAdd,
                        title = "Nhắc nhở câu quote buổi sáng",
                        description = "Gửi châm ngôn động lực đầu ngày",
                        action = {
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { enabled ->
                                    notificationEnabled = enabled
                                    prefs.edit().putBoolean("morning_notif_enabled", enabled).apply()
                                    if (enabled) {
                                        MorningNotificationReceiver.scheduleNotification(context, notificationHour, notificationMinute)
                                    } else {
                                        MorningNotificationReceiver.cancelNotification(context)
                                    }
                                }
                            )
                        }
                    )

                    Divider()

                    SettingsRow(
                        icon = Icons.Rounded.AccessTime,
                        title = "Thời gian gửi thông báo",
                        description = "%02d:%02d".format(notificationHour, notificationMinute),
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    notificationHour = hour
                                    notificationMinute = minute
                                    prefs.edit().putInt("morning_hour", hour).putInt("morning_minute", minute).apply()
                                    if (notificationEnabled) {
                                        MorningNotificationReceiver.scheduleNotification(context, hour, minute)
                                    }
                                },
                                notificationHour,
                                notificationMinute,
                                true
                            ).show()
                        }
                    )
                }
            }

            // --- 3. Data management ---
            Text(
                text = "Dữ liệu & Điểm số",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow(
                        icon = Icons.Rounded.Restore,
                        title = "Reset điểm số sức khỏe",
                        description = "Đặt lại điểm về 100 điểm và làm sạch lịch sử",
                        onClick = {
                            viewModel.resetUserStats()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    val modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        action?.invoke()
    }
}
