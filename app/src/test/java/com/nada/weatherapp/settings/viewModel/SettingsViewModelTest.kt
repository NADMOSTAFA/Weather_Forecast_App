package com.nada.weatherapp.settings.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nada.weatherapp.MainRule
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.data.model.WeatherResponse
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
class SettingsViewModelTest{
    lateinit var repo: WeatherInfoRepository
    lateinit var viewModel: SettingsViewModel

    @get:Rule
    val mainRule = MainRule()

    @Before
    fun setUp() {
        repo = FakeWeatherInfoRepository()
        viewModel = SettingsViewModel(repo)
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun setSettingConfiguration_gpsLocation_locationIsGps(){
        viewModel.setSettingConfiguration(Constants.LOCATION,Constants.GPS)
        val result = viewModel.getSettingConfiguration(Constants.LOCATION,"")
        assertThat(result,`is`(Constants.GPS))
    }

    @Test
    fun setSettingConfiguration_session_sessionStartedSuccessfully(){
        viewModel.setSettingConfiguration(Constants.SESSION,false)
        val result = repo.getBoolean(Constants.SESSION,true)
        assertThat(result,`is`(false))
    }

    @Test
    fun getSettingConfiguration_wrongKey_getDefaultValue(){
        val result = viewModel.getSettingConfiguration(Constants.LANGUAGE,"")
        assertThat(result,`is`(""))
    }

    @Test
    fun setLanguage_englishLanguage_languageIsEnglish()= runBlockingTest{
        viewModel.setLanguage(Constants.ENGLISH)
        var language = ""
        viewModel.language.take(1).collectLatest {
            language = it
        }
        assertThat(language , `is`(Constants.ENGLISH))
    }

    @Test
    fun deleteAll_emptyList() = runBlockingTest{
        repo.insertWeatherResponse(WeatherResponse("en"))
        var result = mutableListOf<WeatherResponse>()
        viewModel.deleteAll()
        repo.getWeatherFromDB("en").take(1).collectLatest {
            result = it.toMutableList()
        }
        assertThat(result ,`is`(emptyList()))
    }

}