package com.nada.weatherapp.favorites.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nada.weatherapp.Utils.State
import com.nada.weatherapp.Utils.getListClass
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.repo.WeatherInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FavoriteViewModel(private val _repo: WeatherInfoRepository) : ViewModel() {
    //Backing Property
    private var _favWeatherInfo: MutableStateFlow<List<FavoriteWeather>> =
        MutableStateFlow<List<FavoriteWeather>>(listOf())
    val favWeatherInfo: StateFlow<List<FavoriteWeather>> = _favWeatherInfo

    init {
        getStoredWeather()
    }

     fun getStoredWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getSavedWeathers().collect { data ->
                _favWeatherInfo.value = data
            }
        }
    }

    fun removeWeather(weather: FavoriteWeather) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.deleteFavoriteWeather(weather)
            getStoredWeather()
        }
    }

    fun insertWeather(weather: FavoriteWeather) {
        viewModelScope.launch {
            _repo.insertFavoriteWeather(weather)
        }
    }


}