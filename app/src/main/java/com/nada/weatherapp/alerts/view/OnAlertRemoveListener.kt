package com.nada.weatherapp.alerts.view

import com.nada.weatherapp.data.model.WeatherAlert

interface OnAlertRemoveListener {
    fun onClickRemove(weatherAlert: WeatherAlert)
}