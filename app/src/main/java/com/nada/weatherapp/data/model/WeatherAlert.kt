package com.nada.weatherapp.data.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "alert")
data class WeatherAlert(
    @PrimaryKey
    @NonNull
    var id : String,
    @ColumnInfo
    var date : String,
    @ColumnInfo
    var time : String,
)
