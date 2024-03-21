package com.nada.weatherapp.data.remote

import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface ForecastRemoteDataSource {
    fun getWeatherInfoOverNetwork(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units : String
    ): Flow<WeatherResponse>
}