package com.nada.weatherapp.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class WeatherInfo(
    val dt: Long,
    val main: MainInfo,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val rain: Rain,
    val sys: System,
    val dt_txt: String,
    var date : String
)








