package com.example.moa_project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.R
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

@Composable
fun MoaBottomNavigationBar(
    currentRoute: String = "home",
    profileImageResId: Int? = null,
    onNavigate: (String) -> Unit = {}
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, spotColor = Color(0xFFDDE4F2)),
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        MoaBottomNavItem(
            title = "홈",
            icon = Icons.Default.Home,
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        MoaBottomNavItem(
            title = "캘린더",
            icon = Icons.Default.DateRange,
            selected = currentRoute == "calendar",
            onClick = { onNavigate("calendar") }
        )
        MoaBottomNavItem(
            title = "모임",
            icon = Icons.Default.Person,
            selected = currentRoute == "meetings",
            onClick = { onNavigate("meetings") }
        )
        NavigationBarItem(
            selected = currentRoute == "my",
            onClick = { onNavigate("my") },
            icon = {
                val imageRes = profileImageResId ?: R.drawable.ic_character
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "마이 프로필",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MoaTextSecondary.copy(alpha = 0.2f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            },
            label = {
                Text(
                    text = "마이",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            },
            colors = moaNavColors(currentRoute == "my")
        )
    }
}

@Composable
private fun RowScope.MoaBottomNavItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp)
            )
        },
        label = {
            Text(
                text = title,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        },
        colors = moaNavColors(selected)
    )
}

@Composable
private fun moaNavColors(selected: Boolean) = NavigationBarItemDefaults.colors(
    selectedIconColor = MoaBlue,
    unselectedIconColor = MoaTextSecondary,
    selectedTextColor = MoaBlue,
    unselectedTextColor = MoaTextSecondary,
    indicatorColor = Color.Transparent
)
