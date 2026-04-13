package com.spencerstiles.zoomnote.data

import com.spencerstiles.zoomnote.model.Point
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException

object PointCodec {

    fun encode(points: List<Point>): ByteArray {
        val baos = ByteArrayOutputStream(points.size * 28)
        val out = DataOutputStream(baos)
        out.writeInt(points.size)
        for (p in points) {
            out.writeDouble(p.x)
            out.writeDouble(p.y)
            out.writeFloat(p.pressure)
            out.writeLong(p.timestamp)
        }
        out.flush()
        return baos.toByteArray()
    }

    fun decode(bytes: ByteArray): List<Point> {
        if (bytes.isEmpty()) return emptyList()
        return try {
            val input = DataInputStream(ByteArrayInputStream(bytes))
            val count = input.readInt()
            List(count) {
                Point(
                    x = input.readDouble(),
                    y = input.readDouble(),
                    pressure = input.readFloat(),
                    timestamp = input.readLong()
                )
            }
        } catch (e: EOFException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
