package com.nada.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nada.weatherapp.data.model.FavoriteWeather

@Dao
interface FavoriteWeatherDao {
    @Query("SELECT * FROM favorite_weather")
    suspend fun getSavedWeathers(): List<FavoriteWeather>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteWeather(favoriteWeather: FavoriteWeather)

    @Delete
    suspend fun deleteFavoriteWeather(favoriteWeather: FavoriteWeather)
}