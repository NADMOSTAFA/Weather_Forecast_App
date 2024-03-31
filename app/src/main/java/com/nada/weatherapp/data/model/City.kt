package com.nada.weatherapp.data.model

data class City(
    val id: Int = 0,
    val name: String = String(),
    val coord: Coordinate = Coordinate(),
    var country: String = String(),
    val population: Int = 0,
    val timezone: Int = 0,
    val sunrise: Long = 0,
    val sunset: Long = 0
)



