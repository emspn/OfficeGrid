package com.app.officegrid.auth.domain.usecase

import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.model.UserSession
import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.UserRole
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class LoginUseCaseTest {

    @Mock
    private lateinit var authRepository: AuthRepository

    private lateinit var loginUseCase: LoginUseCase

    private val testUser = User(
        id = "test-user-id",
        email = "admin@test.com",
        fullName = "Admin User",
        role = UserRole.ADMIN,
        companyId = "TEST123",
        isApproved = true
    )

    private val testSession = UserSession(
        user = testUser,
        token = "test-token"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `login with valid credentials returns success`() = runTest {
        // Given
        val email = "admin@test.com"
        val password = "password123"
        `when`(authRepository.login(email, password))
            .thenReturn(Result.success(testSession))

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testSession, result.getOrNull())
        verify(authRepository).login(email, password)
    }

    @Test
    fun `login with invalid credentials returns failure`() = runTest {
        // Given
        val email = "wrong@test.com"
        val password = "wrongpass"
        val exception = Exception("Invalid credentials")
        `when`(authRepository.login(email, password))
            .thenReturn(Result.failure(exception))

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
        verify(authRepository).login(email, password)
    }

    @Test
    fun `login with empty email returns failure`() = runTest {
        // Given
        val email = ""
        val password = "password123"

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        verify(authRepository, never()).login(anyString(), anyString())
    }

    @Test
    fun `login with empty password returns failure`() = runTest {
        // Given
        val email = "admin@test.com"
        val password = ""

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        verify(authRepository, never()).login(anyString(), anyString())
    }
}

// Placeholder LoginUseCase implementation for testing
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email and password cannot be empty"))
        }
        return authRepository.login(email, password)
    }
}
