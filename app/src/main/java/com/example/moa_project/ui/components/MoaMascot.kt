package com.example.moa_project.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.moa_project.R

enum class MoaMascotVariant {
    Default,
    Sparkle,
    Heart,
}

@DrawableRes
private fun MoaMascotVariant.drawableRes(): Int = when (this) {
    MoaMascotVariant.Default -> R.drawable.ic_character
    MoaMascotVariant.Sparkle -> R.drawable.ic_character_sparkle
    MoaMascotVariant.Heart -> R.drawable.ic_character_heart
}

@Composable
fun MoaMascot(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    variant: MoaMascotVariant = MoaMascotVariant.Default,
    contentDescription: String = "모아 캐릭터",
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = variant.drawableRes()),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .padding(size * 0.04f),
            contentScale = ContentScale.Fit,
        )
    }
}
