package com.example.myapplication.data.model

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T, val message: String? = null) : ApiResult<T>
    data class Error(val message: String, val statusCode: Int = 0) : ApiResult<Nothing>
}
