package com.nada.weatherapp.data.remote

import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ForecastRemoteDataSourceImpl private constructor() : ForecastRemoteDataSource {

    private val weatherApiService: WeatherApiService by lazy {
        RetrofitHelper.getInstance().create(WeatherApiService::class.java)
    }

    override fun getWeatherInfoOverNetwork(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Flow<WeatherResponse> {
        return flow {
            emit(weatherApiService.getForecast(latitude, longitude, apiKey, lang, units))
        }
    }

    companion object {
        private var instance: ForecastRemoteDataSourceImpl? = null
        fun getInstance(): ForecastRemoteDataSourceImpl {
            return instance ?: synchronized(this) {
                val temp = ForecastRemoteDataSourceImpl()
                instance = temp
                temp
            }
        }
    }


}