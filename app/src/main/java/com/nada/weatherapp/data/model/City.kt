package com.nada.weatherapp.data.model

data class City(
    val id: Int,
    val name: String,
    val coord: Coordinate,
    var country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)



