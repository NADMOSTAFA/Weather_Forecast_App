package com.nada.weatherapp.data.local

import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class FakeLocalDataSource(var weatherList: MutableList<FavoriteWeather> = mutableListOf<FavoriteWeather>()) :
    WeatherLocalDataSource {
    override fun getWeatherFromDB(lang: String): Flow<List<WeatherResponse>> {
        TODO("Not yet implemented")
    }

    override fun getAllWeatherFromDB(): Flow<List<WeatherResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertWeatherResponse(weatherResponse: WeatherResponse) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun getSavedWeathers(): Flow<List<FavoriteWeather>> {
        return flow {
            emit(weatherList)
        }
    }

    override suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather) {
        weatherList.add(favoriteWeather)
    }

    override suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather) {
        weatherList.remove(favoriteWeather)
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