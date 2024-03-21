package com.nada.weatherapp.Utils

sealed class ApiState<out T> {
    abstract val type: Class<out T>

    data class Success<T>(val data: T, val clazz: Class<out T>) : ApiState<T>() {
        override val type: Class<out T> = clazz
    }

    data class Failure(val msg: Throwable) : ApiState<Nothing>() {
        override val type: Class<Nothing> = Nothing::class.java
    }

    object Loading : ApiState<Nothing>() {
        override val type: Class<Nothing> = Nothing::class.java
    }
}
