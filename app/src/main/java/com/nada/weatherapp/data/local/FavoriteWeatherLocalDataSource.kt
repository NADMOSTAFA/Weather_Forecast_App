package com.nada.weatherapp.data.local
import com.nada.weatherapp.data.model.FavoriteWeather
import kotlinx.coroutines.flow.Flow

interface FavoriteWeatherLocalDataSource {
    //Favorite Data
    fun getSavedWeathers(): Flow<List<FavoriteWeather>>
    suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather): Long
    suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather): Int
}