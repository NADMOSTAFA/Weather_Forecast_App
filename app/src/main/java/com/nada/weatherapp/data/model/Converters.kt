package com.nada.weatherapp.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromWeatherResponse(weatherResponse: WeatherResponse): String {
        return gson.toJson(weatherResponse)
    }

    @TypeConverter
    fun toWeatherResponse(weatherResponseString: String): WeatherResponse {
        return gson.fromJson(weatherResponseString, WeatherResponse::class.java)
    }

    @TypeConverter
    fun fromWeatherInfoList(weatherInfoList: List<WeatherInfo>): String {
        return gson.toJson(weatherInfoList)
    }

    @TypeConverter
    fun toWeatherInfoList(weatherInfoListString: String): List<WeatherInfo> {
        val listType = object : TypeToken<List<WeatherInfo>>() {}.type
        return gson.fromJson(weatherInfoListString, listType)
    }

    @TypeConverter
    fun fromClouds(clouds: Clouds): String {
        return gson.toJson(clouds)
    }

    @TypeConverter
    fun toClouds(cloudsString: String): Clouds {
        return gson.fromJson(cloudsString, Clouds::class.java)
    }

    @TypeConverter
    fun fromCoordinate(coord: Coordinate): String {
        return gson.toJson(coord)
    }

    @TypeConverter
    fun toCoordinate(coordString: String): Coordinate {
        return gson.fromJson(coordString, Coordinate::class.java)
    }

    @TypeConverter
    fun fromMainInfo(mainInfo: MainInfo): String {
        return gson.toJson(mainInfo)
    }

    @TypeConverter
    fun toMainInfo(mainInfoString: String): MainInfo {
        return gson.fromJson(mainInfoString, MainInfo::class.java)
    }

    @TypeConverter
    fun fromRain(rain: Rain): String {
        return gson.toJson(rain)
    }

    @TypeConverter
    fun toRain(rainString: String): Rain {
        return gson.fromJson(rainString, Rain::class.java)
    }

    @TypeConverter
    fun fromSystem(sys: System): String {
        return gson.toJson(sys)
    }

    @TypeConverter
    fun toSystem(sysString: String): System {
        return gson.fromJson(sysString, System::class.java)
    }

    @TypeConverter
    fun fromWeatherList(weatherList: List<Weather>): String {
        return gson.toJson(weatherList)
    }

    @TypeConverter
    fun toWeatherList(weatherListString: String): List<Weather> {
        val listType = object : TypeToken<List<Weather>>() {}.type
        return gson.fromJson(weatherListString, listType)
    }

    @TypeConverter
    fun fromWind(wind: Wind): String {
        return gson.toJson(wind)
    }

    @TypeConverter
    fun toWind(windString: String): Wind {
        return gson.fromJson(windString, Wind::class.java)
    }

    @TypeConverter
    fun fromCity(city: City): String {
        return gson.toJson(city)
    }

    @TypeConverter
    fun toCity(cityString: String): City {
        return gson.fromJson(cityString, City::class.java)
    }
}
