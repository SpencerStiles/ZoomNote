package com.bounty.zoomnote.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.bounty.zoomnote.model.Stroke
import com.bounty.zoomnote.rendering.ViewMatrix

class MinimapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint().apply { color = Color.argb(180, 255, 255, 255); style = Paint.Style.FILL }
    private val borderPaint = Paint().apply { color = Color.GRAY; style = Paint.Style.STROKE; strokeWidth = 2f }
    private val viewportPaint = Paint().apply { color = Color.argb(80, 33, 150, 243); style = Paint.Style.FILL }
    private val viewportBorderPaint = Paint().apply { color = Color.parseColor("#2196F3"); style = Paint.Style.STROKE; strokeWidth = 2f }

    private var contentBounds = RectF()
    private var viewportRect = RectF()

    var onNavigate: ((Double, Double) -> Unit)? = null
    var onHomeDoubleTap: (() -> Unit)? = null

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onHomeDoubleTap?.invoke()
            return true
        }
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            navigateToTap(e.x, e.y)
            return true
        }
    })

    fun update(strokes: List<Stroke>, viewMatrix: ViewMatrix, screenWidth: Double, screenHeight: Double) {
        if (strokes.isEmpty()) {
            contentBounds = RectF(0f, 0f, 100f, 100f)
        } else {
            val minX = strokes.minOf { it.minX }.toFloat()
            val minY = strokes.minOf { it.minY }.toFloat()
            val maxX = strokes.maxOf { it.maxX }.toFloat()
            val maxY = strokes.maxOf { it.maxY }.toFloat()
            val padding = maxOf(maxX - minX, maxY - minY) * 0.1f
            contentBounds = RectF(minX - padding, minY - padding, maxX + padding, maxY + padding)
        }
        val vp = viewMatrix.viewportInWorld(screenWidth, screenHeight)
        viewportRect = RectF(vp.left.toFloat(), vp.top.toFloat(), vp.right.toFloat(), vp.bottom.toFloat())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRect(0f, 0f, w, h, bgPaint)
        canvas.drawRect(0f, 0f, w, h, borderPaint)
        if (contentBounds.width() <= 0 || contentBounds.height() <= 0) return

        val scale = minOf(w / contentBounds.width(), h / contentBounds.height()) * 0.9f
        val offsetX = (w - contentBounds.width() * scale) / 2f
        val offsetY = (h - contentBounds.height() * scale) / 2f

        fun mapX(wx: Float) = (wx - contentBounds.left) * scale + offsetX
        fun mapY(wy: Float) = (wy - contentBounds.top) * scale + offsetY

        val vpLeft = mapX(viewportRect.left)
        val vpTop = mapY(viewportRect.top)
        val vpRight = mapX(viewportRect.right)
        val vpBottom = mapY(viewportRect.bottom)
        canvas.drawRect(vpLeft, vpTop, vpRight, vpBottom, viewportPaint)
        canvas.drawRect(vpLeft, vpTop, vpRight, vpBottom, viewportBorderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun navigateToTap(screenX: Float, screenY: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (contentBounds.width() <= 0) return
        val scale = minOf(w / contentBounds.width(), h / contentBounds.height()) * 0.9f
        val offsetX = (w - contentBounds.width() * scale) / 2f
        val offsetY = (h - contentBounds.height() * scale) / 2f
        val worldX = ((screenX - offsetX) / scale + contentBounds.left).toDouble()
        val worldY = ((screenY - offsetY) / scale + contentBounds.top).toDouble()
        onNavigate?.invoke(worldX, worldY)
    }
}
