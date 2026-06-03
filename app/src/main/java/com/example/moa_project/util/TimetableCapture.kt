package com.example.moa_project.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.moa_project.ui.home.WeeklyTimetableBlock
import com.example.moa_project.ui.home.WeeklyTimetableGrid
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object TimetableCapture {

    suspend fun capture(
        activity: Activity,
        weekLabel: String,
        blocks: List<WeeklyTimetableBlock>,
    ): Bitmap? = suspendCancellableCoroutine { cont ->
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
        val composeView = ComposeView(activity).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            visibility = View.INVISIBLE
            layoutParams = FrameLayout.LayoutParams(
                1080,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setContent {
                Moa_ProjectTheme {
                    WeeklyTimetableGrid(
                        weekLabel = weekLabel,
                        blocks = blocks,
                        forExport = true,
                    )
                }
            }
        }

        content.addView(composeView)
        composeView.post {
            try {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                composeView.measure(widthSpec, heightSpec)
                composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
                val w = composeView.measuredWidth
                val h = composeView.measuredHeight
                if (w <= 0 || h <= 0) {
                    cont.resume(null)
                    return@post
                }
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                composeView.draw(Canvas(bitmap))
                cont.resume(bitmap)
            } catch (_: Exception) {
                if (cont.isActive) cont.resume(null)
            } finally {
                content.removeView(composeView)
            }
        }

        cont.invokeOnCancellation {
            content.removeView(composeView)
        }
    }
}
