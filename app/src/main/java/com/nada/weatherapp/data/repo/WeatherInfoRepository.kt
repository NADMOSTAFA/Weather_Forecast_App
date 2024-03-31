package com.nada.weatherapp.data.repo

import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.model.WeatherAlert
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

    //Local Cached Data Source
    fun getWeatherFromDB(lang : String): Flow<List<WeatherResponse>>
    suspend fun insertWeatherResponse(weatherResponse: WeatherResponse)
    suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse)
    suspend fun deleteAll()

    //Local Favorite Data Source
    fun getSavedWeathers(): Flow<List<FavoriteWeather>>
    suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather)
    suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather)

    //Shared Preferences
    fun saveString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String?
    fun saveBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun removePreference(key: String)

    //Alerts
    fun getWeatherAlerts():  Flow<List<WeatherAlert>>
    suspend fun insertWeatherAlert(weatherAlert: WeatherAlert)
    suspend fun deleteWeatherAlert(weatherAlert: WeatherAlert)
}