package com.nada.weatherapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.settings.view.ARABIC
import com.nada.weatherapp.settings.view.ENGLISH
import com.nada.weatherapp.settings.viewModel.SettingsViewModel
import com.nada.weatherapp.settings.viewModel.SettingsViewModelFactory
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle
import java.util.Locale


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var drawerLayout: DuoDrawerLayout? = null
    lateinit var navController: NavController

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = findNavController(this, R.id.nav_host_fragment)
        settingsViewModelFactory = SettingsViewModelFactory(
            WeatherInfoRepositoryImpl.getInstance(
                ForecastRemoteDataSourceImpl.getInstance(),
                SharedPreferencesDataSourceImpl.getInstance(this)
            )
        )
        settingsViewModel =
            ViewModelProvider(this, settingsViewModelFactory).get(SettingsViewModel::class.java)
        init();

        lifecycleScope.launch(Dispatchers.IO) {
            settingsViewModel.language.collect { language ->
                val primaryLocale: Locale = this@MainActivity.resources.configuration.locales[0]
                val locale: String = primaryLocale.language
                Log.i("here", "onCreate: locale $locale language $language  ")
                if (!locale.equals(language)) {
                    Log.i("here", "language: $language")
                    val newLocale = when (language) {
                        ARABIC -> {
                            Locale("ar")
                        }

                        ENGLISH -> {
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

//    override fun onStart() {
//        super.onStart()
//
//    }

    private fun updateLocale(context: Context, locale: Locale) {
        Log.i("here", "updateLocale: $locale ")
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun refreshUI() {
        recreate()
    }

    private fun init() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
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
            if (destination.id == R.id.fiveDaysForecast2) {
                supportActionBar?.hide()
                drawerLayout?.setDrawerLockMode(DuoDrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                supportActionBar?.show()
                drawerLayout?.setDrawerLockMode(DuoDrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
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


}