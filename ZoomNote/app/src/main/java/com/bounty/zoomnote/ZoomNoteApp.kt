package com.bounty.zoomnote

import android.app.Application
import com.bounty.zoomnote.data.AppDatabase

class ZoomNoteApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
}
