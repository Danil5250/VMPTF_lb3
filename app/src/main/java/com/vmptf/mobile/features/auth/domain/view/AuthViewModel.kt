package com.vmptf.mobile.features.auth.domain.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vmptf.mobile.core.data.network.api
import com.vmptf.mobile.features.auth.data.repository.AuthRepositoryImpl
import com.vmptf.mobile.features.auth.domain.model.User
import kotlinx.coroutines.launch

// sealed classes can only be extended within the same module and package
sealed class AuthState {
    object Idle : AuthState() // object Idle which is child of AuthState
    object Loading : AuthState()
    data class LoginSuccess(val token: String, val user: User) : AuthState()
    data class RegisterSuccess(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepositoryImpl(api.authService)

    //Observable Data Holder
    //when we put something to _authState it automatically notifies the activity to update ui
    //Mutable = can be changed, but only value
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    //LiveData = variable only for reading
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email та пароль є обов'язковими")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                _authState.value = AuthState.LoginSuccess(response.token, response.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Некоректний пароль або email")
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("Усі поля є обов'язковими")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = repository.register(email, password, name)
                _authState.value = AuthState.RegisterSuccess(response.message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Помилка реєстрації")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
