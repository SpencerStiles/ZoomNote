package com.bounty.zoomnote.rendering

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.bounty.zoomnote.input.GestureHandler
import com.bounty.zoomnote.input.StylusHandler
import com.bounty.zoomnote.model.Point
import com.bounty.zoomnote.model.Stroke
import com.bounty.zoomnote.spatial.SpatialIndex
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.abs
import kotlin.math.log10

class ZoomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    val viewMatrix = ViewMatrix()
    private val spatialIndex = SpatialIndex()
    private val strokeRenderer = StrokeRenderer()

    // Lock protecting all mutable state shared with the render thread
    private val stateLock = ReentrantLock()

    private var activePoints = mutableListOf<Point>()
    var activeColor: Int = Color.BLACK
    var activeThickness: Float = 4f
    var eraserMode: Boolean = false

    private val strokeList = mutableListOf<Stroke>()
    private val undoStack = mutableListOf<Stroke>()

    var canvasId: UUID = UUID.randomUUID()

    var onStrokeSaved: ((Stroke) -> Unit)? = null
    var onStrokeDeleted: ((Stroke) -> Unit)? = null

    private var renderThread: RenderThread? = null
    private val renderLock = Object()

    // Paints for overlay elements
    private val dotPaint = Paint().apply {
        color = Color.argb(40, 150, 150, 150)
        style = Paint.Style.FILL
    }
    private val zoomTextPaint = Paint().apply {
        color = Color.argb(160, 80, 80, 80)
        textSize = 36f
        isAntiAlias = true
    }

    // Hit test radius for eraser: 20dp
    private val eraserRadiusDp = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics
    ).toDouble()

    private val stylusHandler = StylusHandler(
        viewMatrix = viewMatrix,
        onStrokeStarted = {
            stateLock.withLock { activePoints.clear() }
        },
        onPointAdded = { point ->
            stateLock.withLock { activePoints.add(point) }
            requestRender()
        },
        onStrokeFinished = { finalizeStroke() },
        onStrokeCancelled = { stateLock.withLock { activePoints.clear() }; requestRender() },
        onSPenButtonUndo = { undo() }
    )

    private val gestureHandler = GestureHandler(
        viewMatrix = viewMatrix,
        onViewChanged = { requestRender() },
        onUndo = { undo() },
        onRedo = { redo() }
    )

    init {
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setWillNotDraw(false)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        renderThread = RenderThread(holder).also { it.start() }
        requestRender()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        requestRender()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        val thread = renderThread ?: return
        thread.running = false
        synchronized(renderLock) { renderLock.notifyAll() }
        thread.join()
        renderThread = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (eraserMode && event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                handleEraserTap(event.x.toDouble(), event.y.toDouble())
                return true
            }
            return true
        }
        if (stylusHandler.onTouchEvent(event)) return true
        if (gestureHandler.onTouchEvent(event)) return true
        return super.onTouchEvent(event)
    }

    fun loadStrokes(strokes: List<Stroke>) {
        stateLock.withLock {
            strokeList.clear()
            strokeList.addAll(strokes)
            spatialIndex.loadStrokes(strokes)
        }
        requestRender()
    }

    fun requestRender() {
        synchronized(renderLock) { renderLock.notifyAll() }
    }

    fun performUndo() = undo()
    fun performRedo() = redo()

    private fun finalizeStroke() {
        val pointsCopy = stateLock.withLock {
            val copy = activePoints.toList()
            activePoints.clear()
            copy
        }
        if (pointsCopy.size < 2) {
            requestRender()
            return
        }
        val stroke = Stroke(
            canvasId = canvasId,
            zoomLevel = viewMatrix.scale,
            color = activeColor,
            thickness = activeThickness,
            points = pointsCopy
        )
        stateLock.withLock {
            strokeList.add(stroke)
            spatialIndex.add(stroke)
            undoStack.clear()
        }
        onStrokeSaved?.invoke(stroke)
        requestRender()
    }

    private fun undo() {
        val stroke = stateLock.withLock {
            if (strokeList.isEmpty()) return
            val s = strokeList.removeAt(strokeList.lastIndex)
            spatialIndex.remove(s)
            undoStack.add(s)
            s
        }
        onStrokeDeleted?.invoke(stroke)
        requestRender()
    }

    private fun redo() {
        val stroke = stateLock.withLock {
            if (undoStack.isEmpty()) return
            val s = undoStack.removeAt(undoStack.lastIndex)
            strokeList.add(s)
            spatialIndex.add(s)
            s
        }
        onStrokeSaved?.invoke(stroke)
        requestRender()
    }

    private fun handleEraserTap(screenX: Double, screenY: Double) {
        val (worldX, worldY) = viewMatrix.screenToWorld(screenX, screenY)
        val eraserRadiusWorld = eraserRadiusDp / viewMatrix.scale

        val candidates = stateLock.withLock {
            spatialIndex.query(
                worldX - eraserRadiusWorld, worldY - eraserRadiusWorld,
                worldX + eraserRadiusWorld, worldY + eraserRadiusWorld,
                viewMatrix.scale
            ).toList()
        }

        val hit = candidates.firstOrNull { stroke ->
            stroke.points.any { p ->
                val dx = p.x - worldX
                val dy = p.y - worldY
                dx * dx + dy * dy <= eraserRadiusWorld * eraserRadiusWorld
            }
        } ?: return

        stateLock.withLock {
            strokeList.remove(hit)
            spatialIndex.remove(hit)
            undoStack.clear()
        }
        onStrokeDeleted?.invoke(hit)
        requestRender()
    }

    private inner class RenderThread(private val surfaceHolder: SurfaceHolder) : Thread("ZoomNote-Render") {
        @Volatile var running = true

        override fun run() {
            while (running) {
                synchronized(renderLock) {
                    renderLock.wait(100) // max 100ms wait
                }
                if (!running) break

                var canvas: Canvas? = null
                try {
                    canvas = surfaceHolder.lockCanvas() ?: continue
                    drawFrame(canvas)
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
            }
        }
    }

    private fun drawFrame(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)

        val currentScale = viewMatrix.scale
        drawDotGrid(canvas, currentScale)

        // Take snapshot under lock — render outside lock to minimize contention
        val snapshot: List<Stroke>
        val activeSnap: List<Point>
        stateLock.withLock {
            snapshot = strokeList.toList()
            activeSnap = activePoints.toList()
        }

        val viewport = viewMatrix.viewportInWorld(width.toDouble(), height.toDouble())
        val visible = spatialIndex.query(
            viewport.left, viewport.top, viewport.right, viewport.bottom,
            currentZoom = currentScale
        )

        for (stroke in visible) {
            // Zoom fade: compute alpha based on proximity to zoom band boundary
            val alpha = computeZoomFadeAlpha(stroke.zoomLevel, currentScale)
            strokeRenderer.draw(canvas, stroke, viewMatrix, alpha)
        }

        if (activeSnap.size >= 2) {
            strokeRenderer.drawActiveStroke(canvas, activeSnap, activeColor, activeThickness, viewMatrix)
        }

        drawZoomIndicator(canvas, currentScale)
    }

    private fun drawDotGrid(canvas: Canvas, scale: Double) {
        // Grid spacing adapts to zoom: base 100 world units, shown when visible
        val baseSpacing = 100.0
        val screenSpacing = (baseSpacing * scale).toFloat()
        if (screenSpacing < 20f || screenSpacing > 400f) return

        val (leftW, topW) = viewMatrix.screenToWorld(0.0, 0.0)
        val (rightW, bottomW) = viewMatrix.screenToWorld(width.toDouble(), height.toDouble())

        val startX = Math.floor(leftW / baseSpacing) * baseSpacing
        val startY = Math.floor(topW / baseSpacing) * baseSpacing

        var wx = startX
        while (wx <= rightW + baseSpacing) {
            var wy = startY
            while (wy <= bottomW + baseSpacing) {
                val (sx, sy) = viewMatrix.worldToScreen(wx, wy)
                canvas.drawCircle(sx.toFloat(), sy.toFloat(), 2f, dotPaint)
                wy += baseSpacing
            }
            wx += baseSpacing
        }
    }

    private fun computeZoomFadeAlpha(strokeZoom: Double, currentZoom: Double): Int {
        if (strokeZoom <= 0.0 || currentZoom <= 0.0) return 255
        val logDiff = abs(log10(strokeZoom) - log10(currentZoom))
        val bandLimit = 2.0
        val fadeStart = bandLimit * 0.8
        return when {
            logDiff <= fadeStart -> 255
            logDiff >= bandLimit -> 0
            else -> {
                val fadeProgress = (logDiff - fadeStart) / (bandLimit - fadeStart)
                ((1.0 - fadeProgress) * 255).toInt()
            }
        }
    }

    private fun drawZoomIndicator(canvas: Canvas, scale: Double) {
        val text = "%.1fx".format(scale)
        canvas.drawText(text, 32f, height - 32f, zoomTextPaint)
    }
}
