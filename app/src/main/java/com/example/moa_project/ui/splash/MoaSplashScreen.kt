package com.example.moa_project.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.R
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashTop = Color(0xFF8C6EF4)
private val SplashMiddle = Color(0xFFAFCBFA)
private val SplashBottom = Color(0xFFF7F8FC)

@Composable
fun MoaSplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit = {},
) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.88f) }
    val characterAlpha = remember { Animatable(0f) }
    val characterScale = remember { Animatable(0.76f) }
    val taglineAlpha = remember { Animatable(0f) }

    val floating by rememberInfiniteTransition(label = "splashFloating").animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "characterFloat",
    )

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(1f, tween(durationMillis = 620, easing = FastOutSlowInEasing))
        }

        delay(180)

        launch {
            characterAlpha.animateTo(1f, tween(durationMillis = 500, easing = FastOutSlowInEasing))
        }
        launch {
            characterScale.animateTo(1f, tween(durationMillis = 680, easing = FastOutSlowInEasing))
        }

        delay(260)
        taglineAlpha.animateTo(1f, tween(durationMillis = 420, easing = FastOutSlowInEasing))

        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBottom),
    ) {
        SplashBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 118.dp)
                .alpha(logoAlpha.value)
                .scale(logoScale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "MOA",
                color = Color.White,
                fontSize = 76.sp,
                fontWeight = FontWeight.Black,
                fontFamily = SBAggroFontFamily,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "친구들을 모아봐요",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SBAggroFontFamily,
                textAlign = TextAlign.Center,
            )
        }

        WitchGraphic(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (20 + floating).dp)
                .alpha(characterAlpha.value)
                .scale(characterScale.value),
        )

        Text(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(taglineAlpha.value),
            text = "함께 하는 시간 MOA",
            color = Color(0xFFB7B7B7),
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            fontFamily = SBAggroFontFamily,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SplashBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(SplashTop, SplashMiddle, SplashBottom),
                startY = 0f,
                endY = height * 0.78f,
            ),
            size = size,
        )

        val leftCloud = Path().apply {
            moveTo(0f, height * 0.46f)
            cubicTo(width * 0.20f, height * 0.54f, width * 0.05f, height * 0.58f, width * 0.25f, height * 0.72f)
            cubicTo(width * 0.36f, height * 0.80f, width * 0.28f, height * 0.90f, 0f, height)
            close()
        }
        drawPath(leftCloud, SplashBottom)

        val rightCloud = Path().apply {
            moveTo(width, height * 0.47f)
            cubicTo(width * 0.76f, height * 0.50f, width * 0.98f, height * 0.61f, width * 0.75f, height * 0.63f)
            cubicTo(width * 0.55f, height * 0.64f, width * 0.55f, height * 0.84f, width, height)
            close()
        }
        drawPath(rightCloud, SplashBottom)
    }
}

@Composable
private fun WitchGraphic(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 340.dp, height = 400.dp)
    ) {
        // Draw Strings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            val color = Color.White.copy(alpha = 0.8f)
            
            // From blue balloon to blue character
            val path1 = Path().apply {
                moveTo(57.dp.toPx(), 103.dp.toPx()) // balloon 1 bottom
                cubicTo(60.dp.toPx(), 150.dp.toPx(), 90.dp.toPx(), 200.dp.toPx(), 120.dp.toPx(), 240.dp.toPx()) // character
            }
            drawPath(path1, color, style = stroke)

            // From orange balloon to blue character
            val path2 = Path().apply {
                moveTo(165.dp.toPx(), 120.dp.toPx()) // balloon 2 bottom
                cubicTo(165.dp.toPx(), 160.dp.toPx(), 160.dp.toPx(), 200.dp.toPx(), 150.dp.toPx(), 240.dp.toPx())
            }
            drawPath(path2, color, style = stroke)

            // From yellow balloon to pink character
            val path3 = Path().apply {
                moveTo(253.dp.toPx(), 125.dp.toPx()) // balloon 3 bottom
                cubicTo(240.dp.toPx(), 160.dp.toPx(), 220.dp.toPx(), 200.dp.toPx(), 215.dp.toPx(), 240.dp.toPx())
            }
            drawPath(path3, color, style = stroke)
        }

        // Broomstick
        DrawSplashSvg7(modifier = Modifier.offset(x = (-10).dp, y = 230.dp).size(258.dp, 137.dp))
        
        // Purple Character (Back)
        DrawSplashSvg6(modifier = Modifier.offset(x = 135.dp, y = 175.dp).size(92.dp, 97.dp))
        
        // Broom Bristles
        DrawSplashSvg8(modifier = Modifier.offset(x = 190.dp, y = 295.dp).size(72.dp, 56.dp))
        
        // Pink Character (Back Right)
        DrawSplashSvg5(modifier = Modifier.offset(x = 195.dp, y = 210.dp).size(68.dp, 94.dp))

        // Blue Character (Front)
        DrawSplashSvg4(modifier = Modifier.offset(x = 55.dp, y = 180.dp).size(152.dp, 151.dp))

        // Light Blue Balloon
        DrawSplashSvg1(modifier = Modifier.offset(x = 20.dp, y = 30.dp).size(75.dp, 73.dp))
        
        // Orange Balloon
        DrawSplashSvg2(modifier = Modifier.offset(x = 110.dp, y = 10.dp).size(110.dp, 108.dp))
        
        // Yellow Balloon
        DrawSplashSvg3(modifier = Modifier.offset(x = 220.dp, y = 60.dp).size(66.dp, 65.dp))
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MoaSplashScreenPreview() {
    Moa_ProjectTheme {
        MoaSplashScreen()
    }
}
