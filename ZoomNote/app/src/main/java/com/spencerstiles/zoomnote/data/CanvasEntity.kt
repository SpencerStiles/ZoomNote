package com.spencerstiles.zoomnote.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "canvases")
data class CanvasEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long
)
