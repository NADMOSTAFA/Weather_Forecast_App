package com.nada.weatherapp.favorites.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nada.weatherapp.MainRule
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.repo.FakeWeatherInfoRepository
import com.nada.weatherapp.data.repo.WeatherInfoRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FavoriteViewModelTest {
    lateinit var repo: WeatherInfoRepository
    lateinit var viewModel: FavoriteViewModel

    @get:Rule
    val mainRule = MainRule()

    @Before
    fun setUp() {
        repo = FakeWeatherInfoRepository()
        viewModel = FavoriteViewModel(repo)
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun getStoredFavoriteWeather_emptyList() = runBlockingTest{
        viewModel.getStoredWeather()

        // Then
        val result = mutableListOf<FavoriteWeather>()

        viewModel.favWeatherInfo.take(1).collectLatest {
            result.addAll(it)
        }
        assertThat(result, `is`(emptyList()))
    }

    @Test
    fun getStoredFavoriteWeather_favoriteWeatherList() = runBlockingTest {
        // Given
        val favoriteWeather1 =
            FavoriteWeather(30.0444, 31.2357, "Egypt", "Cairo")     // Cairo, Egypt
        val favoriteWeather2 =
            FavoriteWeather(37.9838, 23.7275, "Greece", "Athens")    // Athens, Greece
        val favoriteWeatherList = mutableListOf<FavoriteWeather>(favoriteWeather1,favoriteWeather2)
        // When
        viewModel.insertWeather(favoriteWeather1)
        viewModel.insertWeather(favoriteWeather2)
        viewModel.getStoredWeather()

        // Then
        val result = mutableListOf<FavoriteWeather>()

        viewModel.favWeatherInfo.take(1).collectLatest {
            result.addAll(it)
        }
        assertThat(result, `is`(favoriteWeatherList))
        assertThat(result.size, `is`(favoriteWeatherList.size))


    }

    @Test
    fun insertFavoriteWeather_favoriteWeather_weatherDataInserted() = runBlockingTest {
        // Given
        val favoriteWeather1 =
            FavoriteWeather(30.0444, 31.2357, "Egypt", "Cairo")     // Cairo, Egypt
        val favoriteWeather2 =
            FavoriteWeather(37.9838, 23.7275, "Greece", "Athens")    // Athens, Greece

        // When
        viewModel.insertWeather(favoriteWeather1)
        viewModel.insertWeather(favoriteWeather2)
        viewModel.getStoredWeather()

        // Then
        val result = mutableListOf<FavoriteWeather>()

        viewModel.favWeatherInfo.take(1).collectLatest {
            result.addAll(it)
        }

        assertThat(result.size, `is`(2))
        assertThat(result[0], `is`(favoriteWeather1))
        assertThat(result[1], `is`(favoriteWeather2))
    }

    @Test
    fun deleteFavoriteWeather_favoriteWeather_weatherDataRemoved() =  runBlockingTest() {
        // Given
        val favoriteWeather1 =
            FavoriteWeather(30.0444, 31.2357, "Egypt", "Cairo")     // Cairo, Egypt
        val favoriteWeather2 =
            FavoriteWeather(37.9838, 23.7275, "Greece", "Athens")    // Athens, Greece

        // When
        viewModel.insertWeather(favoriteWeather1)
        viewModel.insertWeather(favoriteWeather2)
        viewModel.removeWeather(favoriteWeather2)
        viewModel.getStoredWeather()

        // Then
        val result = mutableListOf<FavoriteWeather>()

        viewModel.favWeatherInfo.take(1).collectLatest {
            result.addAll(it)
        }

        assertThat(result.size, `is`(1))
        assertThat(result.get(0), `is`(favoriteWeather1))
    }

}