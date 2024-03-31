package com.nada.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nada.weatherapp.data.model.WeatherAlert

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert")
    fun getWeatherAlerts(): List<WeatherAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherAlert(weatherAlert: WeatherAlert)

    @Delete
    suspend fun deleteWeatherAlert(weatherAlert: WeatherAlert)
}