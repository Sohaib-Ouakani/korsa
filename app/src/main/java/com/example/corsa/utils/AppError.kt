package com.example.corsa.utils

sealed interface AppError {
    data object Absent: AppError
    data class Present(val message: String): AppError
}