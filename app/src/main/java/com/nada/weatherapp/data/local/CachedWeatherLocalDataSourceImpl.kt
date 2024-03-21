package com.nada.weatherapp.data.local
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CachedWeatherLocalDataSourceImpl(private val dao : WeatherDao) : CachedWeatherLocalDataSource {
    override fun getWeatherFromDB(): Flow<List<WeatherResponse>> {
        return flow {
            emit(dao.getWeatherFromDB())
        }
    }

    override suspend fun insertWeatherResponse(weatherResponse: WeatherResponse): Long {
       return dao.insertWeatherResponse(weatherResponse)
    }

    override suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse): Int {
       return dao.deleteWeatherResponse(weatherResponse)
    }
}