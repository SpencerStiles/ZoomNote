package com.spencerstiles.zoomnote.input

import android.view.MotionEvent
import com.spencerstiles.zoomnote.model.Point
import com.spencerstiles.zoomnote.rendering.ViewMatrix

class StylusHandler(
    private val viewMatrix: ViewMatrix,
    private val onStrokeStarted: () -> Unit,
    private val onPointAdded: (Point) -> Unit,
    private val onStrokeFinished: () -> Unit,
    private val onStrokeCancelled: () -> Unit = {},
    private val onSPenButtonUndo: (() -> Unit)? = null
) {
    private var isDrawing = false
    private var spenButtonHandled = false

    fun onTouchEvent(event: MotionEvent): Boolean {
        val toolType = event.getToolType(0)
        if (toolType != MotionEvent.TOOL_TYPE_STYLUS && toolType != MotionEvent.TOOL_TYPE_FINGER) return false
        // Only handle single-touch drawing; let multi-touch fall through to GestureHandler
        if (event.pointerCount > 1) {
            if (isDrawing) {
                isDrawing = false
                onStrokeCancelled() // discard, don't save
            }
            return false
        }

        // S Pen primary button → undo (button pressed without drawing)
        val buttonPressed = event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY != 0
        if (buttonPressed) {
            if (!spenButtonHandled) {
                spenButtonHandled = true
                onSPenButtonUndo?.invoke()
            }
            return true
        }
        spenButtonHandled = false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isDrawing = true
                onStrokeStarted()
                addPoint(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDrawing) return false
                for (i in 0 until event.historySize) {
                    addHistoricalPoint(event, i)
                }
                addPoint(event)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isDrawing) return false
                addPoint(event)
                isDrawing = false
                onStrokeFinished()
                return true
            }
        }
        return false
    }

    private fun addPoint(event: MotionEvent) {
        val (wx, wy) = viewMatrix.screenToWorld(event.x.toDouble(), event.y.toDouble())
        onPointAdded(Point(wx, wy, event.pressure, event.eventTime))
    }

    private fun addHistoricalPoint(event: MotionEvent, index: Int) {
        val (wx, wy) = viewMatrix.screenToWorld(
            event.getHistoricalX(index).toDouble(),
            event.getHistoricalY(index).toDouble()
        )
        onPointAdded(Point(wx, wy, event.getHistoricalPressure(index), event.getHistoricalEventTime(index)))
    }
}
