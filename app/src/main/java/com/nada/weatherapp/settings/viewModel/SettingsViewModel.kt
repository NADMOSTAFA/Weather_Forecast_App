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
    private var _language: MutableSharedFlow<String> = MutableSharedFlow<String>(1)
    val language: SharedFlow<String> = _language

    fun deleteAll(){
        viewModelScope.launch (Dispatchers.IO){
            _repo.deleteAll()
        }
    }

    fun setLanguage(lang: String) {

        viewModelScope.launch(Dispatchers.IO) {
            Log.i("here", "setLanguage: emitted ${lang}")
            _language.emit(lang)
            _repo.saveString(Constants.LANGUAGE,lang)
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
}