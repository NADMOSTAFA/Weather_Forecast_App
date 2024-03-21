package com.nada.weatherapp.data.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherResponse(
    @PrimaryKey
    @NonNull
    val id : String = "1",
    val cod: String,
    @ColumnInfo
    val message: Int,
    @ColumnInfo
    val cnt: Int,
    @ColumnInfo
    val list: List<WeatherInfo>,
    @ColumnInfo
    val city: City
)
