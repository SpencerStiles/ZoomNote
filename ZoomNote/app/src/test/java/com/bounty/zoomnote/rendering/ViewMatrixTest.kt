package com.bounty.zoomnote.rendering

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewMatrixTest {

    @Test
    fun `identity transform maps world to screen unchanged`() {
        val vm = ViewMatrix()
        val (sx, sy) = vm.worldToScreen(100.0, 200.0)
        assertEquals(100.0, sx, 0.001)
        assertEquals(200.0, sy, 0.001)
    }

    @Test
    fun `pan offsets world coordinates`() {
        val vm = ViewMatrix()
        vm.pan(50.0, 30.0)
        val (sx, sy) = vm.worldToScreen(100.0, 200.0)
        assertEquals(150.0, sx, 0.001)
        assertEquals(230.0, sy, 0.001)
    }

    @Test
    fun `zoom scales around focus point`() {
        val vm = ViewMatrix()
        vm.zoomBy(2.0, 0.0, 0.0)
        val (sx, sy) = vm.worldToScreen(100.0, 200.0)
        assertEquals(200.0, sx, 0.001)
        assertEquals(400.0, sy, 0.001)
    }

    @Test
    fun `zoom around non-origin focus keeps focus fixed`() {
        val vm = ViewMatrix()
        vm.zoomBy(2.0, 100.0, 100.0)
        val worldPt = vm.screenToWorld(100.0, 100.0)
        val backToScreen = vm.worldToScreen(worldPt.first, worldPt.second)
        assertEquals(100.0, backToScreen.first, 0.001)
        assertEquals(100.0, backToScreen.second, 0.001)
    }

    @Test
    fun `screenToWorld is inverse of worldToScreen`() {
        val vm = ViewMatrix()
        vm.pan(33.0, -17.0)
        vm.zoomBy(3.5, 50.0, 50.0)
        val (sx, sy) = vm.worldToScreen(123.456, 789.012)
        val (wx, wy) = vm.screenToWorld(sx, sy)
        assertEquals(123.456, wx, 0.001)
        assertEquals(789.012, wy, 0.001)
    }

    @Test
    fun `viewportInWorld returns correct bounds`() {
        val vm = ViewMatrix()
        vm.zoomBy(2.0, 0.0, 0.0)
        val (left, top, right, bottom) = vm.viewportInWorld(800.0, 600.0)
        assertEquals(0.0, left, 0.001)
        assertEquals(0.0, top, 0.001)
        assertEquals(400.0, right, 0.001)
        assertEquals(300.0, bottom, 0.001)
    }

    @Test
    fun `zoom is clamped at minimum scale`() {
        val vm = ViewMatrix()
        // Zoom out by huge factor — should clamp at 0.001
        repeat(50) { vm.zoomBy(0.1, 0.0, 0.0) }
        assertTrue("scale should be at least 0.001", vm.scale >= 0.001)
    }

    @Test
    fun `zoom is clamped at maximum scale`() {
        val vm = ViewMatrix()
        // Zoom in by huge factor — should clamp at 1_000_000
        repeat(50) { vm.zoomBy(10.0, 0.0, 0.0) }
        assertTrue("scale should be at most 1_000_000", vm.scale <= 1_000_000.0)
    }
}
