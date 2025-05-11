package com.example.cafemusicchange

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class mainState(
    val Notified: String = "",
    val Error: String = "",
    val userId: Long? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {
    val _uiState = MutableStateFlow(mainState())
    val uiState = _uiState.asStateFlow()

    fun setUserId(userId: Long){
        _uiState.value = _uiState.value.copy(userId=userId)
    }
    fun setError(message: String){
        _uiState.value = _uiState.value.copy(Error=message)
    }
    fun setNotified(message: String){
        _uiState.value = _uiState.value.copy(Notified=message)
    }


}