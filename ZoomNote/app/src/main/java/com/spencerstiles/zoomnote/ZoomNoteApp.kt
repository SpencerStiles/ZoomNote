package com.spencerstiles.zoomnote

import android.app.Application
import com.spencerstiles.zoomnote.data.AppDatabase

class ZoomNoteApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
}
