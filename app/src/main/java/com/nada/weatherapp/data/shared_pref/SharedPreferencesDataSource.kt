package com.nada.weatherapp.data.shared_pref

interface SharedPreferencesDataSource {
    fun saveString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String?
    fun saveInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int): Int
    fun saveBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun removePreference(key: String)
}