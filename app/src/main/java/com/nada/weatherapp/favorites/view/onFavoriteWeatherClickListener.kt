package com.nada.weatherapp.favorites.view

import com.nada.weatherapp.data.model.FavoriteWeather

interface onFavoriteWeatherClickListener {
    fun onClick(favoriteWeather: FavoriteWeather)

}