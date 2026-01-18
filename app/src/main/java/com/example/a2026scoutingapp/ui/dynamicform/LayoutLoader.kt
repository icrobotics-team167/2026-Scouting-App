package com.example.a2026scoutingapp.ui.dynamicform

import android.content.Context
import org.json.JSONObject

object LayoutLoader {
    fun loadJsonObjectFromAssets(context: Context, assetName: String): JSONObject {
        val text = context.assets.open(assetName)
            .bufferedReader()
            .use { it.readText() }
        return JSONObject(text)
    }
}
