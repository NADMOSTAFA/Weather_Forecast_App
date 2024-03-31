package com.nada.weatherapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nada.weatherapp.data.model.Converters
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.model.WeatherResponse

@Database(entities = [WeatherResponse::class, FavoriteWeather::class , WeatherAlert::class], version = 1)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun getWeatherDao(): WeatherDao
    abstract fun getFavoriteWeatherDao(): FavoriteWeatherDao

    abstract fun getAlertDao():AlertDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(ctx: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(ctx, WeatherDatabase::class.java, "weather_forecast_db")
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}