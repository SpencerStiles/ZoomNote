package com.bounty.zoomnote.data

import android.content.Context

// Stub — full implementation in Task 4 (Room Database Layer)
abstract class AppDatabase {
    companion object {
        fun create(context: Context): AppDatabase {
            throw UnsupportedOperationException("AppDatabase not yet implemented")
        }
    }
}
