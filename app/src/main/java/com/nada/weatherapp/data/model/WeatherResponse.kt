package com.nada.weatherapp.data.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherResponse(
    @PrimaryKey
    @NonNull
    var lang : String = "en",
    val cod: String = String(),
    @ColumnInfo
    val message: Int = 0,
    @ColumnInfo
    val cnt: Int = 40,
    @ColumnInfo
    var list: List<WeatherInfo> = listOf(),
    @ColumnInfo
    val city: City = City()
)
