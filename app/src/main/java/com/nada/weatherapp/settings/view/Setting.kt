package com.nada.weatherapp.settings.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.nada.weatherapp.R
import com.nada.weatherapp.databinding.FragmentSettingBinding
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.settings.viewModel.SettingsViewModel
import com.nada.weatherapp.settings.viewModel.SettingsViewModelFactory
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl

const val METRIC = "metric"
const val IMPERIAL = "imperial"
const val STANDARD = "standard"
const val METER_PER_SECOND = "meterPerSec"
const val MILE_PER_HOUR = "milePerHour"
const val ENGLISH = "en"
const val ARABIC = "ar"
const val MAP = "map"
const val GPS = "gps"


class Setting : Fragment() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory

    private lateinit var binding: FragmentSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsViewModelFactory = SettingsViewModelFactory(
            WeatherInfoRepositoryImpl.getInstance(
                ForecastRemoteDataSourceImpl.getInstance(),
                SharedPreferencesDataSourceImpl.getInstance(requireContext())
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

        setUpSelectedRadioButtons()

        binding.temperature.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbCelsius -> {
                    settingsViewModel.setTemperatureUnit(METRIC)
                }

                R.id.rbFahren -> {
                    settingsViewModel.setTemperatureUnit(IMPERIAL)
                }

                R.id.rbKelvin -> {
                    settingsViewModel.setTemperatureUnit(STANDARD)
                }
            }
        }

        binding.wind.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbMeterPerSec -> {
                    settingsViewModel.setWindUnit(METER_PER_SECOND)
                }

                R.id.rbMilePerHour -> {
                    settingsViewModel.setWindUnit(MILE_PER_HOUR)
                }
            }
        }

        binding.langauage.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbArabic -> {
                    settingsViewModel.setLanguage(ARABIC)
                }

                R.id.rbEnglish -> {
                    settingsViewModel.setLanguage(ENGLISH)
                }

            }
        }

        binding.location.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbGps -> {
                    settingsViewModel.setLocation(GPS)
                }

                R.id.rbMap -> {
                    settingsViewModel.setLocation(MAP)
                    Navigation.findNavController(view)
                        .navigate(SettingDirections.actionSettingToGoogleMapFragment())
                }

                else -> settingsViewModel.setLocation(GPS)
            }
        }
    }

    private fun setUpSelectedRadioButtons() {
        when (settingsViewModel.getTemperatureUnit()) {
            METRIC -> binding.temperature.check(R.id.rbCelsius)
            IMPERIAL -> binding.temperature.check(R.id.rbFahren)
            STANDARD -> binding.temperature.check(R.id.rbKelvin)
            else -> binding.temperature.check(R.id.rbKelvin)
        }

        when (settingsViewModel.getWindUnit()) {
            METER_PER_SECOND -> binding.wind.check(R.id.rbMeterPerSec)
            MILE_PER_HOUR -> binding.wind.check(R.id.rbMilePerHour)
            else -> binding.wind.check(R.id.rbMilePerHour)
        }
        when (settingsViewModel.getLanguage()) {
            ARABIC -> binding.langauage.check(R.id.rbArabic)
            ENGLISH -> binding.langauage.check(R.id.rbEnglish)
            else -> binding.langauage.check(R.id.rbEnglish)
        }
        when (settingsViewModel.getLocation()) {
            GPS -> binding.location.check(R.id.rbGps)
            MAP -> binding.location.check(R.id.rbMap)
            else -> binding.location.check(R.id.rbGps)
        }
    }
}