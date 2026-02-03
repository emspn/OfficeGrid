package com.app.officegrid.tasks.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_remarks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class TaskRemarkEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val message: String,
    val createdBy: String,
    val createdAt: Long
)
