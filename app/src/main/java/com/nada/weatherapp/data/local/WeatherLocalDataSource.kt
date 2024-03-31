package com.nada.weatherapp.data.local

import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {
    //Cached Data
    fun getWeatherFromDB(lang : String): Flow<List<WeatherResponse>>
    fun getAllWeatherFromDB(): Flow<List<WeatherResponse>>
    suspend fun insertWeatherResponse(weatherResponse: WeatherResponse)
    suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse)
    suspend fun deleteAll()

    //Favorite
    fun getSavedWeathers(): Flow<List<FavoriteWeather>>
    suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather)
    suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather)

    //Alert
    fun getWeatherAlerts():  Flow<List<WeatherAlert>>
    suspend fun insertWeatherAlert(weatherAlert: WeatherAlert)
    suspend fun deleteWeatherAlert(weatherAlert: WeatherAlert)
}