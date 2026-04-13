package com.spencerstiles.zoomnote.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout

class ToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    var onColorSelected: ((Int) -> Unit)? = null
    var onThicknessSelected: ((Float) -> Unit)? = null
    var onEraserToggled: ((Boolean) -> Unit)? = null
    var onUndoClicked: (() -> Unit)? = null
    var onRedoClicked: (() -> Unit)? = null

    private var isErasing = false
    private var selectedColorIndex = 0
    private var selectedThicknessIndex = 1 // default: medium

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
    private val thicknessButtons = mutableListOf<ImageButton>()
    private var eraserBtn: ImageButton? = null

    private fun dpToPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics
    ).toInt()

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.argb(230, 245, 245, 245))
        elevation = 12f

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padH = dpToPx(12)
            val padV = dpToPx(10)
            setPadding(padH, padV, padH, padV)
        }

        val btnSize = dpToPx(48)
        val btnMargin = dpToPx(6)

        // Color buttons
        for ((i, color) in colors.withIndex()) {
            val btn = ImageButton(context).apply {
                val displayColor = if (color == Color.WHITE) Color.parseColor("#DDDDDD") else color
                setBackgroundColor(displayColor)
                layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply { marginEnd = btnMargin }
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
            row.addView(btn)
        }

        // Spacer
        row.addView(LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(16), 1)
        })

        // Thickness buttons
        for ((i, t) in thicknesses.withIndex()) {
            val btn = ImageButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply { marginEnd = btnMargin }
                setBackgroundColor(Color.parseColor("#444444"))
                scaleY = (t / thicknesses.last()).coerceIn(0.3f, 1.0f)
                contentDescription = "${thicknessNames[i]} thickness"
                setOnClickListener {
                    selectedThicknessIndex = i
                    isErasing = false
                    onEraserToggled?.invoke(false)
                    onThicknessSelected?.invoke(t)
                    updateSelection()
                }
            }
            thicknessButtons.add(btn)
            row.addView(btn)
        }

        // Spacer
        row.addView(LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(16), 1)
        })

        // Eraser
        val eraser = ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply { marginEnd = btnMargin }
            setBackgroundColor(Color.parseColor("#FDD835"))
            contentDescription = "Eraser"
            setOnClickListener {
                isErasing = !isErasing
                onEraserToggled?.invoke(isErasing)
                updateSelection()
            }
        }
        eraserBtn = eraser
        row.addView(eraser)

        // Undo
        row.addView(ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply { marginEnd = btnMargin }
            setBackgroundColor(Color.parseColor("#BDBDBD"))
            contentDescription = "Undo"
            setOnClickListener { onUndoClicked?.invoke() }
        })

        // Redo
        row.addView(ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(btnSize, btnSize)
            setBackgroundColor(Color.parseColor("#BDBDBD"))
            contentDescription = "Redo"
            setOnClickListener { onRedoClicked?.invoke() }
        })

        addView(row)
        updateSelection()
    }

    private fun updateSelection() {
        for ((i, btn) in colorButtons.withIndex()) {
            val selected = i == selectedColorIndex && !isErasing
            btn.alpha = if (selected) 1.0f else 0.45f
            btn.scaleX = if (selected) 1.15f else 1.0f
            btn.scaleY = if (selected) 1.15f else 1.0f
        }
        for ((i, btn) in thicknessButtons.withIndex()) {
            val selected = i == selectedThicknessIndex && !isErasing
            btn.alpha = if (selected) 1.0f else 0.45f
            btn.scaleX = if (selected) 1.15f else 1.0f
        }
        eraserBtn?.alpha = if (isErasing) 1.0f else 0.55f
        eraserBtn?.scaleX = if (isErasing) 1.15f else 1.0f
        eraserBtn?.scaleY = if (isErasing) 1.15f else 1.0f
    }
}
