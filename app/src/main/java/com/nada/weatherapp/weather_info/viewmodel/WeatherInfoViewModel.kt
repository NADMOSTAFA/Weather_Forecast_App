package com.nada.weatherapp.weather_info.viewmodel

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.ApiState
import com.nada.weatherapp.data.model.WeatherInfo
import com.nada.weatherapp.data.repo.WeatherInfoRepository
import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.settings.view.IMPERIAL
import com.nada.weatherapp.settings.view.METER_PER_SECOND
import com.nada.weatherapp.settings.view.MILE_PER_HOUR
import com.nada.weatherapp.settings.viewModel.LANGUAGE
import com.nada.weatherapp.settings.viewModel.LATITUDE
import com.nada.weatherapp.settings.viewModel.LOCATION
import com.nada.weatherapp.settings.viewModel.LONGITUDE
import com.nada.weatherapp.settings.viewModel.TEMPERATURE_UNIT
import com.nada.weatherapp.settings.viewModel.WIND_UNIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherInfoViewModel(private val _repo: WeatherInfoRepository) : ViewModel() {
    //Backing Property
    private var _weatherInfo: MutableStateFlow<ApiState<WeatherResponse>> =
        MutableStateFlow<ApiState<WeatherResponse>>(ApiState.Loading)
    val weatherInfo: StateFlow<ApiState<WeatherResponse>> = _weatherInfo
//    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    var longitude: Double? = null
//    var latitude: Double? = null

    fun getLocationFromMap(){
        Log.i("here", "getLocationFromMap: longitude ${getLongitude()} latitude ${getLatitude()}")
            val longitude = getLongitude()!!.toDouble()
            val  latitude = getLatitude()!!.toDouble()
            getUserLocation(latitude,longitude )
    }

    fun getUserLocation(latitude: Double, longitude: Double){
        Log.i("here", "sendParamToGetWeatherInfoOverNetWork: Entered long $longitude lat $latitude")
        val unit = getTemperatureUnit()
        val lang = getLanguage()
        if (unit != null && lang != null) {
            Log.i("here", "sendParamToGetWeatherInfoOverNetWork: 1")
            getWeatherInfoOverNetwork(
                latitude!!,
                longitude!!,
                "47c1440938387595e380110ff3ce9e84",
                lang,
                unit
            )
        } else if (unit != null) {
            Log.i("here", "sendParamToGetWeatherInfoOverNetWork: 2")
            getWeatherInfoOverNetwork(
                latitude!!,
                longitude!!,
                "47c1440938387595e380110ff3ce9e84",
                "en",
                unit
            )

        } else if (lang != null) {
            Log.i("here", "sendParamToGetWeatherInfoOverNetWork: 3")

            getWeatherInfoOverNetwork(
                30.033333,
                31.233334,
                "47c1440938387595e380110ff3ce9e84",
                lang,
                "standard"
            )
        } else {
            Log.i("here", "sendParamToGetWeatherInfoOverNetWork: 4")

            getWeatherInfoOverNetwork(
                latitude!!,
                longitude!!,
                "47c1440938387595e380110ff3ce9e84",
                "en",
                "standard"
            )
        }
    }

    private fun getWeatherInfoOverNetwork(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ) {
        Log.i("here", "getWeatherInfoOverNetwork: Entered From Real Call")
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getWeatherInfoOverNetwork(latitude, longitude, apiKey, lang, units).catch { e ->
                _weatherInfo.value = ApiState.Failure(e)
            }.collect { data ->
//                 Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour
                when (getWindUnit()) {
                    METER_PER_SECOND -> {
                        if (getTemperatureUnit() == IMPERIAL) {
                            val temp = mphToMps(data.list.get(0).wind.speed)
                            data.list.get(0).wind.speed = temp
                        }
                    }

                    MILE_PER_HOUR -> {
                        if (getTemperatureUnit() != IMPERIAL) {
                            val temp = mpsToMph(data.list.get(0).wind.speed)
                            data.list.get(0).wind.speed = temp

                        }
                    }
                }
                _weatherInfo.value = ApiState.Success(data, WeatherResponse::class.java)

            }
        }

    }


    private fun getTemperatureUnit(): String? {
        return _repo.getString(TEMPERATURE_UNIT, "")
    }

    private fun getWindUnit(): String? {
        return _repo.getString(WIND_UNIT, "")
    }

    fun getLocation(): String? {
        return _repo.getString(LOCATION, "")
    }


    fun getCountryName(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                return addresses[0].countryName
            }
        } catch (e: IOException) {
            Log.i("Mo", "geocodeLocation: " + e.message)
            e.printStackTrace()
        }
        return null
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun getTodayForecast(
        weatherInfoList: MutableList<WeatherInfo>,
        list: List<WeatherInfo>
    ): MutableList<WeatherInfo> {
        for (i in 0..7) {
            weatherInfoList.add(list[i])
        }
        return weatherInfoList
    }

    fun getFiveDaysForecast(
        weatherInfoList: MutableList<WeatherInfo>,
        list: List<WeatherInfo>
    ): MutableList<WeatherInfo> {
        for (i in 8..39 step 8) {
            Log.i("here", "getFiveDaysForecast: i = $i")
            weatherInfoList.add(list[i])
        }
        return weatherInfoList
    }

    fun getCurrentWeather(forecastList: List<WeatherInfo>): WeatherInfo? {
        Log.i("here", "getCurrentWeather:${forecastList.size} ")
        val currentTimeMillis =
            System.currentTimeMillis() / 1000 // Get current time in milliseconds
        Log.i("here", "getCurrentWeather: $currentTimeMillis")

        for (item in forecastList) {
            if (item.dt >= currentTimeMillis) {
                return item
            }

        }

        return null // No current weather found
    }

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

    fun kelvinToCelsius(kelvin: Double): Double {
        return kelvin - 273.15
    }

    fun kelvinToFahrenheit(kelvin: Double): Double {
        return (kelvin - 273.15) * 9 / 5 + 32
    }

    private fun mpsToMph(mps: Double): Double {
        return String.format("%.2f", mps * 2.23694).toDouble()
    }

    // Convert miles per hour (mph) to meters per second (m/s) with two decimal places
    private fun mphToMps(mph: Double): Double {
        return String.format("%.2f", mph / 2.23694).toDouble()
    }

    private fun getLanguage(): String? {
        return _repo.getString(LANGUAGE, "")
    }

    fun getLongitude(): String? {
        return _repo.getString(LONGITUDE, "")
    }

    fun getLatitude() : String? {
        return _repo.getString(LATITUDE,"")
    }

}