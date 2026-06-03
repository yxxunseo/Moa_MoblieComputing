package com.example.moa_project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.moa_project.R

@Composable
fun MoaMascot(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    contentDescription: String = "모아 캐릭터",
) {
    Image(
        painter = painterResource(id = R.drawable.ic_character),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
    )
}
