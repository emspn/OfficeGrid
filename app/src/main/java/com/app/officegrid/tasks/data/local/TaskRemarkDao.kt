package com.app.officegrid.tasks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskRemarkDao {
    @Query("SELECT * FROM task_remarks WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getRemarksForTask(taskId: String): Flow<List<TaskRemarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemarks(remarks: List<TaskRemarkEntity>)

    @Query("DELETE FROM task_remarks WHERE taskId = :taskId")
    suspend fun deleteRemarksForTask(taskId: String)
}
