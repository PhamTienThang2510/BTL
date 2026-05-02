package com.example.btl.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.btl.Model.User
import com.example.btl.Repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private var userRepository: UserRepository? = null

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Initialize with context (should be called in onViewCreated)
     */
    fun init(context: Context) {
        userRepository = UserRepository(context)
    }

    fun loadUserProfile() {
        if (userRepository == null) {
            _errorMessage.value = "UserRepository not initialized. Call init(context) first."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = userRepository?.getCurrentUser()
            if (result != null) {
                result.onSuccess { user ->
                    _user.value = user
                    _isLoading.value = false
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load profile"
                    _isLoading.value = false
                }
            } else {
                _errorMessage.value = "Failed to load profile: repository returned null"
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(username: String?, phone: String?) {
        if (userRepository == null) {
            _errorMessage.value = "UserRepository not initialized. Call init(context) first."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = userRepository?.updateUserProfile(username, phone)
            if (result != null) {
                result.onSuccess { user ->
                    _user.value = user
                    _isLoading.value = false
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update profile"
                    _isLoading.value = false
                }
            } else {
                _errorMessage.value = "Failed to update profile: repository returned null"
                _isLoading.value = false
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        if (userRepository == null) {
            _errorMessage.value = "UserRepository not initialized. Call init(context) first."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = userRepository?.changePassword(oldPassword, newPassword)
            if (result != null) {
                result.onSuccess { message ->
                    _errorMessage.value = message
                    _isLoading.value = false
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to change password"
                    _isLoading.value = false
                }
            } else {
                _errorMessage.value = "Failed to change password: repository returned null"
                _isLoading.value = false
            }
        }
    }
}

