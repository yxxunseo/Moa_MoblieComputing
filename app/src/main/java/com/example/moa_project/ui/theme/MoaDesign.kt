package com.example.moa_project.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MoaSpacing {
    val screen = 20.dp
    val section = 20.dp
    val item = 12.dp
    val tight = 8.dp
}

object MoaRadius {
    val card = 16.dp
    val button = 12.dp
    val chip = 10.dp
}

fun Modifier.moaScreenBackground(): Modifier = background(MoaScreenBackground)

fun Modifier.moaCard(
    elevation: Dp = 2.dp,
    padding: Dp = 16.dp,
): Modifier = this
    .shadow(elevation, RoundedCornerShape(MoaRadius.card), spotColor = MoaCardShadow.copy(alpha = 0.35f))
    .clip(RoundedCornerShape(MoaRadius.card))
    .background(Color.White)
    .padding(padding)

val MoaScreenPadding = PaddingValues(
    horizontal = MoaSpacing.screen,
    vertical = 24.dp,
)
