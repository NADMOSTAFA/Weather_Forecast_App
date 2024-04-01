package com.nada.weatherapp.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.nada.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class WeatherDaoTest {
    private lateinit var db: WeatherDatabase
    private lateinit var dao: WeatherDao

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        ).build()

        dao = db.getWeatherDao()
    }

    @After
    fun tearDao() {
        db.close()
    }

    @Test
    fun insertWeather_weatherData_retrieveWeatherDataInserted() = runBlockingTest {
        //Given
        val weather: WeatherResponse = WeatherResponse(lang = "en")
        //When
        dao.insertWeatherResponse(weather)
        var result = mutableListOf<WeatherResponse>()
        dao.getWeatherFromDB("en").take(1).collectLatest {
            result = it.toMutableList()
        }
        //Then
        assertThat(result.get(0), `is`(weather))
    }

    @Test
    fun insertWeather_duplicateWeatherDataAndBreakConstraints_replaceNewWeatherDataWithOldWeatherData() = runBlockingTest {
        //Given
        val weather: WeatherResponse = WeatherResponse(lang = "en")
        //When
        dao.insertWeatherResponse(weather)
        dao.insertWeatherResponse(weather)

        var result = mutableListOf<WeatherResponse>()
        dao.getWeatherFromDB("en").take(1).collectLatest {
            result = it.toMutableList()
        }
        //Then
        assertThat(result.size, `is`(1))
        assertThat(result.get(0), `is`(weather))
    }

    @Test
    fun deleteWeather_weatherData_retrieveWeatherDataWithoutTheRemovedWeather() = runBlockingTest {
        //Given
        val weatherInEnglish: WeatherResponse = WeatherResponse(lang = "en")
        //When
        dao.insertWeatherResponse(weatherInEnglish)
        dao.deleteWeatherResponse(weatherInEnglish)
        var result = mutableListOf<WeatherResponse>()
        dao.getWeatherFromDB("en").take(1).collectLatest {
            result = it.toMutableList()
        }
        //Then
        assertThat(result, `is`(emptyList()))
    }

    @Test
    fun deleteAllWeathers_emptyList() = runBlockingTest {
        //Given
        val weatherInEnglish: WeatherResponse = WeatherResponse(lang = "en")
        val weatherInArabic: WeatherResponse = WeatherResponse(lang = "ar")
        //When
        dao.insertWeatherResponse(weatherInEnglish)
        dao.insertWeatherResponse(weatherInArabic)
        dao.deleteAll()
        var result = mutableListOf<WeatherResponse>()
        dao.getAllWeatherFromDB().take(1).collectLatest {
            result = it.toMutableList()
        }
        //Then
        assertThat(result, `is`(emptyList()))
        assertThat(result.size, `is`(0))
    }
}

