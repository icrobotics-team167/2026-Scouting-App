package com.example.a2026scoutingapp.data

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MatchStorage {

    private const val DIR_NAME = "matches"

    private fun dir(context: Context): File {
        val d = File(context.filesDir, DIR_NAME)
        if (!d.exists()) d.mkdirs()
        return d
    }

    fun save(context: Context, header: String, json: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeHeader = header
            .replace(Regex("[^a-zA-Z0-9 _-]"), "")
            .take(40)
            .ifBlank { "match" }

        val file = File(dir(context), "${timestamp}_$safeHeader.json")
        file.writeText(json)
        return file
    }

    fun list(context: Context): List<File> =
        dir(context).listFiles()?.filter { it.extension == "json" } ?: emptyList()

    fun read(file: File): String = file.readText()

    fun delete(file: File): Boolean = file.delete()
}
