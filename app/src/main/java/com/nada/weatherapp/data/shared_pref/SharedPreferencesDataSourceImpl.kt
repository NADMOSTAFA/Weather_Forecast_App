package com.nada.weatherapp.data.shared_pref

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesDataSourceImpl private constructor(context: Context) :
    SharedPreferencesDataSource {
    companion object {
        private const val PREFS_NAME = "weather_app_prefs"
        private var instance: SharedPreferencesDataSourceImpl? = null

        @Synchronized
        fun getInstance(context: Context): SharedPreferencesDataSourceImpl {
            return instance ?: synchronized(this){
               var temp = SharedPreferencesDataSourceImpl(context)
                instance = temp
                temp
            }
        }
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String, defaultValue: String): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun removePreference(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}