package com.nada.weatherapp.data.shared_pref

class FakePreferenceDataSource(var map: MutableMap<String, String>):SharedPreferencesDataSource {
    override fun saveString(key: String, value: String) {
        map[key] = value
    }

    override fun getString(key: String, defaultValue: String): String {
       return map[key] ?: defaultValue
    }

    override fun saveInt(key: String, value: Int) {
        TODO("Not yet implemented")

    }

    override fun getInt(key: String, defaultValue: Int): Int {
        TODO("Not yet implemented")
    }

    override fun saveBoolean(key: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun removePreference(key: String) {
        TODO("Not yet implemented")
    }
}