package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class GetTasksUseCaseTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    private lateinit var getTasksUseCase: GetTasksUseCase

    private val testTasks = listOf(
        Task(
            id = "1",
            title = "Task 1",
            description = "Description 1",
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
            assignedTo = "user1",
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
            assignedTo = "user2",
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
    fun `getTasks returns list of tasks`() = runTest {
        // Given
        `when`(taskRepository.getTasks()).thenReturn(flowOf(testTasks))

        // When
        val result = getTasksUseCase().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Task 1", result[0].title)
        assertEquals("Task 2", result[1].title)
        verify(taskRepository).getTasks()
    }

    @Test
    fun `getTasks returns empty list when no tasks exist`() = runTest {
        // Given
        `when`(taskRepository.getTasks()).thenReturn(flowOf(emptyList()))

        // When
        val result = getTasksUseCase().first()

        // Then
        assertTrue(result.isEmpty())
        verify(taskRepository).getTasks()
    }

    @Test
    fun `getTasks filters by status correctly`() = runTest {
        // Given
        val todoTasks = testTasks.filter { it.status == TaskStatus.TODO }
        `when`(taskRepository.getTasksByStatus(TaskStatus.TODO))
            .thenReturn(flowOf(todoTasks))

        // When
        val result = taskRepository.getTasksByStatus(TaskStatus.TODO).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(TaskStatus.TODO, result[0].status)
        verify(taskRepository).getTasksByStatus(TaskStatus.TODO)
    }

    @Test
    fun `getTasks filters by priority correctly`() = runTest {
        // Given
        val highPriorityTasks = testTasks.filter { it.priority == TaskPriority.HIGH }
        `when`(taskRepository.getTasksByPriority(TaskPriority.HIGH))
            .thenReturn(flowOf(highPriorityTasks))

        // When
        val result = taskRepository.getTasksByPriority(TaskPriority.HIGH).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(TaskPriority.HIGH, result[0].priority)
        verify(taskRepository).getTasksByPriority(TaskPriority.HIGH)
    }
}
