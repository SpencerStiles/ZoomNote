package com.bounty.zoomnote.ui

import androidx.lifecycle.ViewModel
import com.bounty.zoomnote.model.Stroke

class CanvasViewModel : ViewModel() {
    var strokes: List<Stroke> = emptyList()
    var strokesLoaded: Boolean = false
}
