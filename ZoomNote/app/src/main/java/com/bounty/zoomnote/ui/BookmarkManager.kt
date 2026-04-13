package com.bounty.zoomnote.ui

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Bookmark(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val worldX: Double,
    val worldY: Double,
    val zoom: Double
)

class BookmarkManager(context: Context, private val canvasId: String) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "bookmarks_$canvasId", Context.MODE_PRIVATE
    )

    fun save(bookmark: Bookmark) {
        persist(loadAll().toMutableList().also { it.add(bookmark) })
    }

    fun delete(bookmarkId: String) {
        persist(loadAll().filter { it.id != bookmarkId })
    }

    fun loadAll(): List<Bookmark> {
        val json = prefs.getString("bookmarks", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Bookmark(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    worldX = obj.getDouble("worldX"),
                    worldY = obj.getDouble("worldY"),
                    zoom = obj.getDouble("zoom")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persist(bookmarks: List<Bookmark>) {
        val arr = JSONArray()
        for (b in bookmarks) {
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("name", b.name)
                put("worldX", b.worldX)
                put("worldY", b.worldY)
                put("zoom", b.zoom)
            })
        }
        prefs.edit().putString("bookmarks", arr.toString()).apply()
    }
}
