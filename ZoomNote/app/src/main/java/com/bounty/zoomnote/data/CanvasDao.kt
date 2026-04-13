package com.bounty.zoomnote.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CanvasDao {
    @Insert
    suspend fun insert(canvas: CanvasEntity)

    @Query("SELECT * FROM canvases ORDER BY modified_at DESC")
    suspend fun getAll(): List<CanvasEntity>

    @Query("SELECT * FROM canvases WHERE id = :id")
    suspend fun getById(id: String): CanvasEntity?

    @Update
    suspend fun update(canvas: CanvasEntity)

    @Query("DELETE FROM canvases WHERE id = :id")
    suspend fun deleteById(id: String)
}
