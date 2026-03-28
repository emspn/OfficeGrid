package com.app.officegrid.tasks.presentation.task_list

import app.cash.turbine.test
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.tasks.domain.usecase.GetTasksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
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
        runBlocking {
            whenever(repository.syncTasks(any())).thenReturn(Result.success(Unit))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `monthly filter includes tasks due within 30 days`() = runTest(testDispatcher) {
        val now = Calendar.getInstance().timeInMillis
        val insideRangeTask = createTask("task-1", now + 86400000) // Tomorrow
        val outsideRangeTask = createTask("task-2", now + (40L * 24 * 60 * 60 * 1000)) // 40 days later

        whenever(repository.observeUserTasks(testUser.id, testUser.companyId))
            .thenReturn(flowOf(listOf(insideRangeTask, outsideRangeTask)))

        viewModel = TaskListViewModel(getTasksUseCase, getCurrentUserUseCase, repository)

        viewModel.state.test {
            assert(awaitItem() is UiState.Loading)
            advanceUntilIdle()
            val state = awaitItem()
            assert(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("task-1", data[0].id)
        }
    }

    @Test
    fun `custom range filters tasks accurately`() = runTest(testDispatcher) {
        val start = 1000L
        val end = 2000L
        val inside = createTask("in", 1500L)
        val outside = createTask("out", 3000L)

        whenever(repository.observeUserTasks(testUser.id, testUser.companyId))
            .thenReturn(flowOf(listOf(inside, outside)))

        viewModel = TaskListViewModel(getTasksUseCase, getCurrentUserUseCase, repository)
        viewModel.onDateRangeSelected(start, end)

        viewModel.state.test {
            assert(awaitItem() is UiState.Loading)
            advanceUntilIdle()
            val state = awaitItem()
            assert(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("in", data[0].id)
        }
    }

    @Test
    fun `search query updates results`() = runTest(testDispatcher) {
        val taskA = createTask("a", title = "Alpha Mission")
        val taskB = createTask("b", title = "Beta Objective")

        whenever(repository.observeUserTasks(testUser.id, testUser.companyId))
            .thenReturn(flowOf(listOf(taskA, taskB)))

        viewModel = TaskListViewModel(getTasksUseCase, getCurrentUserUseCase, repository)
        viewModel.onSearchQueryChange("Alpha")

        viewModel.state.test {
            assert(awaitItem() is UiState.Loading)
            advanceUntilIdle()
            val state = awaitItem()
            assert(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("a", data[0].id)
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
