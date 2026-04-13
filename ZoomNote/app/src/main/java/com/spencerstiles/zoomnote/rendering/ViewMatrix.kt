package com.spencerstiles.zoomnote.rendering

class ViewMatrix {
    var scale: Double = 1.0
        private set
    var offsetX: Double = 0.0
        private set
    var offsetY: Double = 0.0
        private set

    fun worldToScreen(wx: Double, wy: Double): Pair<Double, Double> {
        return Pair(wx * scale + offsetX, wy * scale + offsetY)
    }

    fun screenToWorld(sx: Double, sy: Double): Pair<Double, Double> {
        return Pair((sx - offsetX) / scale, (sy - offsetY) / scale)
    }

    fun pan(dx: Double, dy: Double) {
        offsetX += dx
        offsetY += dy
    }

    fun zoomBy(factor: Double, focusX: Double, focusY: Double) {
        offsetX = focusX - (focusX - offsetX) * factor
        offsetY = focusY - (focusY - offsetY) * factor
        scale *= factor
        scale = scale.coerceIn(0.001, 1_000_000.0)
    }

    fun viewportInWorld(screenWidth: Double, screenHeight: Double): ViewRect {
        val (left, top) = screenToWorld(0.0, 0.0)
        val (right, bottom) = screenToWorld(screenWidth, screenHeight)
        return ViewRect(left, top, right, bottom)
    }

    data class ViewRect(val left: Double, val top: Double, val right: Double, val bottom: Double)
}
