package com.spencerstiles.zoomnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.spencerstiles.zoomnote.ZoomNoteApp
import com.spencerstiles.zoomnote.data.CanvasEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CanvasListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var emptyView: TextView
    private val canvasList = mutableListOf<CanvasEntity>()
    private lateinit var adapter: CanvasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "ZoomNote"
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        header.addView(title)

        val newBtn = Button(this).apply {
            text = "+"
            contentDescription = "New canvas"
            setOnClickListener { showNewCanvasDialog() }
        }
        header.addView(newBtn)
        root.addView(header)

        // Empty state
        emptyView = TextView(this).apply {
            text = "No canvases yet. Tap + to start."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 64, 0, 0)
            visibility = View.GONE
        }
        root.addView(emptyView)

        listView = ListView(this)
        adapter = CanvasAdapter()
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            openCanvas(canvasList[position].id)
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            showDeleteDialog(canvasList[position])
            true
        }
        root.addView(listView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ))

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        loadCanvases()
    }

    private fun loadCanvases() {
        val db = (application as ZoomNoteApp).database
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.canvasDao().getAll()
            launch(Dispatchers.Main) {
                canvasList.clear()
                canvasList.addAll(list)
                adapter.notifyDataSetChanged()
                emptyView.visibility = if (canvasList.isEmpty()) View.VISIBLE else View.GONE
                listView.visibility = if (canvasList.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showNewCanvasDialog() {
        val input = EditText(this).apply {
            hint = "Canvas name"
            setPadding(32, 16, 32, 16)
        }
        AlertDialog.Builder(this)
            .setTitle("New Canvas")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                createCanvas(input.text.toString().ifBlank { "Untitled" })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createCanvas(name: String) {
        val db = (application as ZoomNoteApp).database
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO) {
            db.canvasDao().insert(CanvasEntity(id, name, now, now))
            launch(Dispatchers.Main) { openCanvas(id) }
        }
    }

    private fun openCanvas(canvasId: String) {
        startActivity(Intent(this, CanvasActivity::class.java).putExtra("canvas_id", canvasId))
    }

    private fun showDeleteDialog(canvas: CanvasEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete \"${canvas.name}\"?")
            .setMessage("This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val db = (application as ZoomNoteApp).database
                lifecycleScope.launch(Dispatchers.IO) {
                    db.canvasDao().deleteById(canvas.id)
                    db.strokeDao().deleteByCanvasId(canvas.id)
                    launch(Dispatchers.Main) { loadCanvases() }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private inner class CanvasAdapter : BaseAdapter() {
        private val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())

        override fun getCount() = canvasList.size
        override fun getItem(position: Int) = canvasList[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val canvas = canvasList[position]
            val layout = (convertView as? LinearLayout) ?: LinearLayout(this@CanvasListActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }
            layout.removeAllViews()
            layout.addView(TextView(this@CanvasListActivity).apply {
                text = canvas.name
                textSize = 18f
            })
            layout.addView(TextView(this@CanvasListActivity).apply {
                text = dateFormat.format(Date(canvas.modifiedAt))
                textSize = 12f
            })
            return layout
        }
    }
}
