package com.example.corsa.utils

sealed class Option<out T> {

    data object Absent : Option<Nothing>()
    data class Present<T>(val value: T?) : Option<T>()
}
