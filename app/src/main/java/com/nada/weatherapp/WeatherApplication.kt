package com.nada.weatherapp

import android.app.Application
import android.util.Log
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl

class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = SharedPreferencesDataSourceImpl.getInstance(this)
        sharedPreferences.saveBoolean(Constants.SESSION,false)
    }
}