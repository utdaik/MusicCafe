package com.example.cafemusicchange.common.enum

sealed class LoadStatus (val description: String="") {
    class Loading() : LoadStatus()
    class Success() : LoadStatus()
    class Error(message: String) : LoadStatus(message)
    class Init: LoadStatus()
}