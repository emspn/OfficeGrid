package com.app.officegrid.tasks.presentation.task_list

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.app.officegrid.tasks.domain.model.Task
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportHelper {

    fun generateAndSharePdf(
        context: Context,
        tasks: List<Task>,
        timelineFilter: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Create PDF document
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points

            var pageNumber = 1
            var yPosition = 80f
            val lineHeight = 25f
            val marginLeft = 40f
            val marginRight = 40f
            val contentWidth = pageWidth - marginLeft - marginRight

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight.toInt(), pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            // Paint objects
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
            }

            val labelPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            // Draw header
            canvas.drawText("TASK REGISTRY REPORT", marginLeft, yPosition, titlePaint)
            yPosition += 30f

            // Draw filter info
            canvas.drawText("Filter: $timelineFilter", marginLeft, yPosition, headerPaint)
            yPosition += 20f

            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            canvas.drawText("Generated: ${dateFormat.format(Date())}", marginLeft, yPosition, labelPaint)
            yPosition += 10f

            canvas.drawLine(marginLeft, yPosition, pageWidth - marginRight, yPosition, linePaint)
            yPosition += 30f

            // Draw tasks
            tasks.forEachIndexed { index, task ->
                // Check if we need a new page
                if (yPosition > pageHeight - 100) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight.toInt(), pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 60f
                }

                // Task number
                canvas.drawText("${index + 1}. ${task.title}", marginLeft, yPosition, headerPaint)
                yPosition += 20f

                // Status
                canvas.drawText("Status: ${task.status.name.replace("_", " ")}", marginLeft + 20f, yPosition, textPaint)
                yPosition += 18f

                // Priority
                canvas.drawText("Priority: ${task.priority.name}", marginLeft + 20f, yPosition, textPaint)
                yPosition += 18f

                // Description
                if (task.description.isNotBlank()) {
                    val wrappedText = wrapText(task.description, contentWidth - 40f, textPaint)
                    wrappedText.forEach { line ->
                        canvas.drawText(line, marginLeft + 20f, yPosition, textPaint)
                        yPosition += 18f
                    }
                }

                // Due date
                val dueDateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.dueDate))
                canvas.drawText("Due: $dueDateStr", marginLeft + 20f, yPosition, labelPaint)
                yPosition += 15f

                // Separator line
                canvas.drawLine(marginLeft, yPosition, pageWidth - marginRight, yPosition, linePaint)
                yPosition += 20f
            }

            // Summary footer
            if (yPosition > pageHeight - 80) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight.toInt(), pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 60f
            }

            yPosition += 20f
            canvas.drawLine(marginLeft, yPosition, pageWidth - marginRight, yPosition, linePaint)
            yPosition += 25f
            canvas.drawText("Total Tasks: ${tasks.size}", marginLeft, yPosition, headerPaint)
            yPosition += 20f

            val statusCounts = tasks.groupBy { it.status }.mapValues { it.value.size }
            statusCounts.forEach { (status, count) ->
                canvas.drawText("${status.name.replace("_", " ")}: $count", marginLeft + 20f, yPosition, textPaint)
                yPosition += 18f
            }

            pdfDocument.finishPage(page)

            // Save PDF
            val fileName = "TaskRegistry_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()

            // Share PDF
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Task Registry Report")
                putExtra(Intent.EXTRA_TEXT, "Task Registry Report - Generated on ${dateFormat.format(Date())}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Task Report"))

            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Failed to generate PDF")
        }
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)

            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }
}
