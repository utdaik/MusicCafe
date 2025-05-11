package com.example.cafemusicchange.ui.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafemusicchange.common.enum.LoadStatus
import com.example.cafemusicchange.local.User
import com.example.cafemusicchange.reponsitory.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LoginState(
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val useId: Long? = null,
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: MusicRepository?
): ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@gmail.com")
    }


    private fun isValidPassword(password: String): Boolean {
        return password.trim().matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"))
    }


    fun registerUser(user : User){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            Log.i("Check email" , user.email)
            if(!isValidEmail(user.email)){
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Email không hợp lệ"))
                return@launch
            }
            if (!isValidPassword(user.password)) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt!"))
                return@launch
            }

            if (user.email.isBlank()) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Email không được để trống"))
                return@launch
            }
            if (user.username.isBlank()) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Tên người dùng không được để trống"))
                return@launch
            }
            if (user.password.isBlank()) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Mật khẩu không được để trống"))
                return@launch
            }


            val isExists = repository?.isUserExists(user.email)
            if(isExists == true){
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Email đã tồn tại"))
                return@launch
            }

            try {
                repository?.insertUser(user)
                _uiState.value = _uiState.value.copy(status = LoadStatus.Success())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi ko xác định"))
            }
        }
    }
    fun reset (){
        _uiState.value = _uiState.value.copy(status = LoadStatus.Init())
    }

    fun login(email: String, password: String){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            if(!isValidEmail(email)){
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Email không hợp lệ"))
                return@launch
            }
            if (!isValidPassword(password)) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt!"))
                return@launch
            }
            try {
                val user = repository?.login(email, password)
                if (user != null) {
                    val userId = repository?.getUserId(email)
                    _uiState.value = _uiState.value.copy(useId = userId)
                    Log.i("Check userId in Loginviewmodel" , userId.toString())
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Success())
                } else
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Sai tài khoản hoặc mật khẩu"))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("unow error"))
            }
        }
        }

}