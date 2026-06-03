// File: ui/screens/home/SettingsTab.kt
package com.example.todoapp.ui.screens.home

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
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
import com.example.todoapp.domain.model.AuthUser
import com.example.todoapp.notification.MorningNotificationReceiver
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsTabContent(
    onSignOut: () -> Unit,
    onResetStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất (Sign Out)")
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Morning Quote Notification Toggle
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

                // Morning Quote Notification Time Picker
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Rounded.Restore,
                    title = "Reset điểm số sức khỏe",
                    description = "Đặt lại điểm về 100 điểm và làm sạch lịch sử",
                    onClick = onResetStats
                )
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
