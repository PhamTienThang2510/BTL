package com.example.btl.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.btl.Model.LoginResponse
import com.example.btl.Repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application.applicationContext)

    private val _loginResponse = MutableLiveData<LoginResponse?>(null)
    val loginResponse: LiveData<LoginResponse?> = _loginResponse

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    // LiveData for forgot password success status
    private val _forgotPasswordSuccess = MutableLiveData<Boolean>(false)
    val forgotPasswordSuccess: LiveData<Boolean> = _forgotPasswordSuccess

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.login(email, password)
            result.onSuccess { loginResponse ->
                _loginResponse.value = loginResponse
                _isLoggedIn.value = true
                _isLoading.value = false
            }
             result.onFailure { exception ->
                 _errorMessage.value = exception.message ?: "Đăng nhập thất bại"
                 _isLoading.value = false
             }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.register(username, email, password)
            result.onSuccess { loginResponse ->
                _loginResponse.value = loginResponse
                _isLoggedIn.value = true
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Đăng ký thất bại"
                _isLoading.value = false
            }
        }
    }

    /**
     * Xử lý yêu cầu quên mật khẩu
     */
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _forgotPasswordSuccess.value = false

            val result = authRepository.forgotPassword(email)
            result.onSuccess {
                _forgotPasswordSuccess.value = true
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Gửi yêu cầu thất bại"
                _isLoading.value = false
            }
        }
    }

    fun resetForgotPasswordStatus() {
        _forgotPasswordSuccess.value = false
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginResponse.value = null
            _isLoggedIn.value = false
            _errorMessage.value = null
        }
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            val token = authRepository.getStoredToken()
            _isLoggedIn.value = token != null
        }
    }

    fun clearLoginResponse() {
        _loginResponse.value = null
    }
}
