package com.taskermobile.util

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.taskermobile.data.model.ProjectReport
import java.io.ByteArrayOutputStream

/**
 * Generates a PDF document from the given ProjectReport.
 * This simple implementation writes a header and then each task.
 */
fun generatePdf(report: ProjectReport): ByteArray {
    val pdfDocument = PdfDocument()
    var currentPageNumber = 1
    // Create the first page with A4 dimensions (595 x 842 points)
    var pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    // Prepare a Paint object for drawing text
    val paint = Paint().apply { textSize = 14f }
    var xPos = 50f
    var yPos = 50f

    // Draw report header
    canvas.drawText("Report #${report.id}", xPos, yPos, paint)
    yPos += 30f
    canvas.drawText("Date: ${report.reportDate ?: "N/A"}", xPos, yPos, paint)
    yPos += 30f
    canvas.drawText("Performance: ${report.overallPerformance ?: "N/A"}", xPos, yPos, paint)
    yPos += 50f

    // Draw task list header
    canvas.drawText("Tasks:", xPos, yPos, paint)
    yPos += 30f

    // Draw each task (adjust formatting as needed)
    report.tasks.forEachIndexed { index, task ->
        val taskLine = "${index + 1}. ${task.title} - ${task.status}"
        // Check if yPos is near the bottom of the page
        if (yPos > 800) {
            // Finish current page and start a new one
            pdfDocument.finishPage(page)
            currentPageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 50f
        }
        canvas.drawText(taskLine, xPos, yPos, paint)
        yPos += 20f
    }

    // Finish the last page and write the PDF to a byte array
    pdfDocument.finishPage(page)
    val outputStream = ByteArrayOutputStream()
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()
    return outputStream.toByteArray()
}
