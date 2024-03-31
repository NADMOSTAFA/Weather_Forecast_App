package com.nada.weatherapp.data.repo

import com.nada.weatherapp.data.local.WeatherLocalDataSource
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.data.remote.ForecastRemoteDataSource
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSource
import kotlinx.coroutines.flow.Flow

class WeatherInfoRepositoryImpl  constructor(
    private var forecastRemoteDataSource: ForecastRemoteDataSource,
    private var sharedPreferencesDataSource: SharedPreferencesDataSource,
//    private var favoriteWeatherLocalDataSource: FavoriteWeatherLocalDataSource,
    private var weatherLocalDataSource: WeatherLocalDataSource

) : WeatherInfoRepository {
    companion object {
        private var instance: WeatherInfoRepositoryImpl? = null
        fun getInstance(
            forecastRemoteDataSource: ForecastRemoteDataSource,
            sharedPreferencesDataSource: SharedPreferencesDataSource,
//            favoriteWeatherLocalDataSource: FavoriteWeatherLocalDataSource,
            weatherLocalDataSource: WeatherLocalDataSource
        ): WeatherInfoRepositoryImpl {
            return instance ?: synchronized(this) {
                val temp =
                    WeatherInfoRepositoryImpl(
                        forecastRemoteDataSource,
                        sharedPreferencesDataSource,
//                        favoriteWeatherLocalDataSource,
                        weatherLocalDataSource
                    )
                instance = temp
                temp
            }
        }
    }

    override fun getWeatherInfoOverNetwork(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Flow<WeatherResponse> {
        return forecastRemoteDataSource.getWeatherInfoOverNetwork(
            latitude,
            longitude,
            apiKey,
            lang,
            units
        )
    }

    override fun getWeatherFromDB(lang: String): Flow<List<WeatherResponse>> {
        return weatherLocalDataSource.getWeatherFromDB(lang)
    }

    override suspend fun insertWeatherResponse(weatherResponse: WeatherResponse) {
        weatherLocalDataSource.insertWeatherResponse(weatherResponse)
    }

    override suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse) {
        weatherLocalDataSource.deleteWeatherResponse(weatherResponse)
    }

    override suspend fun deleteAll() {
        weatherLocalDataSource.deleteAll()
    }

    override fun getSavedWeathers(): Flow<List<FavoriteWeather>> {
        return weatherLocalDataSource.getSavedWeathers()
    }

    override suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather) {
        weatherLocalDataSource.insertFavoriteWeather(favoriteWeather)
    }

    override suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather) {
        weatherLocalDataSource.deleteFavoriteWeather(favoriteWeather)
    }

    override fun saveString(key: String, value: String) {
        sharedPreferencesDataSource.saveString(key, value)
    }

    override fun getString(key: String, defaultValue: String): String? {
        return sharedPreferencesDataSource.getString(key, defaultValue)
    }

    override fun saveBoolean(key: String, value: Boolean) {
        sharedPreferencesDataSource.saveBoolean(key, value)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferencesDataSource.getBoolean(key, defaultValue)
    }

    override fun removePreference(key: String) {
        sharedPreferencesDataSource.removePreference(key)
    }

    override fun getWeatherAlerts(): Flow<List<WeatherAlert>> {
        return weatherLocalDataSource.getWeatherAlerts()
    }

    override suspend fun insertWeatherAlert(weatherAlert: WeatherAlert) {
        weatherLocalDataSource.insertWeatherAlert(weatherAlert)
    }

    override suspend fun deleteWeatherAlert(weatherAlert: WeatherAlert) {
        weatherLocalDataSource.deleteWeatherAlert(weatherAlert)
    }
}