package com.nada.weatherapp

import android.app.Application
import android.util.Log

class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("TAG", "onCreate: WeatherApplication")

    }
}