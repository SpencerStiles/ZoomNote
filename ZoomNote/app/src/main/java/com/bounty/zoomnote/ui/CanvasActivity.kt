package com.bounty.zoomnote.ui

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bounty.zoomnote.ZoomNoteApp
import com.bounty.zoomnote.data.StrokeRepository
import com.bounty.zoomnote.rendering.ZoomCanvasView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class CanvasActivity : AppCompatActivity() {

    private lateinit var canvasView: ZoomCanvasView
    private lateinit var strokeRepository: StrokeRepository
    private lateinit var viewModel: CanvasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel = ViewModelProvider(this)[CanvasViewModel::class.java]

        val db = (application as ZoomNoteApp).database
        strokeRepository = StrokeRepository(db.strokeDao())

        val canvasId = UUID.fromString(
            intent.getStringExtra("canvas_id") ?: UUID.randomUUID().toString()
        )

        canvasView = ZoomCanvasView(this)
        canvasView.canvasId = canvasId

        canvasView.onStrokeSaved = { stroke ->
            lifecycleScope.launch(Dispatchers.IO) {
                strokeRepository.save(stroke)
            }
        }

        canvasView.onStrokeDeleted = { stroke ->
            lifecycleScope.launch(Dispatchers.IO) {
                strokeRepository.delete(stroke)
            }
        }

        val root = FrameLayout(this)
        root.addView(canvasView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        val toolbar = ToolbarView(this)
        val toolbarParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = 32
        }
        toolbar.onColorSelected = { color -> canvasView.activeColor = color }
        toolbar.onThicknessSelected = { thickness -> canvasView.activeThickness = thickness }
        toolbar.onEraserToggled = { erasing -> canvasView.eraserMode = erasing }
        toolbar.onUndoClicked = { canvasView.performUndo() }
        toolbar.onRedoClicked = { canvasView.performRedo() }
        root.addView(toolbar, toolbarParams)

        val minimap = MinimapView(this)
        val minimapSize = 150
        val minimapParams = FrameLayout.LayoutParams(minimapSize, minimapSize).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = 32
            marginEnd = 32
        }
        minimap.onNavigate = { wx, wy ->
            val screenCenterX = canvasView.width / 2.0
            val screenCenterY = canvasView.height / 2.0
            val (currentCenterX, currentCenterY) = canvasView.viewMatrix.screenToWorld(screenCenterX, screenCenterY)
            val dx = (wx - currentCenterX) * canvasView.viewMatrix.scale
            val dy = (wy - currentCenterY) * canvasView.viewMatrix.scale
            canvasView.viewMatrix.pan(-dx, -dy)
            canvasView.requestRender()
        }
        root.addView(minimap, minimapParams)

        setContentView(root)

        // Load strokes — use ViewModel cache on rotation
        if (viewModel.strokesLoaded) {
            canvasView.loadStrokes(viewModel.strokes)
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val strokes = strokeRepository.loadForCanvas(canvasId)
                viewModel.strokes = strokes
                viewModel.strokesLoaded = true
                launch(Dispatchers.Main) {
                    canvasView.loadStrokes(strokes)
                }
            }
        }
    }
}
