package com.nada.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather WHERE lang = :lang ")
    fun getWeatherFromDB(lang : String): Flow<List<WeatherResponse>>
    @Query("SELECT * FROM weather ")
    fun getAllWeatherFromDB(): Flow<List<WeatherResponse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherResponse(weatherResponse: WeatherResponse)

    @Delete
    suspend fun deleteWeatherResponse(weatherResponse: WeatherResponse)
    @Query("DELETE FROM weather")
    suspend fun deleteAll()
}