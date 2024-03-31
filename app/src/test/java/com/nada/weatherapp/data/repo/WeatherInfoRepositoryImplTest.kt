package com.nada.weatherapp.data.repo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nada.weatherapp.data.local.FakeLocalDataSource
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.remote.FakeRemoteDataSource
import com.nada.weatherapp.data.shared_pref.FakePreferenceDataSource
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherInfoRepositoryImplTest {
    lateinit var repository: WeatherInfoRepositoryImpl
    lateinit var localDataList: List<FavoriteWeather>

    @Before
    fun setUp() {
        val remoteDataSource = FakeRemoteDataSource()
        localDataList = listOf<FavoriteWeather>(
            favoriteWeather1,
            favoriteWeather2,
            favoriteWeather3,
            favoriteWeather4
        )
        val localDataSource = FakeLocalDataSource(localDataList.toMutableList())
        val preferenceDataSource = FakePreferenceDataSource(preferenceMutableMap)
        repository = WeatherInfoRepositoryImpl(
            remoteDataSource,
            preferenceDataSource,
            localDataSource,
        )
    }

    private val favoriteWeather1 =
        FavoriteWeather(30.0444, 31.2357, "Egypt", "Cairo")     // Cairo, Egypt
    private val favoriteWeather2 =
        FavoriteWeather(37.9838, 23.7275, "Greece", "Athens")    // Athens, Greece
    private val favoriteWeather3 =
        FavoriteWeather(37.5665, 126.9780, "South Korea", "Seoul") // Seoul, South Korea
    private val favoriteWeather4 = FavoriteWeather(51.5074, -0.1278, "England", "London")  //England


    val preferenceMutableMap = mutableMapOf(
        "key1" to "value1",
        "gender" to "female",
    )

    @Test
    fun getSavedWeathers_fourSavedWeathers() = runBlockingTest {
        var result = mutableListOf<FavoriteWeather>()
        repository.getSavedWeathers().collect { list ->
            result = list.toMutableList()
        }
        assertThat(result, `is`(localDataList))
    }

    @Test
    fun insertFavoriteWeather_weather_newAddedWeather() = runBlockingTest {
        val favoriteWeather5 =
            FavoriteWeather(40.7128, -74.0060, "USA", "New York City") // New York City, USA
        repository.insertFavoriteWeather(favoriteWeather5)
        var result = mutableListOf<FavoriteWeather>()
        repository.getSavedWeathers().collect { list ->
            result = list.toMutableList()
        }
        assertThat(result[4], `is`(favoriteWeather5))
        assertThat(result.size, `is`(5))
    }

    @Test
    fun deleteFavoriteWeather_weather_removedWeather() = runBlockingTest {
        var result = mutableListOf<FavoriteWeather>()
        repository.getSavedWeathers().collect { list ->
            result = list.toMutableList()
        }
        assertThat(result.size, `is`(4))
        repository.deleteFavoriteWeather(favoriteWeather4)
        repository.getSavedWeathers().collect { list ->
            result = list.toMutableList()
        }
        assertThat(result.size, `is`(3))
    }

    @Test
    fun saveString_keyValuePair_savePair() {
        repository.saveString("gender", "female")
        assertThat(
            repository.getString("gender", ""),
            `is`("female")
        )
    }

    @Test
    fun getString_wrongKey_defaultValue() {
        assertThat(
            repository.getString("language", "en"),
            `is`("en")
        )
    }


}