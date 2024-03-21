package com.nada.weatherapp.data.repo

import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import kotlinx.coroutines.flow.Flow

class WeatherInfoRepositoryImpl private constructor(
    private var forecastRemoteDataSource: ForecastRemoteDataSourceImpl,
    private var sharedPreferencesDataSource: SharedPreferencesDataSourceImpl
) : WeatherInfoRepository {
    companion object {
        var instance: WeatherInfoRepositoryImpl? = null
        fun getInstance(
            forecastRemoteDataSource: ForecastRemoteDataSourceImpl,
            sharedPreferencesDataSource: SharedPreferencesDataSourceImpl
        ): WeatherInfoRepositoryImpl {
            return instance ?: synchronized(this) {
                val temp =
                    WeatherInfoRepositoryImpl(forecastRemoteDataSource, sharedPreferencesDataSource)
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
        units : String
    ): Flow<WeatherResponse> {
        return forecastRemoteDataSource.getWeatherInfoOverNetwork(latitude, longitude, apiKey, lang,units)
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
}