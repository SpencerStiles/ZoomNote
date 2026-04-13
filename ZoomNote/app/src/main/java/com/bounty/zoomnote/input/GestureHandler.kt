package com.bounty.zoomnote.input

import android.view.MotionEvent
import com.bounty.zoomnote.rendering.ViewMatrix
import kotlin.math.sqrt

class GestureHandler(
    private val viewMatrix: ViewMatrix,
    private val onViewChanged: () -> Unit,
    private val onUndo: () -> Unit,
    private val onRedo: () -> Unit
) {
    private var lastTouchX = 0.0
    private var lastTouchY = 0.0
    private var lastPinchDist = 0.0
    private var isPanning = false
    private var isPinching = false
    private var fingerCount = 0
    private var tapStartTime = 0L
    private val tapTimeout = 300L
    private var tapActionFired = false

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                fingerCount = 1
                lastTouchX = event.x.toDouble()
                lastTouchY = event.y.toDouble()
                isPanning = true
                tapStartTime = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                fingerCount = event.pointerCount
                if (fingerCount == 2) {
                    isPanning = false
                    isPinching = true
                    lastPinchDist = pinchDistance(event)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPinching && event.pointerCount >= 2) {
                    val dist = pinchDistance(event)
                    if (lastPinchDist > 0) {
                        val factor = dist / lastPinchDist
                        val midX = (event.getX(0) + event.getX(1)) / 2.0
                        val midY = (event.getY(0) + event.getY(1)) / 2.0
                        viewMatrix.zoomBy(factor, midX, midY)
                        onViewChanged()
                    }
                    lastPinchDist = dist
                    return true
                }
                if (isPanning && fingerCount == 1) {
                    val dx = event.x.toDouble() - lastTouchX
                    val dy = event.y.toDouble() - lastTouchY
                    viewMatrix.pan(dx, dy)
                    lastTouchX = event.x.toDouble()
                    lastTouchY = event.y.toDouble()
                    onViewChanged()
                    return true
                }
                return false
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val elapsed = System.currentTimeMillis() - tapStartTime
                if (elapsed < tapTimeout && !tapActionFired) {
                    if (fingerCount == 2) {
                        tapActionFired = true
                        onUndo()
                    } else if (fingerCount == 3) {
                        tapActionFired = true
                        onRedo()
                    }
                }
                fingerCount = event.pointerCount - 1
                if (fingerCount < 2) {
                    isPinching = false
                    lastPinchDist = 0.0
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPanning = false
                isPinching = false
                fingerCount = 0
                tapActionFired = false
                return true
            }
        }
        return false
    }

    private fun pinchDistance(event: MotionEvent): Double {
        if (event.pointerCount < 2) return 0.0
        val dx = (event.getX(0) - event.getX(1)).toDouble()
        val dy = (event.getY(0) - event.getY(1)).toDouble()
        return sqrt(dx * dx + dy * dy)
    }
}
