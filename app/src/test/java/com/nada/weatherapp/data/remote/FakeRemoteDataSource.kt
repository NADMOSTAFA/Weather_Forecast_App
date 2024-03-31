package com.nada.weatherapp.data.remote

import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

class FakeRemoteDataSource :ForecastRemoteDataSource {
    override fun getWeatherInfoOverNetwork(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Flow<WeatherResponse> {
        TODO("Not yet implemented")
    }
}