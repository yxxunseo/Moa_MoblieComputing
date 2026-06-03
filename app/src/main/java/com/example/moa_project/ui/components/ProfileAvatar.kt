package com.example.moa_project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.moa_project.R
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaBlueSoft
import com.example.moa_project.ui.theme.SBAggroFontFamily

@Composable
fun ProfileAvatar(
    imageUrl: String?,
    nickname: String? = null,
    modifier: Modifier = Modifier,
    size: Dp,
    defaultImageResId: Int = R.drawable.ic_character,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MoaBlueSoft),
        contentAlignment = Alignment.Center,
    ) {
        when {
            !imageUrl.isNullOrBlank() -> {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "프로필",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = { DefaultAvatarImage(defaultImageResId) },
                    error = { DefaultAvatarImage(defaultImageResId) },
                )
            }
            defaultImageResId != 0 -> {
                DefaultAvatarImage(defaultImageResId)
            }
            !nickname.isNullOrBlank() -> {
                Text(
                    text = nickname.take(1),
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = (size.value * 0.38f).sp,
                    color = MoaBlue,
                )
            }
        }
    }
}

@Composable
private fun DefaultAvatarImage(@androidx.annotation.DrawableRes resId: Int) {
    Image(
        painter = painterResource(resId),
        contentDescription = "기본 프로필",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}
