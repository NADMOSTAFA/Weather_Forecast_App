package com.nada.weatherapp.data.local

import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface CachedWeatherLocalDataSource {
    //Cached Data
    fun getWeatherFromDB(): Flow<List<WeatherResponse>>
    suspend fun insertWeatherResponse(weatherResponse: WeatherResponse): Long
    suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse): Int
}