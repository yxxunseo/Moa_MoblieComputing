package com.example.moa_project.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.MoaTextTertiary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashTop = Color(0xFF8C6EF4)
private val SplashMiddle = Color(0xFFAFCBFA)
private val SplashBottom = Color(0xFFF7F8FC)

private const val SplashTagline = "친구들을 모아봐요"
private const val SplashFooter = "함께 하는 시간 MOA"
private val LogoLetters = listOf("M", "O", "A")

@Composable
fun MoaSplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit = {},
) {
    var logoLettersVisible by remember { mutableIntStateOf(0) }
    var typedChars by remember { mutableIntStateOf(0) }
    var showCursor by remember { mutableStateOf(true) }
    val footerAlpha = remember { Animatable(0f) }
    val mascotAlpha = remember { Animatable(0f) }
    val mascotWalk = remember { Animatable(0f) }

    val cursorAlpha by rememberInfiniteTransition(label = "splashCursor").animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorBlink",
    )

    LaunchedEffect(Unit) {
        launch {
            delay(280)
            mascotAlpha.animateTo(1f, tween(durationMillis = 420, easing = FastOutSlowInEasing))
        }
        launch {
            delay(360)
            mascotWalk.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2600, easing = LinearEasing),
            )
        }

        LogoLetters.indices.forEach { index ->
            delay(130)
            logoLettersVisible = index + 1
        }

        delay(320)

        SplashTagline.indices.forEach { index ->
            delay(82)
            typedChars = index + 1
        }

        delay(480)
        showCursor = false

        footerAlpha.animateTo(1f, tween(durationMillis = 480, easing = FastOutSlowInEasing))
        delay(1000)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBottom),
    ) {
        SplashBackground(modifier = Modifier.fillMaxSize())

        SplashWalkPath(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 118.dp)
                .fillMaxWidth()
                .height(48.dp),
        )

        SplashWalkingMascot(
            walkProgress = mascotWalk.value,
            alpha = mascotAlpha.value,
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MoaLogoRow(visibleCount = logoLettersVisible)

            Spacer(modifier = Modifier.height(28.dp))

            TypewriterLine(
                text = SplashTagline.take(typedChars),
                showCursor = showCursor,
                cursorAlpha = cursorAlpha,
                progress = typedChars.toFloat() / SplashTagline.length.coerceAtLeast(1),
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp)
                .alpha(footerAlpha.value),
            text = SplashFooter,
            color = MoaTextTertiary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Light,
            fontFamily = SBAggroFontFamily,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun SplashWalkingMascot(
    walkProgress: Float,
    alpha: Float,
) {
    val bobPhase by rememberInfiniteTransition(label = "mascotBob").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bobPhase",
    )
    val bobDp = (bobPhase - 0.5f) * 14f
    val squashScale = 1f + (0.5f - kotlin.math.abs(bobPhase - 0.5f)) * 0.06f

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val mascotSize = 64.dp
        val groundY = maxHeight * 0.72f
        val startX = 20.dp
        val endX = maxWidth - mascotSize - 20.dp
        val x = startX + (endX - startX) * walkProgress
        val density = LocalDensity.current

        val shadowWidth = with(density) { 46.dp.toPx() }
        val shadowHeight = with(density) { 9.dp.toPx() }
        val shadowCenterX = with(density) { (x + mascotSize * 0.42f).toPx() }
        val shadowCenterY = with(density) { (groundY + mascotSize - 4.dp + bobDp.dp).toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha * 0.7f),
        ) {
            drawOval(
                color = Color(0xFF4A3D8C).copy(alpha = 0.18f),
                topLeft = Offset(
                    shadowCenterX - shadowWidth / 2f,
                    shadowCenterY - shadowHeight / 2f,
                ),
                size = Size(shadowWidth, shadowHeight),
            )
        }

        MoaMascot(
            modifier = Modifier
                .offset(x = x, y = groundY + bobDp.dp)
                .alpha(alpha)
                .size(mascotSize)
                .graphicsLayer {
                    scaleX = if (walkProgress < 0.92f) 1f else -1f
                    scaleY = squashScale
                },
            size = mascotSize,
        )
    }
}

@Composable
private fun SplashWalkPath(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.alpha(0.35f)) {
        val path = Path().apply {
            moveTo(size.width * 0.04f, size.height * 0.62f)
            cubicTo(
                size.width * 0.28f, size.height * 0.42f,
                size.width * 0.62f, size.height * 0.78f,
                size.width * 0.96f, size.height * 0.55f,
            )
        }
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round),
        )
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.35f),
            style = Stroke(width = 8f, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun MoaLogoRow(visibleCount: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        LogoLetters.forEachIndexed { index, letter ->
            val visible = index < visibleCount
            val scale by animateFloatAsState(
                targetValue = if (visible) 1f else 0.55f,
                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                label = "logoScale$index",
            )
            val alpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(durationMillis = 280),
                label = "logoAlpha$index",
            )

            Text(
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha),
                text = letter,
                color = Color.White,
                fontSize = 88.sp,
                fontWeight = FontWeight.Black,
                fontFamily = SBAggroFontFamily,
                letterSpacing = if (index == 2) 0.sp else 2.sp,
            )
        }
    }
}

@Composable
private fun TypewriterLine(
    text: String,
    showCursor: Boolean,
    cursorAlpha: Float,
    progress: Float,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SBAggroFontFamily,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp,
            )

            if (showCursor) {
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .alpha(cursorAlpha)
                        .background(Color.White.copy(alpha = 0.9f)),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .width(220.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.18f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.9f),
                            ),
                        ),
                    ),
            )
        }
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
                endY = height * 0.82f,
            ),
            size = size,
        )

        val leftCloud = Path().apply {
            moveTo(0f, height * 0.52f)
            cubicTo(width * 0.18f, height * 0.58f, width * 0.04f, height * 0.64f, width * 0.22f, height * 0.76f)
            cubicTo(width * 0.34f, height * 0.84f, width * 0.26f, height * 0.94f, 0f, height)
            close()
        }
        drawPath(leftCloud, SplashBottom.copy(alpha = 0.85f))

        val rightCloud = Path().apply {
            moveTo(width, height * 0.54f)
            cubicTo(width * 0.78f, height * 0.56f, width * 0.96f, height * 0.66f, width * 0.74f, height * 0.68f)
            cubicTo(width * 0.56f, height * 0.70f, width * 0.56f, height * 0.88f, width, height)
            close()
        }
        drawPath(rightCloud, SplashBottom.copy(alpha = 0.85f))

        repeat(6) { index ->
            val x = width * (0.12f + index * 0.14f)
            val y = height * (0.22f + (index % 3) * 0.04f)
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = 2.5f + index * 0.4f,
                center = Offset(x, y),
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
