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

    fun draw(canvas: Canvas, stroke: Stroke, viewMatrix: ViewMatrix) {
        if (stroke.points.size < 2) return

        paint.color = stroke.color
        path.reset()

        val first = stroke.points[0]
        val (sx, sy) = viewMatrix.worldToScreen(first.x, first.y)
        path.moveTo(sx.toFloat(), sy.toFloat())

        for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val curr = stroke.points[i]
            val avgPressure = (prev.pressure + curr.pressure) / 2f
            // Pressure-sensitive width: 0.5x to 2x base thickness
            paint.strokeWidth = (stroke.thickness * viewMatrix.scale * (0.5f + avgPressure * 1.5f)).toFloat()

            val (px, py) = viewMatrix.worldToScreen(curr.x, curr.y)

            // Draw each segment separately to vary width
            val prevScreen = viewMatrix.worldToScreen(prev.x, prev.y)
            val segPath = Path()
            segPath.moveTo(prevScreen.first.toFloat(), prevScreen.second.toFloat())
            segPath.lineTo(px.toFloat(), py.toFloat())
            canvas.drawPath(segPath, paint)
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

            val segPath = Path()
            segPath.moveTo(prevScreen.first.toFloat(), prevScreen.second.toFloat())
            segPath.lineTo(currScreen.first.toFloat(), currScreen.second.toFloat())
            canvas.drawPath(segPath, paint)
        }
    }
}
