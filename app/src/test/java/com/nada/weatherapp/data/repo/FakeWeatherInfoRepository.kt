package com.nada.weatherapp.data.repo

import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeWeatherInfoRepository : WeatherInfoRepository {

    private var favoriteList = mutableListOf<FavoriteWeather>()
    private var weatherList = mutableListOf<WeatherResponse>()
    private var stringMap = mutableMapOf<String, String>()
    private var booleanMap = mutableMapOf<String,Boolean>()

    override fun getWeatherInfoOverNetwork(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Flow<WeatherResponse> {
        TODO("Not yet implemented")
    }

    override fun getWeatherFromDB(lang: String): Flow<List<WeatherResponse>> {
       return flow {
           emit(weatherList)
       }
    }

    //Taken
    override suspend fun insertWeatherResponse(weatherResponse: WeatherResponse) {
        weatherList.add(weatherResponse)
    }

    override suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse) {
        weatherList.remove(weatherResponse)
    }

    override suspend fun deleteAll() {
        weatherList.clear()
    }

    //favorite
    override fun getSavedWeathers(): Flow<List<FavoriteWeather>> {
        return flow {
            emit(favoriteList)
        }
    }

    //favorite
    override suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather) {
        favoriteList.add(favoriteWeather)
    }

    //favorite
    override suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather) {
        favoriteList.remove(favoriteWeather)
    }

    override fun saveString(key: String, value: String) {
        stringMap[key] = value
    }

    override fun getString(key: String, defaultValue: String): String? {
       return stringMap[key] ?: defaultValue
    }

    override fun saveBoolean(key: String, value: Boolean) {
        booleanMap[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return  booleanMap[key] ?: defaultValue
    }

    override fun removePreference(key: String) {
        TODO("Not yet implemented")
    }

    override fun getWeatherAlerts(): Flow<List<WeatherAlert>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertWeatherAlert(weatherAlert: WeatherAlert) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeatherAlert(weatherAlert: WeatherAlert) {
        TODO("Not yet implemented")
    }
}