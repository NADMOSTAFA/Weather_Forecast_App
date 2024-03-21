package com.nada.weatherapp.data.repo

import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface WeatherInfoRepository {
    //Network
    fun getWeatherInfoOverNetwork(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units : String
    ): Flow<WeatherResponse>

    //Shared Preferences
    fun saveString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String?
    fun saveBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
}