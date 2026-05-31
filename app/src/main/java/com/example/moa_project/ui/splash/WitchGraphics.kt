package com.example.moa_project.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.core.graphics.PathParser
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.Path

@Composable
fun DrawSplashSvg1(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 75f
        val scaleY = size.height / 73f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            val path_1 = PathParser.createPathFromPathData("M74.5005 34.617C74.5005 53.7354 59.6379 72.6777 40.0005 72.6777C20.3631 72.6777 0 53.7354 0 34.617C0 15.4986 15.9193 0 35.5567 0C55.1941 0 74.5005 15.4986 74.5005 34.617Z").asComposePath()
            drawPath(path_1, brush = Brush.linearGradient(listOf(Color(0xFF92BEFD), Color.White), Offset(35.5567f, 0f), Offset(35.5567f, 71.1134f)), style = androidx.compose.ui.graphics.drawscope.Fill)
            val path_2 = PathParser.createPathFromPathData("M20.5 11.1777L25.7895 16.8747L18 19.1777M46 16.1777L35.9868 18.8444L40.5 23.6777").asComposePath()
            drawPath(path_2, color = Color(0xFF88ACE0), style = Stroke(width = 1f, cap = StrokeCap.Round))
            val path_3 = PathParser.createPathFromPathData("M25 19.6777C25.6142 20.8803 25.8376 21.7996 25.6405 22.6777M23.5 25.6777C24.7269 24.5231 25.428 23.6243 25.6405 22.6777M25.6405 22.6777L34 23.6777").asComposePath()
            drawPath(path_3, color = Color(0xFF88ACE0), style = Stroke(width = 1f, cap = StrokeCap.Round))
            val path_4 = PathParser.createPathFromPathData("M9 21.6777L4.5 26.1777M10.5 22.6777L6 27.1777M49 27.1777L44 32.1777M50.5 28.1777L45.5 33.1777").asComposePath()
            drawPath(path_4, color = Color(0xFFEFA9A0), style = Stroke(width = 1f, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun DrawSplashSvg2(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 159f
        val scaleY = size.height / 157f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            val path_5 = PathParser.createPathFromPathData("M129.795 80.9512C129.795 108.897 105.863 132.235 79.1951 124.03C54.5789 129.5 28.5952 108.897 28.5952 80.9512C28.5952 53.0057 51.2496 30.3513 79.1951 30.3513C107.141 30.3513 129.795 53.0057 129.795 80.9512Z").asComposePath()
            drawPath(path_5, brush = Brush.linearGradient(listOf(Color(0xFFFFCB8F), Color.White), Offset(79.1951f, 30.3513f), Offset(79.1951f, 125.695f)), style = androidx.compose.ui.graphics.drawscope.Fill)
                drawOval(color = Color(0xFFFF7F7F).copy(alpha=0.6f), topLeft = Offset(51.84419f, 76.84937000000001f), size = Size(15.04322f, 5.47026f))
                drawOval(color = Color(0xFFFF7F7F).copy(alpha=0.6f), topLeft = Offset(94.23839000000001f, 78.21657f), size = Size(15.04322f, 5.47026f))
            rotate(degrees = -18.4289f, pivot = Offset(61.5323f, 72.3386f)) {
                drawCircle(color = Color(0xFFFFCB8F), radius = 6.57856f, center = Offset(61.5323f, 72.3386f))
            }
            rotate(degrees = -18.4289f, pivot = Offset(101.059f, 73.0363f)) {
                drawCircle(color = Color(0xFFFFCB8F), radius = 6.57856f, center = Offset(101.059f, 73.0363f))
            }
            val path_6 = PathParser.createPathFromPathData("M69.644 78.8865C69.644 78.8865 72.0383 83.0531 74.644 83.3865C77.7064 83.7784 78.0666 78.6392 81.144 78.8865C83.9103 79.1088 83.87 83.3079 86.644 83.3865C89.1889 83.4586 92.144 79.8865 92.144 79.8865").asComposePath()
            drawPath(path_6, color = Color(0xFFDDC7AF), style = Stroke(width = 1f, cap = StrokeCap.Round))
            val path_7 = PathParser.createPathFromPathData("M53.644 51.9993L69.1445 60.9998M89.6445 62.4998L108.645 53.9998").asComposePath()
            drawPath(path_7, color = Color(0xFFDDC7AF), style = Stroke(width = 1f, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun DrawSplashSvg3(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 66f
        val scaleY = size.height / 65f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
                drawCircle(brush = Brush.linearGradient(listOf(Color(0xFFF9CD71), Color.White), Offset(30.5f, 4f), Offset(8.5f, 56.5f)), radius = 30.5f, center = Offset(30.5f, 34.5f))
            val path_8 = PathParser.createPathFromPathData("M23 15.5C26.2841 18.5063 28.4141 19.428 33 19M45.5 24C48.5116 27.7901 50.4259 29.266 54.5 30").asComposePath()
            drawPath(path_8, color = Color(0xFFEFA9A0), style = Stroke(width = 1f, cap = StrokeCap.Round))
            val path_9 = PathParser.createPathFromPathData("M43.5 30.5L30.5 26C30.5 26 30.8407 34.9804 35 36C38.8401 36.9413 43.5 30.5 43.5 30.5Z").asComposePath()
            drawPath(path_9, color = Color(0xFFEFA9A0), style = androidx.compose.ui.graphics.drawscope.Fill)
            val path_10 = PathParser.createPathFromPathData("M41.9999 30L33.4999 27C33.4999 27 33.0942 29.2972 36.4999 31C39.5 32.5 41.9999 30 41.9999 30Z").asComposePath()
            drawPath(path_10, color = Color.White, style = androidx.compose.ui.graphics.drawscope.Fill)
            val path_11 = PathParser.createPathFromPathData("M16.1176 20V25M14 22.75H18M15.1765 24L17.2941 21.5M15.1765 21.5L17.2941 24").asComposePath()
            drawPath(path_11, color = Color(0xFFFF7F7F).copy(alpha=0.6f), style = Stroke(width = 0.5f, cap = StrokeCap.Round))
            val path_12 = PathParser.createPathFromPathData("M53.1176 34V39M51 36.75H55M52.1765 38L54.2941 35.5M52.1765 35.5L54.2941 38").asComposePath()
            drawPath(path_12, color = Color(0xFFFF7F7F).copy(alpha=0.6f), style = Stroke(width = 0.5f, cap = StrokeCap.Round))
            val path_13 = PathParser.createPathFromPathData("M18 0L13.5 9.5L30 4L18 0Z").asComposePath()
            drawPath(path_13, color = Color(0xFFFAD381), style = androidx.compose.ui.graphics.drawscope.Fill)
            val path_14 = PathParser.createPathFromPathData("M66 28L57 20L61 36.5L66 28Z").asComposePath()
            drawPath(path_14, color = Color(0xFFFAD381), style = androidx.compose.ui.graphics.drawscope.Fill)
            val path_15 = PathParser.createPathFromPathData("M18.5 1.5L15.5 8.5L26.5 4.5L18.5 1.5Z").asComposePath()
            drawPath(path_15, color = Color.White.copy(alpha=0.54f), style = androidx.compose.ui.graphics.drawscope.Fill)
            val path_16 = PathParser.createPathFromPathData("M64.0005 28L58.6828 23.9999L60.1854 33.3717L64.0005 28Z").asComposePath()
            drawPath(path_16, color = Color.White.copy(alpha=0.54f), style = androidx.compose.ui.graphics.drawscope.Fill)
        }
    }
}

@Composable
fun DrawSplashSvg4(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 152f
        val scaleY = size.height / 151f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            val path_17 = PathParser.createPathFromPathData("M152 76C152 117.974 153.474 150.5 111.5 150.5C69.5264 150.5 0 117.974 0 76C0 34.0264 34.0264 0 76 0C117.974 0 152 34.0264 152 76Z").asComposePath()
            drawPath(path_17, brush = Brush.linearGradient(listOf(Color(0xFF77AAFD), Color(0xFF66FFC2)), Offset(76.0032f, 3.28087e-06f), Offset(185f, 215f)), style = androidx.compose.ui.graphics.drawscope.Fill)
            rotate(degrees = 8.84107f, pivot = Offset(73.5787f, 42.0838f)) {
                drawOval(color = Color(0xFF284478), topLeft = Offset(68.77453f, 36.674139999999994f), size = Size(9.60834f, 10.81932f))
            }
            rotate(degrees = 8.84107f, pivot = Offset(41.5787f, 38.0838f)) {
                drawOval(color = Color(0xFF284478), topLeft = Offset(36.77453f, 32.674139999999994f), size = Size(9.60834f, 10.81932f))
            }
                drawOval(color = Color(0xFFFF7F7F).copy(alpha=0.6f), topLeft = Offset(30.0f, 41.0f), size = Size(10.0f, 7.0f))
                drawOval(color = Color(0xFFFF7F7F).copy(alpha=0.6f), topLeft = Offset(74.0f, 46.0f), size = Size(10.0f, 7.0f))
            val path_18 = PathParser.createPathFromPathData("M72.5 57.258C74.0677 45.2569 42 51.758 42 51.758C42 51.758 47.9271 70.9948 57.5 70.758C65.3785 70.5632 71.4792 65.0726 72.5 57.258Z").asComposePath()
            drawPath(path_18, color = Color(0xFF284478), style = androidx.compose.ui.graphics.drawscope.Fill)
            val path_19 = PathParser.createPathFromPathData("M70 64.0002C61.9624 60.8803 56.9202 60.9054 47.5 63.5002C49.3512 66.4732 50.6849 67.8996 53.5 70.0002C56.3593 70.9287 58.0126 70.9205 61 70.5002C65.3426 68.8915 67.4836 67.6604 70 64.0002Z").asComposePath()
            drawPath(path_19, color = Color(0xFFFF7F7F), style = androidx.compose.ui.graphics.drawscope.Fill)
            val path_20 = PathParser.createPathFromPathData("M60.5 50C55.6785 49.6815 53.2721 50.0545 49.1785 50.5214C45.085 50.9884 52.6011 53.2806 55 53C56.8946 52.7783 65.3215 50.3185 60.5 50Z").asComposePath()
            drawPath(path_20, color = Color.White, style = androidx.compose.ui.graphics.drawscope.Fill)
        }
    }
}

@Composable
fun DrawSplashSvg5(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 68f
        val scaleY = size.height / 94f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            val path_21 = PathParser.createPathFromPathData("M68 34C68 52.7777 37.2777 94 18.5 94C-0.277681 94 0 52.7777 0 34C0 15.2223 15.2223 0 34 0C52.7777 0 68 15.2223 68 34Z").asComposePath()
            drawPath(path_21, brush = Brush.linearGradient(listOf(Color(0xFFF1A5A5), Color.White), Offset(34f, 0f), Offset(34f, 94f)), style = androidx.compose.ui.graphics.drawscope.Fill)
            // Added missing facial details for Pink character
            drawCircle(color = Color(0xFF284478), radius = 2.5f, center = Offset(30f, 55f)) // Left eye
            drawCircle(color = Color(0xFF284478), radius = 2.5f, center = Offset(45f, 60f)) // Right eye
            drawLine(color = Color(0xFF284478), start = Offset(24f, 50f), end = Offset(32f, 52f), strokeWidth = 1.5f, cap = StrokeCap.Round) // Left eyebrow
            drawLine(color = Color(0xFF284478), start = Offset(51f, 55f), end = Offset(43f, 57f), strokeWidth = 1.5f, cap = StrokeCap.Round) // Right eyebrow
            drawArc(color = Color(0xFF284478), startAngle = 0f, sweepAngle = 180f, useCenter = true, topLeft = Offset(33f, 58f), size = Size(10f, 6f)) // Mouth
        }
    }
}

