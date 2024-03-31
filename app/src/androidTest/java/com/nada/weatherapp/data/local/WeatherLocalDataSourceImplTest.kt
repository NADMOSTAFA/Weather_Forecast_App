package com.nada.weatherapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@MediumTest
@RunWith(AndroidJUnit4::class)
class WeatherLocalDataSourceImplTest {
    lateinit var db: WeatherDatabase
    lateinit var localDataSource: WeatherLocalDataSourceImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        ).allowMainThreadQueries().build()

        localDataSource = WeatherLocalDataSourceImpl(
            db.getWeatherDao(),
            db.getFavoriteWeatherDao(),
            db.getAlertDao()
        )
    }

    @After
    fun tearDb() {
        db.close()
    }

    @Test
    fun insertWeather_weatherData_retrieveWeatherDataInserted() = runTest {
        //Given
        val weather: WeatherResponse = WeatherResponse(lang = "en")
        //When
        localDataSource.insertWeatherResponse(weather)
        var result = WeatherResponse()
        localDataSource.getWeatherFromDB("en").collect { data ->
            result = data.get(0)
        }
        //Then
        assertThat(result, `is`(weather))
    }

    @Test
    fun deleteWeather_weatherData_retrieveWeatherDataWithoutTheRemovedWeather() = runTest {
        //Given
        val weatherInEnglish: WeatherResponse = WeatherResponse(lang = "en")
        //When
        localDataSource.insertWeatherResponse(weatherInEnglish)
        localDataSource.deleteWeatherResponse(weatherInEnglish)
        var result = mutableListOf<WeatherResponse>()
        localDataSource.getWeatherFromDB("en").collect { data ->
            result = data.toMutableList()
        }
        //Then
        assertThat(result, `is`(emptyList()))
    }

    @Test
    fun deleteAllWeathers_emptyList() = runTest {
        //Given
        val weatherInEnglish: WeatherResponse = WeatherResponse(lang = "en")
        val weatherInArabic: WeatherResponse = WeatherResponse(lang = "ar")
        //When
        localDataSource.insertWeatherResponse(weatherInEnglish)
        localDataSource.insertWeatherResponse(weatherInArabic)
        localDataSource.deleteAll()
        var result = mutableListOf<WeatherResponse>()
        localDataSource.getAllWeatherFromDB().collect { data ->
            result = data.toMutableList()
        }
        //Then
        assertThat(result, `is`(emptyList()))
        assertThat(result.size, `is`(0))
    }

    @Test
    fun insertWeatherAlert_weatherAlert_retrieveWeatherAlert()= runTest(){
        //Given
        val weatherAlert1: WeatherAlert = WeatherAlert("1","1-1-2023" ,"3:00 am")
        val weatherAlert2: WeatherAlert = WeatherAlert("2","1-1-2023" ,"3:00 am")

        //When
        localDataSource.insertWeatherAlert(weatherAlert1)
        localDataSource.insertWeatherAlert(weatherAlert2)

        var result = mutableListOf<WeatherAlert>()
        localDataSource.getWeatherAlerts().collect { data ->
            result = data.toMutableList()
        }
        //Then
        assertThat(result.size, `is`(2))
        assertThat(result.get(0), `is`(weatherAlert1))
    }

    @Test
    fun deleteWeatherAlert_weatherAlert_retrieveWeatherAlert()= runTest(){
        //Given
        val weatherAlert1: WeatherAlert = WeatherAlert("1","1-1-2023" ,"3:00 am")
        val weatherAlert2: WeatherAlert = WeatherAlert("2","1-1-2023" ,"3:00 am")

        //When
        localDataSource.insertWeatherAlert(weatherAlert1)
        localDataSource.insertWeatherAlert(weatherAlert2)
        localDataSource.deleteWeatherAlert(weatherAlert1)
        var result = mutableListOf<WeatherAlert>()
        localDataSource.getWeatherAlerts().collect { data ->
            result = data.toMutableList()
        }

        //Then
        assertThat(result.size, `is`(1))
        assertThat(result.get(0), `is`(weatherAlert2))
    }


}