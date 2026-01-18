package com.example.a2026scoutingapp.ui.dynamicform

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import org.json.JSONArray
import org.json.JSONObject

object ValueStore {

    fun createWithDefaults(layoutJson: JSONObject): SnapshotStateMap<String, Any> {
        val map = mutableStateMapOf<String, Any>()
        val categories = layoutJson.getJSONArray("categories")

        for (i in 0 until categories.length()) {
            val cat = categories.getJSONObject(i)
            val rows = cat.getJSONArray("rows")
            for (r in 0 until rows.length()) {
                val row = rows.getJSONObject(r)
                val widgets = row.getJSONArray("widgets")
                for (w in 0 until widgets.length()) {
                    val widget = widgets.getJSONObject(w)
                    val id = widget.getString("id")
                    map[id] = defaultFor(widget)
                }
            }
        }

        return map
    }

    private fun defaultFor(widget: JSONObject): Any {
        if (widget.has("default")) {
            return widget.get("default")
        }

        return when (widget.getString("type")) {
            "checkbox" -> false
            "counter" -> 0
            "text" -> ""
            "dropdown" -> widget.optJSONArray("options")?.optString(0) ?: ""
            "slider" -> widget.optDouble("sliderMin", 0.0).toFloat()
            else -> ""
        }
    }

    fun exportAsJson(values: Map<String, Any>): String {
        val obj = JSONObject()
        values.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString(2)
    }
}

// Helper getters/setters
fun SnapshotStateMap<String, Any>.bool(id: String) = (this[id] as? Boolean) ?: false
fun SnapshotStateMap<String, Any>.setBool(id: String, v: Boolean) { this[id] = v }

fun SnapshotStateMap<String, Any>.int(id: String) = (this[id] as? Int) ?: (this[id] as? Number)?.toInt() ?: 0
fun SnapshotStateMap<String, Any>.setInt(id: String, v: Int) { this[id] = v }

fun SnapshotStateMap<String, Any>.string(id: String) = (this[id] as? String) ?: ""
fun SnapshotStateMap<String, Any>.setString(id: String, v: String) { this[id] = v }

fun SnapshotStateMap<String, Any>.float(id: String) =
    (this[id] as? Float) ?: (this[id] as? Double)?.toFloat() ?: (this[id] as? Number)?.toFloat() ?: 0f
fun SnapshotStateMap<String, Any>.setFloat(id: String, v: Float) { this[id] = v }
