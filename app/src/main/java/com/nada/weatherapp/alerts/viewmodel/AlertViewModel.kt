package com.nada.weatherapp.alerts.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.repo.WeatherInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class AlertViewModel(private val _repo: WeatherInfoRepository) : ViewModel() {
    private var _weatherAlertInfo: MutableStateFlow<List<WeatherAlert>> =
        MutableStateFlow<List<WeatherAlert>>(listOf())
    val weatherAlertInfo: StateFlow<List<WeatherAlert>> = _weatherAlertInfo

    init {
        getWeatherAlerts()
    }

     fun getWeatherAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getWeatherAlerts().collect { data ->
                var myList = data.toMutableList()
                for (weatherAlert in data) {
                    if (isAlertDateBeforeCurrent(weatherAlert.date+" "+weatherAlert.time)) {
                        _repo.deleteWeatherAlert(weatherAlert)
                        myList.remove(weatherAlert)
                    }
                }
                _weatherAlertInfo.value = myList
            }
        }
    }

    private fun getAlertDate(inputDateTimeString: String): Date {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateTimeFormat.parse(inputDateTimeString) ?: Date()
    }

    private fun isAlertDateBeforeCurrent(inputDateTimeString: String): Boolean {
        val currentDateTime = Date()
        val alertDateTime = getAlertDate(inputDateTimeString)
        return alertDateTime.time < currentDateTime.time
    }

    fun cancelWeatherAlert(weatherAlert: WeatherAlert) {
        val workManager = WorkManager.getInstance()
        val workerId = stringToUUID(weatherAlert.id)
        viewModelScope.launch(Dispatchers.IO) {
            workManager.cancelWorkById(workerId)
            _repo.deleteWeatherAlert(weatherAlert)
            getWeatherAlerts()
        }
    }


    private fun stringToUUID(input: String): UUID {
        return UUID.fromString(input)
    }

    private fun insertWeatherAlert(weatherAlert: WeatherAlert) {
        viewModelScope.launch {
            _repo.insertWeatherAlert(weatherAlert)
            getWeatherAlerts()
        }
    }

    fun setAlert(
        context: Context,
        date: String,
        time: String,
        latitude: Double,
        longitude: Double,
        alertType: String
    ) {

        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateTimeString = "$date $time"
        val dateTime: Date = dateTimeFormat.parse(dateTimeString) ?: Date()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()

        inputData.putDouble(Constants.LATITUDE, latitude)
        inputData.putDouble(Constants.LONGITUDE, longitude)
        inputData.putString(Constants.LANGUAGE, _repo.getString(Constants.LANGUAGE, ""))
        inputData.putString(Constants.TYPE, alertType)
        val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
            .setInputData(inputData.build())
            .setInitialDelay(dateTime.time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        val weatherAlert = WeatherAlert(
            workRequest.id.toString(),
            date,
            time,
        )
        insertWeatherAlert(weatherAlert)
        WorkManager.getInstance(context).enqueue(workRequest)

    }
}