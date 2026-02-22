package com.app.officegrid.tasks.presentation.task_list

import app.cash.turbine.test
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.*
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.tasks.domain.usecase.GetTasksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private lateinit var viewModel: TaskListViewModel
    private val getTasksUseCase: GetTasksUseCase = mock()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mock()
    private val repository: TaskRepository = mock()

    private val testDispatcher = StandardTestDispatcher()

    private val testUser = User(
        id = "operative-1",
        email = "op@grid.com",
        fullName = "Operative One",
        role = UserRole.EMPLOYEE,
        companyId = "NODE-X",
        isApproved = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(getCurrentUserUseCase()).thenReturn(flowOf(testUser))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when monthly filter is applied, should filter missions within 30 days`() = runTest {
        val now = Calendar.getInstance().timeInMillis
        val insideRangeTask = createTask("task-1", now + 86400000) // Tomorrow
        val outsideRangeTask = createTask("task-2", now + (40L * 24 * 60 * 60 * 1000)) // 40 days later
        
        whenever(getTasksUseCase(any())).thenReturn(flowOf(listOf(insideRangeTask, outsideRangeTask)))

        viewModel = TaskListViewModel(getTasksUseCase, getCurrentUserUseCase, repository)
        runCurrent()

        viewModel.state.test {
            val state = awaitItem()
            assert(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("task-1", data[0].id)
        }
    }

    @Test
    fun `when custom range is selected, should filter missions accurately`() = runTest {
        val start = 1000L
        val end = 2000L
        val inside = createTask("in", 1500L)
        val outside = createTask("out", 3000L)

        whenever(getTasksUseCase(any())).thenReturn(flowOf(listOf(inside, outside)))

        viewModel = TaskListViewModel(getTasksUseCase, getCurrentUserUseCase, repository)
        runCurrent()

        viewModel.onDateRangeSelected(start, end)
        runCurrent()

        viewModel.state.test {
            val state = awaitItem()
            assert(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("in", data[0].id)
        }
    }

    @Test
    fun `onSearchQueryChange should update results in real-time`() = runTest {
        val taskA = createTask("a", title = "Alpha Mission")
        val taskB = createTask("b", title = "Beta Objective")

        whenever(getTasksUseCase(any())).thenReturn(flowOf(listOf(taskA, taskB)))

        viewModel = TaskListViewModel(getTasksUseCase, getCurrentUserUseCase, repository)
        runCurrent()

        viewModel.onSearchQueryChange("Alpha")
        runCurrent()

        viewModel.state.test {
            val state = awaitItem()
            assert(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assert(data[0].title.contains("Alpha"))
        }
    }

    private fun createTask(id: String, dueDate: Long = 0L, title: String = "Title") = Task(
        id = id,
        title = title,
        description = "Desc",
        status = TaskStatus.TODO,
        priority = TaskPriority.MEDIUM,
        assignedTo = "operative-1",
        createdBy = "admin",
        companyId = "NODE-X",
        dueDate = dueDate,
        createdAt = System.currentTimeMillis()
    )
}
