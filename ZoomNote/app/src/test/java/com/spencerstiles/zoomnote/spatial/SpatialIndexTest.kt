package com.spencerstiles.zoomnote.spatial

import com.spencerstiles.zoomnote.model.Point
import com.spencerstiles.zoomnote.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SpatialIndexTest {

    private val canvasId = UUID.randomUUID()

    private fun stroke(
        x1: Double, y1: Double, x2: Double, y2: Double,
        zoomLevel: Double = 1.0
    ): Stroke {
        return Stroke(
            canvasId = canvasId,
            zoomLevel = zoomLevel,
            color = 0xFF000000.toInt(),
            thickness = 2f,
            points = listOf(
                Point(x1, y1, 0.5f, 0L),
                Point(x2, y2, 0.5f, 16L)
            )
        )
    }

    @Test
    fun `query returns strokes inside viewport`() {
        val index = SpatialIndex()
        val s1 = stroke(10.0, 10.0, 20.0, 20.0)
        val s2 = stroke(500.0, 500.0, 510.0, 510.0)
        index.add(s1)
        index.add(s2)

        val results = index.query(0.0, 0.0, 100.0, 100.0, currentZoom = 1.0)
        assertEquals(1, results.size)
        assertEquals(s1.id, results[0].id)
    }

    @Test
    fun `query returns empty for empty viewport`() {
        val index = SpatialIndex()
        index.add(stroke(10.0, 10.0, 20.0, 20.0))
        val results = index.query(200.0, 200.0, 300.0, 300.0, currentZoom = 1.0)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `zoom-level filtering hides strokes drawn at much deeper zoom`() {
        val index = SpatialIndex()
        val deepStroke = stroke(10.0, 10.0, 20.0, 20.0, zoomLevel = 1000.0)
        index.add(deepStroke)

        val results = index.query(0.0, 0.0, 100.0, 100.0, currentZoom = 1.0)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `zoom-level filtering shows strokes drawn at similar zoom`() {
        val index = SpatialIndex()
        val nearStroke = stroke(10.0, 10.0, 20.0, 20.0, zoomLevel = 2.0)
        index.add(nearStroke)

        val results = index.query(0.0, 0.0, 100.0, 100.0, currentZoom = 1.0)
        assertEquals(1, results.size)
    }

    @Test
    fun `remove deletes stroke from index`() {
        val index = SpatialIndex()
        val s = stroke(10.0, 10.0, 20.0, 20.0)
        index.add(s)
        index.remove(s)
        val results = index.query(0.0, 0.0, 100.0, 100.0, currentZoom = 1.0)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `loadStrokes bulk-loads all strokes`() {
        val index = SpatialIndex()
        val strokes = (1..100).map { i ->
            stroke(i.toDouble(), i.toDouble(), (i + 1).toDouble(), (i + 1).toDouble())
        }
        index.loadStrokes(strokes)
        val results = index.query(0.0, 0.0, 200.0, 200.0, currentZoom = 1.0)
        assertEquals(100, results.size)
    }
}
