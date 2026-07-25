package com.debtfreein.app.data.logging

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val TAG = "FileLogger"
    private const val LOG_FILE_NAME = "DebtFreeIn_Debug_Log.txt"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private fun getLogFile(context: Context? = null): File {
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (publicDir != null && (publicDir.exists() || publicDir.mkdirs())) {
            return File(publicDir, LOG_FILE_NAME)
        }
        val externalDir = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (externalDir != null && (externalDir.exists() || externalDir.mkdirs())) {
            return File(externalDir, LOG_FILE_NAME)
        }
        val fallbackDir = context?.filesDir ?: File("/sdcard/Documents")
        if (!fallbackDir.exists()) fallbackDir.mkdirs()
        return File(fallbackDir, LOG_FILE_NAME)
    }

    @Synchronized
    fun log(tag: String, message: String, context: Context? = null) {
        val timestamp = dateFormat.format(Date())
        val logLine = "[$timestamp] [$tag] $message\n"

        // Output to Logcat
        Log.i(tag, message)

        try {
            val file = getLogFile(context)
            FileWriter(file, true).use { writer ->
                writer.append(logLine)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to $LOG_FILE_NAME", e)
        }
    }
}
