package com.bounty.zoomnote.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout

class ToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var onColorSelected: ((Int) -> Unit)? = null
    var onThicknessSelected: ((Float) -> Unit)? = null
    var onEraserToggled: ((Boolean) -> Unit)? = null
    var onUndoClicked: (() -> Unit)? = null
    var onRedoClicked: (() -> Unit)? = null

    private var isErasing = false
    private var selectedColorIndex = 0

    private val colors = intArrayOf(
        Color.BLACK,
        Color.parseColor("#E53935"),
        Color.parseColor("#1E88E5"),
        Color.parseColor("#43A047"),
        Color.parseColor("#FB8C00"),
        Color.parseColor("#8E24AA"),
        Color.parseColor("#546E7A"),
        Color.WHITE
    )
    private val colorNames = arrayOf("Black", "Red", "Blue", "Green", "Orange", "Purple", "Gray", "White")

    private val thicknesses = floatArrayOf(2f, 4f, 8f)
    private val thicknessNames = arrayOf("Fine", "Medium", "Bold")

    private val colorButtons = mutableListOf<ImageButton>()

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(16, 8, 16, 8)
        setBackgroundColor(Color.argb(200, 240, 240, 240))
        elevation = 8f

        // Color buttons
        for ((i, color) in colors.withIndex()) {
            val btn = ImageButton(context).apply {
                val displayColor = if (color == Color.WHITE) Color.LTGRAY else color
                setBackgroundColor(displayColor)
                val size = 48
                layoutParams = LayoutParams(size, size).apply { marginEnd = 8 }
                contentDescription = "${colorNames[i]} pen"
                setOnClickListener {
                    selectedColorIndex = i
                    isErasing = false
                    onEraserToggled?.invoke(false)
                    onColorSelected?.invoke(colors[i])
                    updateSelection()
                }
            }
            colorButtons.add(btn)
            addView(btn)
        }

        // Spacer
        addView(LinearLayout(context).apply { layoutParams = LayoutParams(24, 1) })

        // Thickness buttons
        for ((i, t) in thicknesses.withIndex()) {
            val btn = ImageButton(context).apply {
                layoutParams = LayoutParams(48, 48).apply { marginEnd = 8 }
                setBackgroundColor(Color.DKGRAY)
                scaleX = t / thicknesses.last()
                scaleY = t / thicknesses.last()
                contentDescription = "${thicknessNames[i]} thickness"
                setOnClickListener { onThicknessSelected?.invoke(t) }
            }
            addView(btn)
        }

        // Spacer
        addView(LinearLayout(context).apply { layoutParams = LayoutParams(24, 1) })

        // Eraser
        val eraserBtn = ImageButton(context).apply {
            layoutParams = LayoutParams(48, 48).apply { marginEnd = 8 }
            setBackgroundColor(Color.YELLOW)
            contentDescription = "Eraser"
            setOnClickListener {
                isErasing = !isErasing
                onEraserToggled?.invoke(isErasing)
                updateSelection()
            }
        }
        addView(eraserBtn)

        // Undo
        val undoBtn = ImageButton(context).apply {
            layoutParams = LayoutParams(48, 48).apply { marginEnd = 8 }
            setBackgroundColor(Color.LTGRAY)
            contentDescription = "Undo"
            setOnClickListener { onUndoClicked?.invoke() }
        }
        addView(undoBtn)

        // Redo
        val redoBtn = ImageButton(context).apply {
            layoutParams = LayoutParams(48, 48)
            setBackgroundColor(Color.LTGRAY)
            contentDescription = "Redo"
            setOnClickListener { onRedoClicked?.invoke() }
        }
        addView(redoBtn)

        updateSelection()
    }

    private fun updateSelection() {
        for ((i, btn) in colorButtons.withIndex()) {
            btn.alpha = if (i == selectedColorIndex && !isErasing) 1.0f else 0.4f
        }
    }
}
