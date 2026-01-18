package com.example.a2026scoutingapp.data

import android.content.Context
import android.net.Uri
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportUtils {

    fun exportAllAsZip(context: Context, outUri: Uri) {
        val files = MatchStorage.list(context)

        context.contentResolver.openOutputStream(outUri)?.use { os ->
            ZipOutputStream(os).use { zos ->
                files.forEach { file ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    zos.write(file.readBytes())
                    zos.closeEntry()
                }
            }
        } ?: error("Unable to open output stream")
    }
}
