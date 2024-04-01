package com.nada.weatherapp

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.data.local.WeatherLocalDataSourceImpl
import com.nada.weatherapp.data.local.WeatherDatabase
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.settings.viewModel.SettingsViewModel
import com.nada.weatherapp.settings.viewModel.SettingsViewModelFactory
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle
import java.util.Locale


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var drawerLayout: DuoDrawerLayout? = null
    private lateinit var navController: NavController

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory
    private lateinit var toolbar : Toolbar
    private lateinit var themedContext: ContextThemeWrapper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModelFactory = SettingsViewModelFactory(
            WeatherInfoRepositoryImpl.getInstance(
                ForecastRemoteDataSourceImpl.getInstance(),
                SharedPreferencesDataSourceImpl.getInstance(this),
                WeatherLocalDataSourceImpl(
                    WeatherDatabase.getInstance(this).getWeatherDao(),
                    WeatherDatabase.getInstance(this).getFavoriteWeatherDao(),
                    WeatherDatabase.getInstance(this).getAlertDao()
                )
            )
        )
        settingsViewModel =
            ViewModelProvider(this, settingsViewModelFactory).get(SettingsViewModel::class.java)
        setUp()
        setContentView(R.layout.activity_main)
        navController = findNavController(this, R.id.nav_host_fragment)
        init();
        Log.i("here", "onCreate: main activity ${settingsViewModel.getSettingConfiguration(Constants.LOCATION,"")}")
        lifecycleScope.launch(Dispatchers.IO) {
            settingsViewModel.language.collectLatest { language ->
                val primaryLocale = this@MainActivity.resources.configuration.locales[0]
                val locale: String = primaryLocale.language
                Log.i(
                    "here",
                    "onCreate: locale ${locale} language ${language} if ${!locale.equals(language)}"
                )
                if (!locale.equals(language)) {
                    val newLocale = when (language) {
                        Constants.ARABIC -> {
                            Locale("ar")
                        }

                        Constants.ENGLISH -> {
                            Locale("en")
                        }

                        else -> Locale.getDefault()
                    }


                    Locale.setDefault(newLocale)
                    val res = this@MainActivity.resources
                    val config = Configuration(res.configuration)
                    config.setLocale(newLocale)
                    res.updateConfiguration(config, res.displayMetrics)
                    withContext(Dispatchers.Main) {
                        this@MainActivity.recreate()
                        if (language == "en") {
                            drawerLayout?.gravity = GravityCompat.START
                            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
                        } else {
                            drawerLayout?.gravity = GravityCompat.START
                            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
                        }
                    }

                }

            }
        }

    }

    private fun init() {
         toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        drawerLayout = findViewById<View>(R.id.drawer) as DuoDrawerLayout
        val drawerToggle = DuoDrawerToggle(
            this, drawerLayout, toolbar,
            nl.psdcompany.psd.duonavigationdrawer.R.string.navigation_drawer_open,
            nl.psdcompany.psd.duonavigationdrawer.R.string.navigation_drawer_close
        )

        drawerLayout!!.setDrawerListener(drawerToggle)
        drawerToggle.syncState()
        val contentView = drawerLayout!!.getContentView()
        val menuView = drawerLayout!!.getMenuView()
        val ll_Home = menuView.findViewById<LinearLayout>(R.id.Home)
        val ll_Favorites = menuView.findViewById<LinearLayout>(R.id.Favorites)
        val ll_Settings = menuView.findViewById<LinearLayout>(R.id.Settings)
        val ll_Alerts = menuView.findViewById<LinearLayout>(R.id.Alerts)
        ll_Home.setOnClickListener(this)
        ll_Favorites.setOnClickListener(this)
        ll_Settings.setOnClickListener(this)
        ll_Alerts.setOnClickListener(this)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.googleMapFragment || destination.id == R.id.fiveDaysForecast2) {
                supportActionBar?.hide()
                drawerLayout?.setDrawerLockMode(DuoDrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }



    override fun onRestart() {
        settingsViewModel.setSettingConfiguration(Constants.SESSION,true)
        super.onRestart()
        Log.i("here", "onRestart: ")
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.Home -> navigateToFragment(R.id.home2)
            R.id.Favorites -> navigateToFragment(R.id.favorite)
            R.id.Settings -> navigateToFragment(R.id.setting)
            R.id.Alerts -> navigateToFragment(R.id.alert)
            else -> Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
    }

    private fun navigateToFragment(fragmentId: Int) {
        navController.navigate(
            fragmentId,
            null,
            NavOptions.Builder().setPopUpTo(R.id.home2, false).build()
        )
    }

    private fun setUp() {
        val primaryLocale = this@MainActivity.resources.configuration.locales[0]
        val locale: String = primaryLocale.language
        val language = settingsViewModel.getSettingConfiguration(Constants.LANGUAGE,"")
        if (!locale.equals(language)) {
            val newLocale = when (language) {
                Constants.ARABIC -> {
                    Locale("ar")
                }

                Constants.ENGLISH -> {
                    Locale("en")
                }
                // Add more languages as needed
                else -> Locale.getDefault() // Fallback to device's default language
            }
            Locale.setDefault(newLocale)
            val res = this@MainActivity.resources
            val config = Configuration(res.configuration)
            config.setLocale(newLocale)
            res.updateConfiguration(config, res.displayMetrics)
            if (language == "en") {
                drawerLayout?.gravity = GravityCompat.START
                window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
            } else {
                drawerLayout?.gravity = GravityCompat.START
                window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
        }
    }

    fun hideActionBarAndDrawer() {
        supportActionBar?.hide()
        drawerLayout?.setDrawerLockMode(DuoDrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    }

    fun openDrawerLayout(){
        drawerLayout?.openDrawer()
        drawerLayout?.setDrawerLockMode(DuoDrawerLayout.LOCK_MODE_UNLOCKED)
    }
    fun showActionBarAndDrawer() {
        supportActionBar?.show()
        drawerLayout?.setDrawerLockMode(DuoDrawerLayout.LOCK_MODE_UNLOCKED)
    }

}