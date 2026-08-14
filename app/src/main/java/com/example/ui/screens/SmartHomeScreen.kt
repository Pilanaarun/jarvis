package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SmartDeviceEntity
import com.example.ui.components.HUDCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DeepBlueContainer
import com.example.ui.theme.GeometricBorder
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SmartHomeScreen(
    devices: List<SmartDeviceEntity>,
    securityStatus: String,
    onToggleDevice: (SmartDeviceEntity) -> Unit,
    onTriggerPatrol: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security Monitoring HUD
        item {
            HUDCard(
                title = "Surveillance & Security Monitoring",
                icon = Icons.Default.Shield,
                statusColor = if (securityStatus == "SECURED") StatusGreen else AlertRed,
                badgeText = securityStatus
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERIMETER CCTVS ONLINE",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = onTriggerPatrol,
                            modifier = Modifier
                                .testTag("trigger_patrol_btn")
                                .height(32.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepBlueContainer,
                                contentColor = IceBluePrimary
                            )
                        ) {
                            Text(
                                text = "PATROL SCAN",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Camera Feeds Grid Simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecurityCameraView(
                            title = "CAM-01: Main Entrance",
                            modifier = Modifier.weight(1f)
                        )
                        SecurityCameraView(
                            title = "CAM-02: Garage Bay",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Smart Home Controls Header
        item {
            HUDCard(
                title = "Smart Home Automation Node",
                icon = Icons.Default.HomeWork,
                badgeText = "${devices.count { it.isOn }} / ${devices.size} Active"
            ) {
                Text(
                    text = "Control lights, climate, door locks, and security sensors directly or via voice commands.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Grid of Smart Devices
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    devices.forEach { device ->
                        SmartDeviceCard(
                            device = device,
                            onToggle = { onToggleDevice(device) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityCameraView(
    title: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarScan")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanLine"
    )

    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070E1A))
            .border(1.dp, GeometricBorder, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width

            // Simulated Radar Grid Lines
            drawLine(
                color = GeometricBorder,
                start = Offset(0f, h / 2),
                end = Offset(w, h / 2),
                strokeWidth = 1f
            )
            drawLine(
                color = GeometricBorder,
                start = Offset(w / 2, 0f),
                end = Offset(w / 2, h),
                strokeWidth = 1f
            )

            // Scanning Line Beam
            val currentY = scanY * h
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, IceBluePrimary.copy(alpha = 0.6f))
                ),
                start = Offset(0f, currentY),
                end = Offset(w, currentY),
                strokeWidth = 3f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AlertRed.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AlertRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE HD",
                            color = AlertRed,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "1080P",
                    color = IceBluePrimary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SmartDeviceCard(
    device: SmartDeviceEntity,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}")
            .clip(RoundedCornerShape(12.dp))
            .background(GeometricSurface)
            .border(
                1.dp,
                if (device.isOn) IceBluePrimary.copy(alpha = 0.4f) else GeometricBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (device.isOn) DeepBlueContainer else GeometricBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (device.type.uppercase()) {
                        "LIGHT" -> Icons.Default.Lightbulb
                        "THERMOSTAT" -> Icons.Default.AcUnit
                        "LOCK" -> if (device.isOn) Icons.Default.Lock else Icons.Default.LockOpen
                        else -> Icons.Default.Videocam
                    },
                    contentDescription = null,
                    tint = if (device.isOn) IceBluePrimary else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${device.location} • ${device.statusDetail}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Switch(
                checked = device.isOn,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("switch_device_${device.id}"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = IceBluePrimary,
                    checkedTrackColor = DeepBlueContainer,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = GeometricBorder
                )
            )
        }
    }
}
