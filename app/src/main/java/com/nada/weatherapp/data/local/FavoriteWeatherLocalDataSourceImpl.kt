package com.nada.weatherapp.data.local

import com.nada.weatherapp.data.model.FavoriteWeather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavoriteWeatherLocalDataSourceImpl(private val dao : FavoriteWeatherDao) : FavoriteWeatherLocalDataSource {
    override fun getSavedWeathers(): Flow<List<FavoriteWeather>> {
        return flow {
            emit(dao.getSavedWeathers())
        }
    }

    override suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather) {
         dao.insertFavoriteWeather(favoriteWeather)
    }

    override suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather) {
         dao.deleteFavoriteWeather(favoriteWeather)
    }
}