package com.bounty.zoomnote.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strokes")
data class StrokeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "canvas_id") val canvasId: String,
    @ColumnInfo(name = "zoom_level") val zoomLevel: Double,
    val color: Int,
    val thickness: Float,
    val points: ByteArray,
    @ColumnInfo(name = "min_x") val minX: Double,
    @ColumnInfo(name = "min_y") val minY: Double,
    @ColumnInfo(name = "max_x") val maxX: Double,
    @ColumnInfo(name = "max_y") val maxY: Double,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long
)
