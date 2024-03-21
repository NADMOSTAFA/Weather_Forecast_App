package com.nada.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nada.weatherapp.data.model.WeatherResponse

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather")
    fun getWeatherFromDB(): List<WeatherResponse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherResponse(weatherResponse: WeatherResponse): Long

    @Delete
    suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse): Int
}