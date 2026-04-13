package com.bounty.zoomnote.input

import android.view.MotionEvent
import com.bounty.zoomnote.model.Point
import com.bounty.zoomnote.rendering.ViewMatrix

class StylusHandler(
    private val viewMatrix: ViewMatrix,
    private val onStrokeStarted: () -> Unit,
    private val onPointAdded: (Point) -> Unit,
    private val onStrokeFinished: () -> Unit,
    private val onSPenButtonUndo: (() -> Unit)? = null
) {
    private var isDrawing = false

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS) return false

        // S Pen primary button → undo (button pressed without drawing)
        if (event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY != 0) {
            onSPenButtonUndo?.invoke()
            return true
        }

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
