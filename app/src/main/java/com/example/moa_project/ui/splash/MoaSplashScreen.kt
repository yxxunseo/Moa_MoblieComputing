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

        BalloonBundle(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 20.dp)
                .alpha(characterAlpha.value)
                .scale(characterScale.value),
        )

        Image(
            painter = painterResource(id = R.drawable.ic_character),
            contentDescription = "MOA 캐릭터",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (160 + floating).dp)
                .size(178.dp)
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
private fun BalloonBundle(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "balloonFloating")
    val leftFloat by transition.animateFloat(
        initialValue = -6f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "leftBalloonFloat",
    )
    val centerFloat by transition.animateFloat(
        initialValue = 5f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1680, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "centerBalloonFloat",
    )
    val rightFloat by transition.animateFloat(
        initialValue = -4f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rightBalloonFloat",
    )

    Canvas(
        modifier = modifier
            .size(width = 336.dp, height = 310.dp),
    ) {
        val width = size.width
        val height = size.height
        val base = Offset(width * 0.50f, height * 0.82f)
        val leftCenter = Offset(width * 0.23f - leftFloat * 0.35f, height * 0.16f + leftFloat)
        val centerCenter = Offset(width * 0.50f + centerFloat * 0.18f, height * 0.13f + centerFloat)
        val rightCenter = Offset(width * 0.78f + rightFloat * 0.28f, height * 0.25f + rightFloat)

        drawBalloonLine(leftCenter.copy(y = leftCenter.y + width * 0.12f), base.copy(x = width * 0.43f))
        drawBalloonLine(centerCenter.copy(y = centerCenter.y + width * 0.15f), base)
        drawBalloonLine(rightCenter.copy(y = rightCenter.y + width * 0.10f), base.copy(x = width * 0.58f))

        drawBlueBalloon(
            center = leftCenter,
            radius = width * 0.12f,
        )
        drawOrangeBalloon(
            center = centerCenter,
            radius = width * 0.15f,
        )
        drawCatBalloon(
            center = rightCenter,
            radius = width * 0.108f,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBalloonLine(
    start: Offset,
    end: Offset,
) {
    val path = Path().apply {
        moveTo(start.x, start.y)
        cubicTo(start.x, start.y + size.height * 0.28f, end.x - size.width * 0.10f, end.y - size.height * 0.24f, end.x, end.y)
    }
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.86f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBalloonBase(
    center: Offset,
    radius: Float,
    color: Color,
) {
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(color, Color.White.copy(alpha = 0.90f)),
            startY = center.y - radius,
            endY = center.y + radius,
        ),
        radius = radius,
        center = center,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlueBalloon(
    center: Offset,
    radius: Float,
) {
    val lineColor = Color(0xFF78A2E4)
    drawBalloonBase(center, radius, Color(0xFFDDEEFF))

    drawLine(color = lineColor, start = Offset(center.x - radius * 0.48f, center.y - radius * 0.28f), end = Offset(center.x - radius * 0.28f, center.y - radius * 0.08f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = lineColor, start = Offset(center.x + radius * 0.30f, center.y - radius * 0.08f), end = Offset(center.x + radius * 0.50f, center.y - radius * 0.24f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)

    val mouth = Path().apply {
        moveTo(center.x - radius * 0.14f, center.y + radius * 0.02f)
        cubicTo(center.x - radius * 0.08f, center.y - radius * 0.10f, center.x + radius * 0.02f, center.y - radius * 0.10f, center.x + radius * 0.09f, center.y + radius * 0.02f)
        cubicTo(center.x + radius * 0.13f, center.y + radius * 0.09f, center.x + radius * 0.22f, center.y + radius * 0.09f, center.x + radius * 0.28f, center.y + radius * 0.00f)
    }
    drawPath(mouth, lineColor, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))

    drawBlushSlash(Offset(center.x - radius * 0.78f, center.y - radius * 0.01f), radius, tiltRight = true)
    drawBlushSlash(Offset(center.x + radius * 0.74f, center.y + radius * 0.02f), radius, tiltRight = false)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrangeBalloon(
    center: Offset,
    radius: Float,
) {
    val faceColor = Color(0xFFD7B99D)
    val cheek = Color(0xFFFF8F89)
    drawBalloonBase(center, radius, Color(0xFFFFE2BA))

    drawLine(color = faceColor.copy(alpha = 0.48f), start = Offset(center.x - radius * 0.46f, center.y - radius * 0.42f), end = Offset(center.x - radius * 0.18f, center.y - radius * 0.27f), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = faceColor.copy(alpha = 0.48f), start = Offset(center.x + radius * 0.46f, center.y - radius * 0.42f), end = Offset(center.x + radius * 0.18f, center.y - radius * 0.27f), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)

    drawCircle(Color(0xFFFFC477), radius = radius * 0.125f, center = Offset(center.x - radius * 0.31f, center.y - radius * 0.02f))
    drawCircle(Color(0xFFFFC477), radius = radius * 0.125f, center = Offset(center.x + radius * 0.31f, center.y - radius * 0.02f))
    drawOval(
        color = cheek.copy(alpha = 0.76f),
        topLeft = Offset(center.x - radius * 0.50f, center.y + radius * 0.16f),
        size = Size(radius * 0.34f, radius * 0.13f),
    )
    drawOval(
        color = cheek.copy(alpha = 0.76f),
        topLeft = Offset(center.x + radius * 0.16f, center.y + radius * 0.16f),
        size = Size(radius * 0.34f, radius * 0.13f),
    )

    val mouth = Path().apply {
        moveTo(center.x - radius * 0.19f, center.y + radius * 0.17f)
        cubicTo(center.x - radius * 0.11f, center.y + radius * 0.31f, center.x - radius * 0.03f, center.y + radius * 0.31f, center.x + radius * 0.02f, center.y + radius * 0.18f)
        cubicTo(center.x + radius * 0.08f, center.y + radius * 0.31f, center.x + radius * 0.18f, center.y + radius * 0.31f, center.x + radius * 0.27f, center.y + radius * 0.17f)
    }
    drawPath(mouth, faceColor.copy(alpha = 0.72f), style = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlushSlash(
    center: Offset,
    radius: Float,
    tiltRight: Boolean,
) {
    val sign = if (tiltRight) 1f else -1f
    repeat(2) { index ->
        val dx = index * radius * 0.10f
        drawLine(
            color = Color(0xFFFF8C78).copy(alpha = 0.82f),
            start = Offset(center.x + dx, center.y + radius * 0.08f),
            end = Offset(center.x + dx + sign * radius * 0.12f, center.y - radius * 0.08f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawClosedEye(
    center: Offset,
    radius: Float,
    flip: Boolean = false,
) {
    val path = Path().apply {
        moveTo(center.x - radius * 0.18f, center.y)
        cubicTo(center.x - radius * 0.08f, center.y + radius * 0.10f, center.x + radius * 0.08f, center.y + radius * 0.10f, center.x + radius * 0.18f, center.y)
    }
    drawPath(path, Color(0xFFE99EA2), style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCatMouth(
    center: Offset,
    radius: Float,
) {
    val color = Color(0xFFE99EA2)
    drawOval(
        color = color.copy(alpha = 0.56f),
        topLeft = Offset(center.x - radius * 0.06f, center.y - radius * 0.03f),
        size = Size(radius * 0.12f, radius * 0.08f),
    )
    val mouth = Path().apply {
        moveTo(center.x, center.y + radius * 0.03f)
        cubicTo(center.x - radius * 0.07f, center.y + radius * 0.16f, center.x - radius * 0.17f, center.y + radius * 0.16f, center.x - radius * 0.23f, center.y + radius * 0.05f)
        moveTo(center.x, center.y + radius * 0.03f)
        cubicTo(center.x + radius * 0.07f, center.y + radius * 0.16f, center.x + radius * 0.17f, center.y + radius * 0.16f, center.x + radius * 0.23f, center.y + radius * 0.05f)
    }
    drawPath(mouth, color, style = Stroke(width = 1.55.dp.toPx(), cap = StrokeCap.Round))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCatBalloon(
    center: Offset,
    radius: Float,
) {
    val earColor = Color(0xFFFFD55D)
    val faceColor = Color(0xFFE99EA2)
    val leftEar = Path().apply {
        moveTo(center.x - radius * 0.82f, center.y - radius * 0.45f)
        lineTo(center.x - radius * 0.56f, center.y - radius * 1.13f)
        lineTo(center.x - radius * 0.14f, center.y - radius * 0.72f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(center.x + radius * 0.82f, center.y - radius * 0.45f)
        lineTo(center.x + radius * 0.56f, center.y - radius * 1.13f)
        lineTo(center.x + radius * 0.14f, center.y - radius * 0.72f)
        close()
    }
    val leftInnerEar = Path().apply {
        moveTo(center.x - radius * 0.66f, center.y - radius * 0.50f)
        lineTo(center.x - radius * 0.54f, center.y - radius * 0.90f)
        lineTo(center.x - radius * 0.28f, center.y - radius * 0.66f)
        close()
    }
    val rightInnerEar = Path().apply {
        moveTo(center.x + radius * 0.66f, center.y - radius * 0.50f)
        lineTo(center.x + radius * 0.54f, center.y - radius * 0.90f)
        lineTo(center.x + radius * 0.28f, center.y - radius * 0.66f)
        close()
    }

    drawPath(leftEar, earColor)
    drawPath(rightEar, earColor)
    drawPath(leftInnerEar, Color(0xFFFFF1C2))
    drawPath(rightInnerEar, Color(0xFFFFF1C2))
    drawBalloonBase(center, radius, Color(0xFFFFEDAE))

    drawClosedEye(Offset(center.x - radius * 0.30f, center.y - radius * 0.10f), radius)
    drawClosedEye(Offset(center.x + radius * 0.30f, center.y - radius * 0.10f), radius, flip = true)
    drawCatMouth(Offset(center.x + radius * 0.02f, center.y + radius * 0.10f), radius)
    drawSparkle(Offset(center.x - radius * 0.72f, center.y + radius * 0.12f), radius * 0.09f, faceColor)
    drawSparkle(Offset(center.x + radius * 0.72f, center.y + radius * 0.12f), radius * 0.09f, faceColor)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color,
) {
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color.copy(alpha = 0.8f),
        start = Offset(center.x - radius * 0.65f, center.y - radius * 0.65f),
        end = Offset(center.x + radius * 0.65f, center.y + radius * 0.65f),
        strokeWidth = 1.3.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color.copy(alpha = 0.8f),
        start = Offset(center.x + radius * 0.65f, center.y - radius * 0.65f),
        end = Offset(center.x - radius * 0.65f, center.y + radius * 0.65f),
        strokeWidth = 1.3.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MoaSplashScreenPreview() {
    Moa_ProjectTheme {
        MoaSplashScreen()
    }
}
