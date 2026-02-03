package com.app.officegrid.tasks.presentation.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.usecase.ExportFormat
import com.app.officegrid.tasks.domain.usecase.ExportTasksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class TaskExportHelper @Inject constructor(
    private val exportTasksUseCase: ExportTasksUseCase
) {

    suspend fun exportAndShare(
        context: Context,
        tasks: List<Task>,
        format: ExportFormat
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Generate export content
            val content = exportTasksUseCase(tasks, format)

            // Get file extension
            val extension = when (format) {
                ExportFormat.CSV -> "csv"
                ExportFormat.TEXT -> "txt"
                ExportFormat.MARKDOWN -> "md"
            }

            // Create file in cache directory
            val fileName = "tasks_export_${System.currentTimeMillis()}.$extension"
            val file = File(context.cacheDir, fileName)
            file.writeText(content)

            // Create share intent
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = when (format) {
                    ExportFormat.CSV -> "text/csv"
                    ExportFormat.TEXT -> "text/plain"
                    ExportFormat.MARKDOWN -> "text/markdown"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "OfficeGrid Tasks Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Tasks"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

