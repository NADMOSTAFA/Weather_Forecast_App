package com.nada.weatherapp.data.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "favorite_weather", primaryKeys = ["latitude", "longitude"])
data class FavoriteWeather(
    @NonNull
    val latitude : Double,
    @NonNull
    val longitude: Double,
    @ColumnInfo
    val country: String,
    @ColumnInfo
    var city: String,
)
