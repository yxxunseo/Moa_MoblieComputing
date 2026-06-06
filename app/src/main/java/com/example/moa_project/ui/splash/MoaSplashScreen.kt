package com.example.moa_project.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaMascotVariant
import com.example.moa_project.ui.theme.MoaAccentBlueBg
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily
import kotlinx.coroutines.delay

private const val SplashTagline = "친구들을 모아봐요"

@Composable
fun MoaSplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit = {},
) {
    val contentAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.97f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
        contentScale.animateTo(1f, tween(520, easing = FastOutSlowInEasing))

        delay(200)
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))

        delay(320)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MoaAccentBlueBg.copy(alpha = 0.45f),
                        MoaScreenBackground,
                        MoaScreenBackground,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = contentAlpha.value
                    scaleX = contentScale.value
                    scaleY = contentScale.value
                }
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MoaMascot(size = 72.dp, variant = MoaMascotVariant.Heart)

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "MOA",
                color = MoaTextPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SBAggroFontFamily,
                letterSpacing = 8.sp,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = SplashTagline,
                color = MoaTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = SBAggroFontFamily,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.2).sp,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp)
                .width(120.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE8ECF4)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.value)
                    .height(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MoaBlue),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MoaSplashScreenPreview() {
    Moa_ProjectTheme {
        MoaSplashScreen()
    }
}
