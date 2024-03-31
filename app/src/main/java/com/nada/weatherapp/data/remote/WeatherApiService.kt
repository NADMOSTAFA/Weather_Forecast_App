package com.nada.weatherapp.data.remote

import com.nada.weatherapp.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = "47c1440938387595e380110ff3ce9e84",
        @Query("lang") lang: String,
        @Query("units") units: String = "standard"
    ): WeatherResponse
}