@Composable
fun DrawSplashSvg6(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 92f
        val scaleY = size.height / 97f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            val path_22 = PathParser.createPathFromPathData("M91.5 78.5C91.5 104.457 72.9574 94 47 94C21.0426 94 0 72.9574 0 47C0 21.0426 21.0426 0 47 0C72.9574 0 91.5 52.5426 91.5 78.5Z").asComposePath()
            drawPath(path_22, brush = Brush.linearGradient(listOf(Color(0xFFD478E4), Color(0xFF008686)), Offset(45.75f, 0f), Offset(45.75f, 96.0821f)), style = androidx.compose.ui.graphics.drawscope.Fill)
            // Added missing facial details for Purple character
            drawCircle(color = Color(0xFF284478), radius = 3f, center = Offset(40f, 35f))
            drawCircle(color = Color(0xFF284478), radius = 3f, center = Offset(60f, 40f))
        }
    }
}

@Composable
fun DrawSplashSvg7(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 258f
        val scaleY = size.height / 137f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            val path_23 = PathParser.createPathFromPathData("M250.417 129.149C250.417 129.149 170.056 115.324 117.519 95.7787C99.1675 88.9512 82.9628 79.7719 69.0186 69.9084C28.9071 41.5354 7.50075 7.50113 7.50075 7.50113").asComposePath()
            drawPath(path_23, brush = Brush.linearGradient(listOf(Color(0xFFE6D3AB), Color(0xFFD1BB8E)), Offset(166.991f, 34.7232f), Offset(89.5322f, 103.158f)), style = Stroke(width = 15f, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun DrawSplashSvg8(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 72f
        val scaleY = size.height / 56f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            val path_24 = PathParser.createPathFromPathData("M47.5308 55L1.03076 22L53.5308 0.5M1.03076 22L59.5308 9.5M1.03076 22L65.5308 37M1.03076 22L56.5308 46M1.03076 22L70.5308 20").asComposePath()
            drawPath(path_24, brush = Brush.linearGradient(listOf(Color(0xFFE6D3AB), Color(0xFFDBC79C)), Offset(0.530761f, 22.5f), Offset(34.5308f, 21f)), style = Stroke(width = 1f, cap = StrokeCap.Round))
        }
    }
}
