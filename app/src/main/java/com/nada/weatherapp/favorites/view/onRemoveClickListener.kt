package com.nada.weatherapp.favorites.view

import com.nada.weatherapp.data.model.FavoriteWeather

interface onRemoveClickListener {
    fun onClickRemove(favoriteWeather: FavoriteWeather)
}