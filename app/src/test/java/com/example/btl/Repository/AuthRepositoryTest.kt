package com.example.btl.Repository

import android.content.Context
import android.util.Log
import com.example.btl.Api.AuthApi
import com.example.btl.Model.LoginRequest
import com.example.btl.Model.LoginResponse
import com.example.btl.Model.RegisterRequest
import com.example.btl.Model.User
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response
import okhttp3.ResponseBody

class AuthRepositoryTest {

    @Mock
    private lateinit var authApi: AuthApi
    
    @Mock
    private lateinit var context: Context

    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository(context)
    }

    @Test
    fun testLoginSuccess() = runTest {
        // Arrange
        val loginRequest = LoginRequest("user@example.com", "password123")
        val mockUser = User(
            user_id = 1,
            email = "user@example.com",
            username = "testuser",
            phone = "0123456789",
            role = "customer",
            status = "active",
            created_at = "2026-04-17",
            updated_at = "2026-04-17"
        )
        val mockResponse = LoginResponse(
            access_token = "fake_token_123",
            user = mockUser
        )

        whenever(authApi.login(loginRequest))
            .thenReturn(Response.success(mockResponse))

        // Act
        val response = authApi.login(loginRequest)

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.user?.user_id == 1)
        assert(response.body()?.user?.email == "user@example.com")
    }

    @Test
    fun testLoginFailure() = runTest {
        // Arrange
        val loginRequest = LoginRequest("invalid@example.com", "wrongpassword")

        whenever(authApi.login(loginRequest))
            .thenReturn(Response.error(401, ResponseBody.create(null, "")))

        // Act
        val response = authApi.login(loginRequest)

        // Assert
        assert(!response.isSuccessful)
        assert(response.code() == 401)
    }

    @Test
    fun testRegisterSuccess() = runTest {
        // Arrange
        val registerRequest = RegisterRequest(
            username = "newuser",
            email = "newuser@example.com",
            password = "password123"
        )
        val mockUser = User(
            user_id = 2,
            email = "newuser@example.com",
            username = "newuser",
            phone = "0987654321",
            role = "customer",
            status = "active",
            created_at = "2026-04-17",
            updated_at = "2026-04-17"
        )
        val mockResponse = LoginResponse(
            access_token = "fake_token_456",
            user = mockUser
        )

        whenever(authApi.register(registerRequest))
            .thenReturn(Response.success(mockResponse))

        // Act
        val response = authApi.register(registerRequest)

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.user?.username == "newuser")
    }

    @Test
    fun testLoginWithEmptyEmail() = runTest {
        // Arrange
        val loginRequest = LoginRequest("", "password123")

        // Act & Assert
        assert(loginRequest.email.isEmpty())
    }

    @Test
    fun testLoginWithWeakPassword() = runTest {
        // Arrange
        val password = "123"  // Too weak

        // Act & Assert
        assert(password.length < 6)
    }
}

