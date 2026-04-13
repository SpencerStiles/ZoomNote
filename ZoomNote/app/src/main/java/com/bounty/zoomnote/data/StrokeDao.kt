package com.bounty.zoomnote.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query

@Dao
interface StrokeDao {
    @Insert
    suspend fun insert(stroke: StrokeEntity)

    @Delete
    suspend fun delete(stroke: StrokeEntity)

    @Query("SELECT * FROM strokes WHERE canvas_id = :canvasId")
    suspend fun getByCanvasId(canvasId: String): List<StrokeEntity>

    @Query("DELETE FROM strokes WHERE canvas_id = :canvasId")
    suspend fun deleteByCanvasId(canvasId: String)
}
