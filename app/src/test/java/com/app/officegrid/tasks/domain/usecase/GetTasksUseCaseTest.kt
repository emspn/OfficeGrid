package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class GetTasksUseCaseTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    private lateinit var getTasksUseCase: GetTasksUseCase

    private val testUserId = "user1"

    private val testTasks = listOf(
        Task(
            id = "1",
            title = "Task 1",
            description = "Description 1",
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
            assignedTo = testUserId,
            createdBy = "admin1",
            companyId = "TEST123",
            dueDate = System.currentTimeMillis()
        ),
        Task(
            id = "2",
            title = "Task 2",
            description = "Description 2",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.MEDIUM,
            assignedTo = testUserId,
            createdBy = "admin1",
            companyId = "TEST123",
            dueDate = System.currentTimeMillis()
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getTasksUseCase = GetTasksUseCase(taskRepository)
    }

    @Test
    fun `invoke returns list of tasks for a user`() = runTest {
        // Given
        `when`(taskRepository.getTasks(testUserId)).thenReturn(flowOf(testTasks))

        // When
        val result = getTasksUseCase(testUserId).first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Task 1", result[0].title)
        assertEquals("Task 2", result[1].title)
        verify(taskRepository).getTasks(testUserId)
    }

    @Test
    fun `invoke returns empty list when no tasks exist for user`() = runTest {
        // Given
        `when`(taskRepository.getTasks(testUserId)).thenReturn(flowOf(emptyList()))

        // When
        val result = getTasksUseCase(testUserId).first()

        // Then
        assertTrue(result.isEmpty())
        verify(taskRepository).getTasks(testUserId)
    }
}
