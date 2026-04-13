package com.bounty.zoomnote.data

import com.bounty.zoomnote.model.Stroke
import java.util.UUID

class StrokeRepository(private val strokeDao: StrokeDao) {

    suspend fun save(stroke: Stroke) {
        strokeDao.insert(toEntity(stroke))
    }

    suspend fun delete(stroke: Stroke) {
        strokeDao.delete(toEntity(stroke))
    }

    suspend fun loadForCanvas(canvasId: UUID): List<Stroke> {
        return strokeDao.getByCanvasId(canvasId.toString()).map { fromEntity(it) }
    }

    suspend fun deleteForCanvas(canvasId: UUID) {
        strokeDao.deleteByCanvasId(canvasId.toString())
    }

    private fun toEntity(stroke: Stroke): StrokeEntity {
        return StrokeEntity(
            id = stroke.id.toString(),
            canvasId = stroke.canvasId.toString(),
            zoomLevel = stroke.zoomLevel,
            color = stroke.color,
            thickness = stroke.thickness,
            points = PointCodec.encode(stroke.points),
            minX = stroke.minX,
            minY = stroke.minY,
            maxX = stroke.maxX,
            maxY = stroke.maxY,
            createdAt = stroke.createdAt,
            modifiedAt = stroke.modifiedAt
        )
    }

    private fun fromEntity(entity: StrokeEntity): Stroke {
        return Stroke(
            id = UUID.fromString(entity.id),
            canvasId = UUID.fromString(entity.canvasId),
            zoomLevel = entity.zoomLevel,
            color = entity.color,
            thickness = entity.thickness,
            points = PointCodec.decode(entity.points),
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}
