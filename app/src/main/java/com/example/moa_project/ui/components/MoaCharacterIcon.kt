package com.example.moa_project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.moa_project.R

@Composable
fun MoaCharacterIcon(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    contentDescription: String? = "모아",
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_character),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.92f),
            contentScale = ContentScale.Fit,
        )
    }
}
