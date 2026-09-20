package com.evmcstudios.joblerio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evmcstudios.joblerio.data.AlertManager
import com.evmcstudios.joblerio.data.JobAlert
import com.evmcstudios.joblerio.ui.theme.PrimaryBlue
import com.evmcstudios.joblerio.ui.theme.TextGray
import com.evmcstudios.joblerio.ui.theme.TitleDark

@Composable
fun AlertSetupDialog(
    currentQuery: String,
    currentLocation: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf(currentQuery) }
    var location by remember { mutableStateOf(currentLocation) }
    var frequency by remember { mutableStateOf("daily") }
    val existingAlerts = remember { AlertManager.getAlerts(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Job Alerts",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TitleDark
            )
        },
        text = {
            Column {
                Text(
                    text = "Get notified when new jobs match your search",
                    fontSize = 14.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Search Term",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("e.g. CDL driver", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                        focusedBorderColor = PrimaryBlue
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("e.g. New York, NY", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                        focusedBorderColor = PrimaryBlue
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Frequency",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrequencyChip("Daily", frequency == "daily") { frequency = "daily" }
                    FrequencyChip("Weekly", frequency == "weekly") { frequency = "weekly" }
                }

                if (existingAlerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Alerts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    existingAlerts.take(3).forEach { alert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (alert.isActive) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (alert.isActive) PrimaryBlue else TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.query,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TitleDark
                                )
                                Text(
                                    text = "${alert.location.ifBlank { "All" }} - ${alert.frequency}",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            IconButton(
                                onClick = {
                                    AlertManager.toggleAlert(context, alert.id)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (alert.isActive) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                    contentDescription = "Toggle",
                                    tint = if (alert.isActive) PrimaryBlue else TextGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    AlertManager.removeAlert(context, alert.id)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (query.isNotBlank()) {
                        AlertManager.saveAlert(
                            context,
                            JobAlert(
                                query = query,
                                location = location,
                                frequency = frequency
                            )
                        )
                        com.evmcstudios.joblerio.NotificationHelper.createNotificationChannel(context)
                        com.evmcstudios.joblerio.NotificationHelper.schedulePeriodicCheck(context)
                    }
                    onDismiss()
                }
            ) {
                Text("Save Alert", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
fun FrequencyChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) PrimaryBlue else Color.Transparent
    val textColor = if (selected) Color.White else TitleDark

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}
