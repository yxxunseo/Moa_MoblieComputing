package com.example.moa_project.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaCardShadow
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

private val NavIconSize = 28.dp
private val ProfileIconSize = 30.dp

@Composable
fun MoaBottomNavigationBar(
    currentRoute: String = "home",
    profileImageUrl: String? = null,
    onNavigate: (String) -> Unit = {},
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(elevation = 8.dp, spotColor = MoaCardShadow),
        containerColor = Color.White,
        tonalElevation = 0.dp,
    ) {
        MoaBottomNavItem(
            title = "홈",
            icon = Icons.Rounded.Home,
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
        )
        MoaBottomNavItem(
            title = "캘린더",
            icon = Icons.Default.DateRange,
            selected = currentRoute == "calendar",
            onClick = { onNavigate("calendar") },
        )
        MoaBottomNavItem(
            title = "모임",
            icon = Icons.Default.Groups,
            selected = currentRoute == "meetings",
            onClick = { onNavigate("meetings") },
        )
        NavigationBarItem(
            selected = currentRoute == "my",
            onClick = { onNavigate("my") },
            modifier = Modifier.widthIn(min = 64.dp),
            icon = {
                ProfileAvatar(
                    imageUrl = profileImageUrl,
                    size = ProfileIconSize,
                )
            },
            label = {
                Text(
                    text = "마이",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = if (currentRoute == "my") FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                )
            },
            colors = moaNavColors(currentRoute == "my"),
        )
    }
}

@Composable
private fun RowScope.MoaBottomNavItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.widthIn(min = 64.dp),
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(NavIconSize),
            )
        },
        label = {
            Text(
                text = title,
                fontFamily = SBAggroFontFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
            )
        },
        colors = moaNavColors(selected),
    )
}

@Composable
private fun moaNavColors(selected: Boolean) = NavigationBarItemDefaults.colors(
    selectedIconColor = MoaBlue,
    unselectedIconColor = MoaTextSecondary,
    selectedTextColor = MoaBlue,
    unselectedTextColor = MoaTextSecondary,
    indicatorColor = Color.Transparent,
)
