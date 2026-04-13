package com.spencerstiles.zoomnote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StrokeEntity::class, CanvasEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun strokeDao(): StrokeDao
    abstract fun canvasDao(): CanvasDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "zoomnote.db"
            ).build()
        }
    }
}
