package com.spencerstiles.zoomnote.model

import com.spencerstiles.zoomnote.data.PointCodec
import org.junit.Assert.assertEquals
import org.junit.Test

class PointCodecTest {

    @Test
    fun `encode and decode round-trips a list of points`() {
        val points = listOf(
            Point(100.0, 200.0, 0.5f, 1000L),
            Point(101.5, 202.3, 0.7f, 1016L),
            Point(103.0, 205.0, 0.9f, 1032L)
        )
        val encoded = PointCodec.encode(points)
        val decoded = PointCodec.decode(encoded)

        assertEquals(points.size, decoded.size)
        for (i in points.indices) {
            assertEquals(points[i].x, decoded[i].x, 0.001)
            assertEquals(points[i].y, decoded[i].y, 0.001)
            assertEquals(points[i].pressure, decoded[i].pressure, 0.001f)
            assertEquals(points[i].timestamp, decoded[i].timestamp)
        }
    }

    @Test
    fun `encode and decode handles empty list`() {
        val encoded = PointCodec.encode(emptyList())
        val decoded = PointCodec.decode(encoded)
        assertEquals(0, decoded.size)
    }

    @Test
    fun `encode and decode handles single point`() {
        val points = listOf(Point(0.0, 0.0, 1.0f, 0L))
        val decoded = PointCodec.decode(PointCodec.encode(points))
        assertEquals(1, decoded.size)
        assertEquals(0.0, decoded[0].x, 0.001)
    }

    @Test
    fun `decode returns empty list on corrupt data`() {
        val corrupt = ByteArray(5) { it.toByte() }
        val decoded = PointCodec.decode(corrupt)
        assertEquals(0, decoded.size)
    }
}
