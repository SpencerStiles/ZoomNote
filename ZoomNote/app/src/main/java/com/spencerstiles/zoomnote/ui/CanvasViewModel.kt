package com.spencerstiles.zoomnote.ui

import androidx.lifecycle.ViewModel
import com.spencerstiles.zoomnote.model.Stroke

class CanvasViewModel : ViewModel() {
    var strokes: List<Stroke> = emptyList()
    var strokesLoaded: Boolean = false
}
