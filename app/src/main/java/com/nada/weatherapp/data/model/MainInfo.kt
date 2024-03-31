package com.nada.weatherapp.data.model


data class MainInfo(
    var temp: Double,
    var feels_like: Double,
    var temp_min: Double,
    var temp_max: Double,
    val pressure: Int,
    val sea_level: Int,
    val grnd_level: Int,
    val humidity: Int,
    var temp_kf: Double
)
