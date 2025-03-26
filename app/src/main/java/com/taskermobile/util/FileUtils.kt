package com.taskermobile.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

/**
 * Saves the provided PDF bytes into the app’s external Downloads directory.
 * Returns true if successful.
 */
fun savePdfToStorage(pdfBytes: ByteArray, context: Context): Boolean {
    // Use the app-specific external files directory for downloads.
    val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    return if (downloadsDir != null && (downloadsDir.exists() || downloadsDir.mkdirs())) {
        // Create a unique filename
        val fileName = "project_report_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)
        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(pdfBytes)
            }
            Toast.makeText(context, "Report saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving report: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    } else {
        Toast.makeText(context, "Unable to access downloads directory", Toast.LENGTH_LONG).show()
        false
    }
}
