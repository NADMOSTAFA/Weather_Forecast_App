package com.nada.weatherapp.Utils

import com.google.gson.reflect.TypeToken

sealed class State<out T> {
    abstract val type: Class<out T>
    data class Success<T>(val data: T, val clazz: Class<out T>) : State<T>() {
        override val type: Class<out T> = clazz
    }

    data class Failure(val msg: Throwable) : State<Nothing>() {
        override val type: Class<Nothing> = Nothing::class.java
    }

    object Loading : State<Nothing>() {
        override val type: Class<Nothing> = Nothing::class.java
    }
}

inline fun <reified T> getListClass(): Class<List<T>> {
    return object : TypeToken<List<T>>() {}.rawType as Class<List<T>>
}
