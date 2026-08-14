package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.GeometricBg
import com.example.ui.theme.GeometricBorder
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BottomNavBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GeometricBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(GeometricSurface)
                .border(1.dp, GeometricBorder, RoundedCornerShape(30.dp))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                title = "HOME",
                icon = Icons.Default.Psychology,
                isSelected = selectedTab == AppTab.HOME_ARC_CORE,
                testTag = "nav_tab_home",
                onClick = { onTabSelected(AppTab.HOME_ARC_CORE) }
            )

            NavItem(
                title = "TASKS",
                icon = Icons.Default.Task,
                isSelected = selectedTab == AppTab.TASKS_MATRIX,
                testTag = "nav_tab_tasks",
                onClick = { onTabSelected(AppTab.TASKS_MATRIX) }
            )

            NavItem(
                title = "AUTOMATION",
                icon = Icons.Default.HomeWork,
                isSelected = selectedTab == AppTab.SMART_HOME_SECURITY,
                testTag = "nav_tab_automation",
                onClick = { onTabSelected(AppTab.SMART_HOME_SECURITY) }
            )

            NavItem(
                title = "SOCIAL",
                icon = Icons.Default.Language,
                isSelected = selectedTab == AppTab.SOCIAL_SCROLLER,
                testTag = "nav_tab_social",
                onClick = { onTabSelected(AppTab.SOCIAL_SCROLLER) }
            )
        }
    }
}

@Composable
private fun NavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .testTag(testTag)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(IceBluePrimary)
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) IceBluePrimary else TextSecondary,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = title,
            color = if (isSelected) IceBluePrimary else TextSecondary,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}
