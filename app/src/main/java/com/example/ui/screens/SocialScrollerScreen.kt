package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SocialPlatform
import com.example.ui.SocialScrollState
import com.example.ui.components.HUDCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DeepBlueContainer
import com.example.ui.theme.GeometricBorder
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SocialScrollerScreen(
    state: SocialScrollState,
    onPlatformSelected: (SocialPlatform) -> Unit,
    onSpeedSelected: (Int) -> Unit,
    onNextItem: () -> Unit,
    onPrevItem: () -> Unit,
    onToggleAutoScroll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Social Media Controller Header
        item {
            HUDCard(
                title = "Hands-Free Social Media Scroller",
                icon = Icons.Default.Smartphone,
                badgeText = if (state.isAutoScrolling) "AUTO-SCROLLING" else "PAUSED"
            ) {
                // Voice prompt tip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GeometricBorder)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = IceBluePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Voice Command: \"Scroll Instagram\" or \"Next Video\" or \"Start Auto Scroll\"",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Platform Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SocialPlatform.values().forEach { platform ->
                        val isSelected = platform == state.platform
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("platform_select_${platform.name}")
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) DeepBlueContainer else GeometricSurface)
                                .border(1.dp, if (isSelected) IceBluePrimary else GeometricBorder, RoundedCornerShape(10.dp))
                                .clickable { onPlatformSelected(platform) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = platform.displayName,
                                color = if (isSelected) IceBluePrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto Scroll Speed Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "INTERVAL TIMING",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 5, 8, 10).forEach { speed ->
                            val isSelected = speed == state.scrollSpeedSec
                            Box(
                                modifier = Modifier
                                    .testTag("speed_chip_$speed")
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IceBluePrimary else GeometricSurface)
                                    .clickable { onSpeedSelected(speed) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${speed}s",
                                    color = if (isSelected) Color(0xFF003355) else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Feed Video Frame Simulation
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0B1220))
                    .border(1.dp, GeometricBorder, RoundedCornerShape(16.dp))
            ) {
                // Background Gradient Simulation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = when (state.platform) {
                                    SocialPlatform.INSTAGRAM -> listOf(
                                        Color(0xFF833AB4).copy(alpha = 0.3f),
                                        Color(0xFFFD1D1D).copy(alpha = 0.2f),
                                        Color(0xFFFCB045).copy(alpha = 0.3f)
                                    )
                                    SocialPlatform.YOUTUBE -> listOf(
                                        AlertRed.copy(alpha = 0.4f),
                                        Color(0xFF280000).copy(alpha = 0.6f)
                                    )
                                    else -> listOf(
                                        DeepBlueContainer.copy(alpha = 0.5f),
                                        Color(0xFF0B1220)
                                    )
                                }
                            )
                        )
                )

                // Animated Reel Content Card
                AnimatedContent(
                    targetState = state.currentItemIndex,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut()
                    },
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Feed Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GeometricSurface.copy(alpha = 0.8f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${state.platform.displayName} • Reel #$index",
                                    color = IceBluePrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (state.isAutoScrolling) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(StatusGreen)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "AUTO ON",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Video Title / Description preview
                        Column {
                            Text(
                                text = when (index % 4) {
                                    1 -> "🚀 AI & Cybernetics Automation Trends 2026"
                                    2 -> "🤖 JARVIS Smart Home Voice Control Setup"
                                    3 -> "💻 Jetpack Compose Android Development Tips"
                                    else -> "🔥 Top 10 Futuristic Tech Gadgets Review"
                                },
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "@jarvis_ai_official • Original Audio",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Overlay Controls (Scroll Up / Down buttons)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("scroll_up_btn")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GeometricSurface.copy(alpha = 0.8f))
                            .border(1.dp, GeometricBorder, CircleShape)
                            .clickable { onPrevItem() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Previous Reel",
                            tint = IceBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .testTag("scroll_down_btn")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GeometricSurface.copy(alpha = 0.8f))
                            .border(1.dp, GeometricBorder, CircleShape)
                            .clickable { onNextItem() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Next Reel",
                            tint = IceBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Action Command Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleAutoScroll,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_auto_scroll_btn")
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isAutoScrolling) AlertRed else DeepBlueContainer,
                        contentColor = IceBluePrimary
                    )
                ) {
                    Icon(
                        imageVector = if (state.isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isAutoScrolling) "PAUSE AUTO-SCROLL" else "START AUTO-SCROLL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onNextItem,
                    modifier = Modifier
                        .testTag("next_reel_btn")
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeometricSurface,
                        contentColor = IceBluePrimary
                    )
                ) {
                    Text(
                        text = "NEXT REEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
