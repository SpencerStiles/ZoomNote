package com.spencerstiles.zoomnote.spatial

import com.spencerstiles.zoomnote.model.Stroke
import com.github.davidmoten.rtree2.Entries
import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.Rectangle
import java.util.UUID
import kotlin.math.abs
import kotlin.math.log10

class SpatialIndex {

    private var tree: RTree<UUID, Rectangle> = RTree.create()
    private val strokes: MutableMap<UUID, Stroke> = mutableMapOf()

    // Strokes within 2 orders of magnitude of current zoom are visible
    private val zoomBandOrders = 2.0

    fun add(stroke: Stroke) {
        strokes[stroke.id] = stroke
        tree = tree.add(stroke.id, rect(stroke))
    }

    fun remove(stroke: Stroke) {
        strokes.remove(stroke.id)
        tree = tree.delete(stroke.id, rect(stroke))
    }

    fun loadStrokes(strokes: List<Stroke>) {
        this.strokes.clear()
        for (s in strokes) {
            this.strokes[s.id] = s
        }
        // Bulk-load using RTree.create(entries) — O(n log n) vs O(n²) for sequential adds
        val entries: List<Entry<UUID, Rectangle>> = strokes.map { s ->
            Entries.entry(s.id, rect(s))
        }
        tree = RTree.create(entries)
    }

    fun query(
        left: Double, top: Double, right: Double, bottom: Double,
        currentZoom: Double
    ): List<Stroke> {
        val searchRect = Geometries.rectangle(left, top, right, bottom)
        val entries: Iterable<Entry<UUID, Rectangle>> = tree.search(searchRect)

        return entries.mapNotNull { entry ->
            val stroke = strokes[entry.value()] ?: return@mapNotNull null
            if (isVisibleAtZoom(stroke.zoomLevel, currentZoom)) stroke else null
        }
    }

    fun clear() {
        tree = RTree.create()
        strokes.clear()
    }

    private fun isVisibleAtZoom(strokeZoom: Double, currentZoom: Double): Boolean {
        if (strokeZoom <= 0.0 || currentZoom <= 0.0) return true
        val logDiff = abs(log10(strokeZoom) - log10(currentZoom))
        return logDiff <= zoomBandOrders
    }

    private fun rect(stroke: Stroke): Rectangle {
        // Handle degenerate case (single point or zero-size bounding box)
        val minX = stroke.minX
        val minY = stroke.minY
        val maxX = if (stroke.maxX > minX) stroke.maxX else minX + 0.001
        val maxY = if (stroke.maxY > minY) stroke.maxY else minY + 0.001
        return Geometries.rectangle(minX, minY, maxX, maxY)
    }
}
