package com.nada.weatherapp.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nada.weatherapp.data.repo.WeatherInfoRepository
import com.nada.weatherapp.settings.view.ENGLISH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

const val TEMPERATURE_UNIT = "temperatureUnit"
const val WIND_UNIT = "windUnit"
const val LANGUAGE = "language"
const val LOCATION = "location"
const val LONGITUDE = "longitude"
const val LATITUDE = "latitude"

class SettingsViewModel(private val _repo: WeatherInfoRepository) : ViewModel() {
    //Backing Property
//    private var _language: MutableStateFlow<String> = MutableStateFlow<String>(ENGLISH)
//    val language: StateFlow<String> = _language

    private var _language: MutableSharedFlow<String> = MutableSharedFlow<String>(1)
    val language: SharedFlow<String> = _language

    init {
        val lang = getLanguage()
        viewModelScope.launch (Dispatchers.IO){
            if (lang != null) {
                if(lang == ""){
                    _language.emit(ENGLISH)
                }else{
                    _language.emit(lang)
                }
            }
        }
    }

    fun setTemperatureUnit(unit: String) {
        _repo.saveString(TEMPERATURE_UNIT, unit)
    }

    fun setWindUnit(unit: String) {
        _repo.saveString(WIND_UNIT, unit)
    }

    fun setLanguage(lang: String) {
        _repo.saveString(LANGUAGE, lang)
        viewModelScope.launch(Dispatchers.IO) {
            _language.emit(lang)
        }
    }

    fun setLocation(location:String){
        _repo.saveString(LOCATION,location)
    }

    fun setLongitude(longitude:String){
        _repo.saveString(LONGITUDE,longitude)
    }

    fun setLatitude(latitude:String){
        _repo.saveString(LATITUDE,latitude)
    }

    fun getTemperatureUnit(): String? {
        return _repo.getString(TEMPERATURE_UNIT, "")
    }

    fun getWindUnit(): String? {
        return _repo.getString(WIND_UNIT, "")
    }

    fun getLanguage(): String? {
        return _repo.getString(LANGUAGE, "")
    }

    fun getLocation() : String? {
        return _repo.getString(LOCATION,"")
    }
}