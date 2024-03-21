package com.nada.weatherapp.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.nada.weatherapp.data.repo.WeatherInfoRepository
import java.lang.IllegalArgumentException

class SettingsViewModelFactory(private val _repo: WeatherInfoRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            SettingsViewModel(_repo) as T
        } else {
            throw IllegalArgumentException("Not Found")
        }
    }
}