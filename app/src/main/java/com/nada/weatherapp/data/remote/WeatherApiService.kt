package com.nada.weatherapp.data.remote

import com.nada.weatherapp.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String,
        @Query("units") units: String
    ): WeatherResponse
}