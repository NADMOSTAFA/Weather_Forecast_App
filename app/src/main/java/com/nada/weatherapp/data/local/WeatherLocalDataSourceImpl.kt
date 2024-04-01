package com.nada.weatherapp.data.local

import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherLocalDataSourceImpl(
    private val weatherDao: WeatherDao,
    private val favoriteWeatherDao: FavoriteWeatherDao,
    private val alertDao: AlertDao
) : WeatherLocalDataSource {
    override fun getWeatherFromDB(lang: String): Flow<List<WeatherResponse>> {
//        return flow {
//            emit(weatherDao.getWeatherFromDB(lang))
//        }
        return weatherDao.getWeatherFromDB(lang)
    }

    override fun getAllWeatherFromDB(): Flow<List<WeatherResponse>> {
//        return flow {
//            emit(weatherDao.getAllWeatherFromDB())
//        }
        return weatherDao.getAllWeatherFromDB()
    }

    override suspend fun insertWeatherResponse(weatherResponse: WeatherResponse) {
        weatherDao.insertWeatherResponse(weatherResponse)
    }

    override suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse) {
        weatherDao.deleteWeatherResponse(weatherResponse)
    }

    override suspend fun deleteAll() {
        weatherDao.deleteAll()
    }

    //Favorite
    override fun getSavedWeathers(): Flow<List<FavoriteWeather>> {
//        return flow {
//            emit(favoriteWeatherDao.getSavedWeathers())
//        }
        return favoriteWeatherDao.getSavedWeathers()
    }

    override suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather) {
        favoriteWeatherDao.insertFavoriteWeather(favoriteWeather)
    }

    override suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather) {
        favoriteWeatherDao.deleteFavoriteWeather(favoriteWeather)
    }

    //Alert
    override fun getWeatherAlerts():  Flow<List<WeatherAlert>> {
//        return flow {
//            emit(alertDao.getWeatherAlerts())
//        }
        return alertDao.getWeatherAlerts()
    }

    override suspend fun insertWeatherAlert(weatherAlert: WeatherAlert) {
       alertDao.insertWeatherAlert(weatherAlert)
    }

    override suspend fun deleteWeatherAlert(weatherAlert: WeatherAlert) {
        alertDao.deleteWeatherAlert(weatherAlert)
    }
}