package com.nada.weatherapp.settings.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.data.repo.WeatherInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch



class SettingsViewModel(private val _repo: WeatherInfoRepository) : ViewModel() {
    //Backing Property
//    val lang = getLanguage()
//    private var _language: MutableStateFlow<String> = MutableStateFlow<String>(lang!!)
//    val language: StateFlow<String> = _language

    private var _language: MutableSharedFlow<String> = MutableSharedFlow<String>(1)
    val language: SharedFlow<String> = _language

//    init {
//        val lang = getLanguage()
//        viewModelScope.launch (Dispatchers.IO){
//            if (lang != null) {
//                if(lang == ""){
//                    _language.emit(ENGLISH)
//                }else{
//                    _language.emit(lang)
//                }
//            }
//        }
//    }


    fun deleteAll(){
        viewModelScope.launch (Dispatchers.IO){
            _repo.deleteAll()
        }
    }

//    fun setTemperatureUnit(unit: String) {
//        _repo.saveString(TEMPERATURE_UNIT, unit)
//    }
//
//    fun setWindUnit(unit: String) {
//        _repo.saveString(WIND_UNIT, unit)
//    }

    fun setLanguage(lang: String) {

        viewModelScope.launch(Dispatchers.IO) {
            Log.i("here", "setLanguage: emitted ${lang}")
            _language.emit(lang)
            _repo.saveString(Constants.LANGUAGE,lang)
//            _language.value = lang
        }
    }

     fun setSettingConfiguration(key: String, value: String) {
         _repo.saveString(key,value)
    }
    fun setSettingConfiguration(key: String, value: Boolean) {
        _repo.saveBoolean(key,value)
    }
     fun getSettingConfiguration(key: String, defaultValue: String): String? {
         return _repo.getString(key,defaultValue)
    }
//    fun getSettingConfiguration(key: String, defaultValue: Boolean): Boolean {
//        return _repo.getBoolean(key,defaultValue)
//    }

//    fun setLocation(location:String){
//        _repo.saveString(LOCATION,location)
//    }
//
//    fun setLongitude(longitude:String){
//        _repo.saveString(LONGITUDE,longitude)
//    }
//
//    fun setLatitude(latitude:String){
//        _repo.saveString(LATITUDE,latitude)
//    }
//
//    fun setDataCached(isCached : Boolean){
//        _repo.saveBoolean(SESSION,isCached)
//    }
//
//
//    fun getTemperatureUnit(): String? {
//        return _repo.getString(TEMPERATURE_UNIT, "")
//    }
//
//    fun getWindUnit(): String? {
//        return _repo.getString(WIND_UNIT, "")
//    }
//
//    fun getLanguage(): String? {
//        return _repo.getString(LANGUAGE, "")
//    }
//
//    fun getLocation() : String? {
//        return _repo.getString(LOCATION,"")
//    }

//    fun isDataCached() : Boolean? {
//        return _repo.getBoolean(SESSION,false)
//    }
//
//    fun removeSession(key: String) {
//        _repo.removePreference(key)
//    }

}