package com.nada.weatherapp.weather_info.view

import com.nada.weatherapp.data.model.WeatherInfo

interface OnWeatherInfoClickListener {
    fun onClick(weatherInfo: WeatherInfo)
}