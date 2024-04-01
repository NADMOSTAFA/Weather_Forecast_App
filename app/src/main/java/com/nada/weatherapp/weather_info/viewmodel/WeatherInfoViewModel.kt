package com.nada.weatherapp.weather_info.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.Utils.State
import com.nada.weatherapp.data.model.WeatherInfo
import com.nada.weatherapp.data.repo.WeatherInfoRepository
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherInfoViewModel(private val _repo: WeatherInfoRepository) : ViewModel() {
    //Backing Property
    private var _weatherInfo: MutableStateFlow<State<WeatherResponse>> =
        MutableStateFlow<State<WeatherResponse>>(State.Loading)
    val weatherInfo: StateFlow<State<WeatherResponse>> = _weatherInfo

    fun getWeatherInfoOverNetwork(
        latitude: Double,
        longitude: Double,
        apiKey: String = "47c1440938387595e380110ff3ce9e84",
        lang: String = "en",
        units: String = "standard",
        source: String = Constants.HOME
    ) {
        Log.i("here", "getWeatherInfoOverNetwork: Entered From Real Call")
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getWeatherInfoOverNetwork(latitude, longitude, apiKey, lang, units).catch { e ->
                _weatherInfo.value = State.Failure(e)
            }.collect { data ->
                Log.i(
                    "here",
                    "getWeatherInfoOverNetwork: ${_repo.getBoolean(Constants.SESSION, false)}"
                )

                when (source) {
                    Constants.HOME -> {
                        if (!_repo.getBoolean(Constants.SESSION, false)) {
                            _repo.deleteAll()
                        }
                        data.lang = lang
                        _repo.insertWeatherResponse(data)
                        data.list = getTodayForecast(data.list)
                    }

                    Constants.FOUR_DAYS_FORECAST -> {
                        data.list = getFourDaysForecast(data.list)
                    }

                    Constants.TODAY_FORECAST -> {
                        data.list = getTodayForecast(data.list)
                    }
                }
                var weatherResponse = data

                weatherResponse = setWeatherResponseTempUnit(data)
                when (getWindUnit()) {
                    Constants.MILE_PER_HOUR -> {
                        weatherResponse = convertToMPH(weatherResponse)
                    }
                }
                if (source == Constants.HOME) {
                    setDataCached(true)
                }
                _weatherInfo.value =
                    State.Success(weatherResponse, WeatherResponse::class.java)
            }
        }

    }

    private fun setWeatherResponseTempUnit(weatherResponse: WeatherResponse) : WeatherResponse{
        var convertedData = weatherResponse
        when (getTemperatureUnit()) {
            Constants.IMPERIAL -> {
                convertedData = convertToFahrenheit(weatherResponse)

            }

            Constants.METRIC -> {
                convertedData = convertToCelsius(weatherResponse)

            }
        }
        return convertedData
    }


    fun getWeatherFromDB(lang: String, fromHome: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getWeatherFromDB(lang).collect { data ->
                if (data.size == 1) {
                    if (fromHome) {
                        data.get(0).list = getTodayForecast(data.get(0).list)
                    } else {
                        data.get(0).list = getFourDaysForecast(data.get(0).list)
                    }
                    var weatherResponse = data.get(0)
                    when (getTemperatureUnit()) {
                        Constants.IMPERIAL -> {
                            weatherResponse = convertToFahrenheit(data.get(0))

                        }

                        Constants.METRIC -> {
                            weatherResponse = convertToCelsius(data.get(0))

                        }
                    }
                    when (getWindUnit()) {
                        Constants.MILE_PER_HOUR -> {
                            weatherResponse = convertToMPH(weatherResponse)
                        }
                    }
                    _weatherInfo.value =
                        State.Success(weatherResponse, WeatherResponse::class.java)
                } else {
                    getWeatherInfoOverNetwork(
                        latitude = getLatitude()!!.toDouble(),
                        longitude = getLongitude()!!.toDouble(),
                        lang = lang
                    )
                }
            }

        }
    }

    private fun convertToCelsius(weatherResponse: WeatherResponse): WeatherResponse {
        for (item in weatherResponse.list) {
            item.main.temp = kelvinToCelsius(item.main.temp)
            item.main.feels_like = kelvinToCelsius(item.main.feels_like)
            item.main.temp_min = kelvinToCelsius(item.main.temp_min)
            item.main.temp_max = kelvinToCelsius(item.main.temp_max)
            item.main.temp_kf = kelvinToCelsius(item.main.temp_kf)
        }
        return weatherResponse
    }

    private fun convertToFahrenheit(weatherResponse: WeatherResponse): WeatherResponse {
        for (item in weatherResponse.list) {
            item.main.temp = kelvinToFahrenheit(item.main.temp)
            item.main.feels_like = kelvinToFahrenheit(item.main.feels_like)
            item.main.temp_min = kelvinToFahrenheit(item.main.temp_min)
            item.main.temp_max = kelvinToFahrenheit(item.main.temp_max)
            item.main.temp_kf = kelvinToFahrenheit(item.main.temp_kf)
        }
        return weatherResponse
    }

    fun convertToMPH(weatherResponse: WeatherResponse): WeatherResponse {
        for (item in weatherResponse.list) {
            item.wind.speed = mpsToMph(item.wind.speed)
        }
        return weatherResponse
    }

    fun getTemperatureUnit(): String? {
        return _repo.getString(Constants.TEMPERATURE_UNIT, "")
    }

    private fun getWindUnit(): String? {
        return _repo.getString(Constants.WIND_UNIT, "")
    }

    fun getLocation(): String? {
        return _repo.getString(Constants.LOCATION, "")
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getTodayForecast(
        list: List<WeatherInfo>
    ): List<WeatherInfo> {
        var weatherInfoList = mutableListOf<WeatherInfo>()
        for (i in 0..7) {
            weatherInfoList.add(list[i])
        }
        return weatherInfoList
    }

    private fun getFourDaysForecast(
        list: List<WeatherInfo>
    ): MutableList<WeatherInfo> {
        var weatherInfoList = mutableListOf<WeatherInfo>()
        for (i in 8..39 step 8) {
            weatherInfoList.add(list[i])
        }
        return weatherInfoList
    }
//
//    fun getCurrentWeather(forecastList: List<WeatherInfo>): WeatherInfo? {
//        val currentTimeMillis =
//            System.currentTimeMillis() / 1000 // Get current time in milliseconds
//        for (item in forecastList) {
//            if (item.dt >= currentTimeMillis) {
//                return item
//            }
//
//        }
//
//        return null // No current weather found
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayOfWeekFromDate(dateString: String, context: Context): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)

        val parsedDate = sdf.parse(dateString)
        calendar.time = parsedDate

        val inputDay = calendar.get(Calendar.DAY_OF_YEAR)

        return when {
            inputDay == today -> context.getString(R.string.today)
            inputDay == today + 1 -> context.getString(R.string.tomorrow)
            else -> SimpleDateFormat("EEEE").format(parsedDate)
        }
    }

    private fun kelvinToCelsius(kelvin: Double): Double {
        return (kelvin - 273.15).round(2)
    }

    private fun kelvinToFahrenheit(kelvin: Double): Double {
        return ((kelvin - 273.15) * 9 / 5 + 32).round(2)
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    private fun mpsToMph(mps: Double): Double {
        Log.i("here", "mpsToMph: ${mps}")
        return (mps * 2.23694).round(2)
    }


    fun getLanguage(): String? {
        return _repo.getString(Constants.LANGUAGE, "")
    }

    fun getLongitude(): String? {
        return _repo.getString(Constants.LONGITUDE, "")
    }

    fun getLatitude(): String? {
        return _repo.getString(Constants.LATITUDE, "")
    }

    fun setDataCached(isCached: Boolean) {
        _repo.saveBoolean(Constants.SESSION, isCached)
    }

    fun isDataCached(): Boolean? {
        return _repo.getBoolean(Constants.SESSION, false)
    }

    fun setLongitude(longitude: String) {
        _repo.saveString(Constants.LONGITUDE, longitude)
    }

    fun setLatitude(latitude: String) {
        _repo.saveString(Constants.LATITUDE, latitude)
    }

}