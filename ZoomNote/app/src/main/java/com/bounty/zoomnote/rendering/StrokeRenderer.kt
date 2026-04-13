package com.bounty.zoomnote.rendering

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.bounty.zoomnote.model.Point
import com.bounty.zoomnote.model.Stroke

class StrokeRenderer {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val path = Path()

    fun draw(canvas: Canvas, stroke: Stroke, viewMatrix: ViewMatrix, alpha: Int = 255) {
        if (stroke.points.size < 2) return
        paint.color = stroke.color
        paint.alpha = alpha

        for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val curr = stroke.points[i]
            val avgPressure = (prev.pressure + curr.pressure) / 2f
            paint.strokeWidth = (stroke.thickness * viewMatrix.scale * (0.5f + avgPressure * 1.5f)).toFloat()

            val prevScreen = viewMatrix.worldToScreen(prev.x, prev.y)
            val currScreen = viewMatrix.worldToScreen(curr.x, curr.y)

            path.reset()
            path.moveTo(prevScreen.first.toFloat(), prevScreen.second.toFloat())
            path.lineTo(currScreen.first.toFloat(), currScreen.second.toFloat())
            canvas.drawPath(path, paint)
        }
    }

    fun drawActiveStroke(canvas: Canvas, points: List<Point>, color: Int, thickness: Float, viewMatrix: ViewMatrix) {
        if (points.size < 2) return
        paint.color = color

        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val avgPressure = (prev.pressure + curr.pressure) / 2f
            paint.strokeWidth = (thickness * viewMatrix.scale * (0.5f + avgPressure * 1.5f)).toFloat()

            val prevScreen = viewMatrix.worldToScreen(prev.x, prev.y)
            val currScreen = viewMatrix.worldToScreen(curr.x, curr.y)

            path.reset()
            path.moveTo(prevScreen.first.toFloat(), prevScreen.second.toFloat())
            path.lineTo(currScreen.first.toFloat(), currScreen.second.toFloat())
            canvas.drawPath(path, paint)
        }
    }
}
