package com.example.moa_project.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.R
import com.example.moa_project.ui.theme.SBAggroFontFamily

enum class Moa3DIconType {
    Trophy,
    Chart,
    Clock,
    Link,
    Success,
    Pending,
}

private data class IconStyle(
    val brush: Brush,
    val vector: ImageVector,
    @DrawableRes val drawableRes: Int? = null,
    val tint: Color,
)

@Composable
fun Moa3DIcon(
    type: Moa3DIconType,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    val style = when (type) {
        Moa3DIconType.Trophy -> IconStyle(
            Brush.linearGradient(listOf(Color(0xFFFFE082), Color(0xFFFFB300))),
            Icons.Rounded.EmojiEvents,
            tint = Color(0xFFB8860B),
        )
        Moa3DIconType.Chart -> IconStyle(
            Brush.linearGradient(listOf(Color(0xFFD6E8FF), Color(0xFF8FB8FF))),
            Icons.Rounded.BarChart,
            tint = Color(0xFF1A5FB4),
        )
        Moa3DIconType.Clock -> IconStyle(
            Brush.linearGradient(listOf(Color(0xFFE8ECFF), Color(0xFFB8C4F0))),
            Icons.Rounded.Schedule,
            tint = Color(0xFF4F5D9A),
        )
        Moa3DIconType.Link -> IconStyle(
            Brush.linearGradient(listOf(Color(0xFFD9F5E5), Color(0xFF7DD3A8))),
            Icons.Rounded.Link,
            drawableRes = R.drawable.ic_create,
            tint = Color(0xFF2E8B57),
        )
        Moa3DIconType.Success -> IconStyle(
            Brush.linearGradient(listOf(Color(0xFFD9F5E5), Color(0xFF35A96D))),
            Icons.Rounded.CheckCircle,
            tint = Color(0xFF1B7A45),
        )
        Moa3DIconType.Pending -> IconStyle(
            Brush.linearGradient(listOf(Color(0xFFFFF3D6), Color(0xFFFFD966))),
            Icons.Rounded.Schedule,
            tint = Color(0xFF8A6800),
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape, spotColor = Color(0x22000000))
            .clip(CircleShape)
            .background(style.brush),
        contentAlignment = Alignment.Center,
    ) {
        if (style.drawableRes != null) {
            Image(
                painter = painterResource(style.drawableRes),
                contentDescription = null,
                modifier = Modifier.size(size * 0.55f),
                contentScale = ContentScale.Fit,
            )
        } else {
            Icon(
                imageVector = style.vector,
                contentDescription = null,
                tint = style.tint,
                modifier = Modifier.size(size * 0.52f),
            )
        }
    }
}

@Composable
fun MoaSectionTitle(
    title: String,
    iconType: Moa3DIconType,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Moa3DIcon(type = iconType, size = 30.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF101B33),
        )
    }
}
