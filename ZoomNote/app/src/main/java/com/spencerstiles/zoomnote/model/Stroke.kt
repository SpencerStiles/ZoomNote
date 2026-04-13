package com.spencerstiles.zoomnote.model

import java.util.UUID

data class Stroke(
    val id: UUID = UUID.randomUUID(),
    val canvasId: UUID,
    val zoomLevel: Double,
    val color: Int,
    val thickness: Float,
    val points: List<Point>,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
) {
    val minX: Double get() = if (points.isEmpty()) 0.0 else points.minOf { it.x }
    val minY: Double get() = if (points.isEmpty()) 0.0 else points.minOf { it.y }
    val maxX: Double get() = if (points.isEmpty()) 0.0 else points.maxOf { it.x }
    val maxY: Double get() = if (points.isEmpty()) 0.0 else points.maxOf { it.y }
}
