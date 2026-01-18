package com.example.a2026scoutingapp.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

object ExportUtils {

    fun exportToUsb(context: Context, outUri: Uri) {
        try {
            val files = MatchStorage.list(context)

            if (files.isEmpty()) {
                Toast.makeText(context, "No data to export!", Toast.LENGTH_SHORT).show()
                return
            }

            val outArr = JSONArray()

            for (file in files) {
                val root = JSONObject(MatchStorage.read(file))

                val flat = JSONObject()
                flat.put("header", root.optString("header"))

                val responses = root.optJSONObject("responses")
                if (responses != null) {
                    val keys = responses.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        flat.put(k, responses.get(k))
                    }
                }

                outArr.put(flat)
            }

            val os = context.contentResolver.openOutputStream(outUri)
            if (os == null) {
                Toast.makeText(context, "Error: Could not open USB file", Toast.LENGTH_SHORT).show()
                return
            }

            os.use {
                it.write(outArr.toString().toByteArray())
                it.flush()
            }

            Toast.makeText(
                context,
                "Export Successful! (${files.size} matches)",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
