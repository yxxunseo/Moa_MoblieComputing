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
            .shadow(elevation = 8.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        // 홈
        MoaBottomNavItem(
            title = "홈",
            icon = Icons.Default.Home,
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )

        // 캘린더
        MoaBottomNavItem(
            title = "캘린더",
            icon = Icons.Default.DateRange, // 시안의 그리드 모양 아이콘으로 추후 교체 가능
            selected = currentRoute == "calendar",
            onClick = { onNavigate("calendar") }
        )

        // 그룹
        MoaBottomNavItem(
            title = "그룹",
            icon = Icons.Default.Person, // 시안의 사람 2명 아이콘으로 추후 교체 가능
            selected = currentRoute == "group",
            onClick = { onNavigate("group") }
        )

        // 마이
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
                        .background(Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            },
            label = {
                Text(
                    text = "마이",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2179FE),
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color(0xFF2179FE),
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent // 선택되었을 때 아이콘 뒤 배경색 투명하게
            )
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
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF2179FE),
            unselectedIconColor = Color.Gray,
            selectedTextColor = Color(0xFF2179FE),
            unselectedTextColor = Color.Gray,
            indicatorColor = Color.Transparent
        )
    )
}
