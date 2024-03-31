package com.nada.weatherapp.settings.view

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.nada.weatherapp.MainActivity
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.Utils.Source
import com.nada.weatherapp.data.local.WeatherLocalDataSourceImpl
import com.nada.weatherapp.data.local.WeatherDatabase
import com.nada.weatherapp.databinding.FragmentSettingBinding
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.settings.viewModel.SettingsViewModel
import com.nada.weatherapp.settings.viewModel.SettingsViewModelFactory
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl




class Setting : Fragment() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory

    private lateinit var binding: FragmentSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModelFactory = SettingsViewModelFactory(
            WeatherInfoRepositoryImpl.getInstance(
                ForecastRemoteDataSourceImpl.getInstance(),
                SharedPreferencesDataSourceImpl.getInstance(requireContext()),
                WeatherLocalDataSourceImpl(
                    WeatherDatabase.getInstance(requireContext()).getWeatherDao(),
                    WeatherDatabase.getInstance(requireContext()).getFavoriteWeatherDao(),
                    WeatherDatabase.getInstance(requireContext()).getAlertDao()
                )
            )
        )

        settingsViewModel =
            ViewModelProvider(
                requireActivity(),
                settingsViewModelFactory
            ).get(SettingsViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if((activeNetworkInfo != null && activeNetworkInfo.isConnected)){
            connected()
            setUpSelectedRadioButtons()

            binding.temperature.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbCelsius -> {
                        settingsViewModel.setSettingConfiguration(
                            Constants.TEMPERATURE_UNIT,
                            Constants.METRIC
                        )
                    }

                    R.id.rbFahren -> {
                        settingsViewModel.setSettingConfiguration(
                            Constants.TEMPERATURE_UNIT,
                            Constants.IMPERIAL
                        )
                    }

                    R.id.rbKelvin -> {
                        settingsViewModel.setSettingConfiguration(
                            Constants.TEMPERATURE_UNIT,
                            Constants.STANDARD
                        )
                    }
                }
            }

            binding.wind.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbMeterPerSec -> {
                        settingsViewModel.setSettingConfiguration(
                            Constants.WIND_UNIT,
                            Constants.METER_PER_SECOND
                        )
                    }

                    R.id.rbMilePerHour -> {
                        settingsViewModel.setSettingConfiguration(
                            Constants.WIND_UNIT,
                            Constants.MILE_PER_HOUR
                        )
                    }
                }
            }



            binding.langauage.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbArabic -> {
                        settingsViewModel.setLanguage(Constants.ARABIC)
                    }

                    R.id.rbEnglish -> {
                        settingsViewModel.setLanguage(Constants.ENGLISH)
                    }
                }
            }

            binding.location.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbGps -> {
                        settingsViewModel.setSettingConfiguration(Constants.LOCATION, Constants.GPS)

                        //New
                        settingsViewModel.setSettingConfiguration(Constants.SESSION, false)
                        settingsViewModel.deleteAll()
                    }

                    R.id.rbMap -> {
                    }

                    else -> settingsViewModel.setSettingConfiguration(
                        Constants.LOCATION,
                        Constants.GPS
                    )
                }
            }

            binding.rbMap.setOnClickListener {
                settingsViewModel.setSettingConfiguration(Constants.LOCATION, Constants.MAP)
                settingsViewModel.setSettingConfiguration(Constants.SESSION, false)
                settingsViewModel.deleteAll()
                Navigation.findNavController(view)
                    .navigate(SettingDirections.actionSettingToGoogleMapFragment(Source.SETTINGS))
            }

        }else {
            noConnection()

        }
    }

    override fun onStart() {
        super.onStart()
        checkInternetConnection()
    }

    private fun setUpSelectedRadioButtons() {
        when (settingsViewModel.getSettingConfiguration(Constants.TEMPERATURE_UNIT,"")) {
            Constants.METRIC -> binding.temperature.check(R.id.rbCelsius)
            Constants.IMPERIAL -> binding.temperature.check(R.id.rbFahren)
            Constants.STANDARD -> binding.temperature.check(R.id.rbKelvin)
            else -> binding.temperature.check(R.id.rbKelvin)
        }

        when (settingsViewModel.getSettingConfiguration(Constants.WIND_UNIT,"")) {
            Constants.METER_PER_SECOND -> binding.wind.check(R.id.rbMeterPerSec)
            Constants.MILE_PER_HOUR -> binding.wind.check(R.id.rbMilePerHour)
            else -> binding.wind.check(R.id.rbMilePerHour)
        }
        when (settingsViewModel.getSettingConfiguration(Constants.LANGUAGE,"")) {
            Constants.ARABIC -> binding.langauage.check(R.id.rbArabic)
            Constants.ENGLISH -> binding.langauage.check(R.id.rbEnglish)
            else -> binding.langauage.check(R.id.rbEnglish)
        }
        when (settingsViewModel.getSettingConfiguration(Constants.LOCATION,"")) {
            Constants.GPS -> binding.location.check(R.id.rbGps)
            Constants.MAP -> binding.location.check(R.id.rbMap)
            else -> binding.location.check(R.id.rbGps)
        }
    }

    private fun checkInternetConnection(){
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val mHandler = Handler(Looper.getMainLooper())
        val networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                mHandler.post {
                    connected()
                    setUpSelectedRadioButtons()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                mHandler.post {
                    noConnection()
                }
            }
        }

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun noConnection(){
        binding.networkLayout.visibility = View.VISIBLE
        binding.settingLayout.visibility = View.GONE
        binding.viewLayout.visibility = View.GONE
    }
    private fun connected(){
        binding.networkLayout.visibility = View.GONE
        binding.settingLayout.visibility = View.VISIBLE
        binding.viewLayout.visibility = View.VISIBLE
    }
}