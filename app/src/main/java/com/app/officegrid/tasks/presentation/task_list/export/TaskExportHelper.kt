package com.app.officegrid.tasks.presentation.task_list.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import android.text.Layout
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.app.officegrid.tasks.domain.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🚀 PRODUCTION-GRADE TASK EXPORT HELPER
 * Optimized to run off the main thread to prevent UI lag/crashes.
 */
object TaskExportHelper {

    suspend fun exportToCsv(context: Context, tasks: List<Task>, fileName: String) = withContext(Dispatchers.IO) {
        try {
            val csvData = StringBuilder()
            csvData.append("ID,Title,Description,Status,Priority,Assigned To,Created By,Due Date,Created At\n")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            
            tasks.forEach { task ->
                csvData.append("\"${task.id}\",")
                csvData.append("\"${task.title.replace("\"", "'")}\",")
                csvData.append("\"${task.description.replace("\n", " ").replace("\"", "'")}\",")
                csvData.append("\"${task.status}\",")
                csvData.append("\"${task.priority}\",")
                csvData.append("\"${task.assignedTo}\",")
                csvData.append("\"${task.createdBy}\",")
                csvData.append("\"${dateFormat.format(Date(task.dueDate))}\",")
                csvData.append("\"${dateFormat.format(Date(task.createdAt))}\"\n")
            }

            val file = File(context.cacheDir, "$fileName.csv")
            FileOutputStream(file).use { it.write(csvData.toString().toByteArray()) }
            
            withContext(Dispatchers.Main) {
                shareFile(context, file, "text/csv")
            }
        } catch (e: Exception) {
            Log.e("Export", "CSV Export failed", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "CSV Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun exportToPdf(context: Context, tasks: List<Task>, fileName: String) = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        try {
            val textPaint = TextPaint().apply {
                textSize = 10f
                isAntiAlias = true
            }
            val headerPaint = TextPaint().apply {
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val titlePaint = TextPaint().apply {
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val pageWidth = 595 // A4 width
            val pageHeight = 842 // A4 height
            val margin = 40f
            val contentWidth = (pageWidth - (margin * 2)).toInt()
            var yPosition = margin

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            // Header
            canvas.drawText("OFFICE_GRID MISSION REGISTRY REPORT", margin, yPosition, titlePaint)
            yPosition += 30f
            canvas.drawText("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", margin, yPosition, textPaint)
            yPosition += 40f

            tasks.forEach { task ->
                if (yPosition > pageHeight - 150f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = margin
                }

                canvas.drawText("MISSION: ${task.title.uppercase()}", margin, yPosition, headerPaint)
                yPosition += 18f
                
                canvas.drawText("Status: ${task.status} | Priority: ${task.priority}", margin, yPosition, textPaint)
                yPosition += 15f
                
                val descriptionText = "Specs: ${task.description.ifBlank { "No specifications provided." }}"
                val descLayout = StaticLayout.Builder.obtain(
                    descriptionText,
                    0, 
                    descriptionText.length,
                    textPaint,
                    contentWidth
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(margin, yPosition)
                descLayout.draw(canvas)
                canvas.restore()
                
                yPosition += descLayout.height + 20f
                
                canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, Paint().apply { alpha = 50 })
                yPosition += 25f
            }

            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, "$fileName.pdf")
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            
            withContext(Dispatchers.Main) {
                shareFile(context, file, "application/pdf")
            }
        } catch (e: Exception) {
            Log.e("Export", "PDF generation failed", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "PDF generation failed", Toast.LENGTH_SHORT).show()
            }
        } finally {
            pdfDocument.close()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Mission Report"))
        } catch (e: Exception) {
            Log.e("Export", "File sharing failed", e)
            Toast.makeText(context, "Sharing failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
