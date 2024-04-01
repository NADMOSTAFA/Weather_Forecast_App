package com.nada.weatherapp.weather_info.view

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.nada.weatherapp.MainActivity
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.Utils.State
import com.nada.weatherapp.data.local.WeatherLocalDataSourceImpl
import com.nada.weatherapp.data.local.WeatherDatabase
import com.nada.weatherapp.databinding.FragmentFourDaysForecastBinding
import com.nada.weatherapp.data.model.WeatherInfo
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModel
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FourDaysForecast : Fragment(), OnWeatherInfoClickListener {
    private lateinit var weatherInfoViewModel: WeatherInfoViewModel
    private lateinit var weatherInfoViewModelFactory: WeatherInfoViewModelFactory
    private lateinit var binding: FragmentFourDaysForecastBinding

    private lateinit var adapter: NextDaysForecastListAdapter
    var longitude: Double? = null
    var latitude: Double? = null
    private var isComingFromHome: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherInfoViewModelFactory = WeatherInfoViewModelFactory(
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

        weatherInfoViewModel = ViewModelProvider(
            this,
            weatherInfoViewModelFactory
        ).get(WeatherInfoViewModel::class.java)

        val args = FourDaysForecastArgs.fromBundle(arguments ?: Bundle())
        longitude = args.longitude.toDouble()
        latitude = args.latitude.toDouble()
        isComingFromHome = args.isComingFromHome

        if (isComingFromHome) {
            weatherInfoViewModel.getWeatherFromDB(weatherInfoViewModel.getLanguage()!!, false)
        } else {
            weatherInfoViewModel.getWeatherInfoOverNetwork(
                latitude = latitude!!,
                longitude = longitude!!,
                lang = weatherInfoViewModel.getLanguage()!!,
                source = Constants.FOUR_DAYS_FORECAST
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(container!!.context),
            R.layout.fragment_four_days_forecast,
            container,
            false
        )
        adapter = NextDaysForecastListAdapter({
            this.onClick(it)
        }, requireContext())
        binding.adapter = adapter
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? MainActivity)?.hideActionBarAndDrawer()
        super.onViewCreated(view, savedInstanceState)
        if (weatherInfoViewModel.getLanguage() == Constants.ARABIC) {
            binding.tempUnit1.visibility = View.GONE
            binding.tempUnit2.visibility = View.GONE
        }
        var navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding.btnBack.setOnClickListener {
            if (isComingFromHome) {
                navController.navigate(
                    R.id.home2,
                    null,
                    NavOptions.Builder().setPopUpTo(R.id.home2, false).build()
                )
            } else {

                navController.navigate(
                    R.id.favorite,
                    null,
                    NavOptions.Builder().setPopUpTo(R.id.favorite, false).build()
                )
            }
        }
        lifecycleScope.launch {
            weatherInfoViewModel.weatherInfo.collectLatest {
                when (it) {
                    is State.Success<*> -> {
                        withContext(Dispatchers.Main) {
//                            (activity as? MainActivity)?.showActionBarAndDrawer()
                            binding.loadingLayout.visibility = View.GONE
                            binding.homeLayout.visibility = View.VISIBLE
                            binding.viewLayout.visibility = View.VISIBLE
                        }
                        when (it.type) {
                            WeatherResponse::class.java -> {
                                val weatherResponse: WeatherResponse = it.data as WeatherResponse
                                adapter.submitList(weatherResponse.list)
                                val weatherInfo = weatherResponse.list.get(0)
                                getTempUnit()
                                var data = weatherInfo.weather.get(0).icon
                                val resId: Int = resources.getIdentifier(
                                    "icon_$data",
                                    "drawable",
                                    requireContext().packageName
                                )
                                binding.weatherImage.setImageResource(resId)
                                if (weatherInfo.weather.get(0).icon == "01d") {
                                    binding.weatherImage.layoutParams.width = 210
                                }
                                binding.weatherInfo = weatherInfo
                                binding.day = weatherInfoViewModel.getDayOfWeekFromDate(
                                    weatherInfo.dt_txt,
                                    requireContext()
                                )
                                Log.i("here", "onViewCreated: ${weatherInfo.dt_txt}")
                            }
                        }
                    }

                    is State.Failure -> {
                        val throwable: Throwable = it.msg
                    }

                    is State.Loading -> {
                        withContext(Dispatchers.Main) {
                            binding.loadingLayout.visibility = View.VISIBLE
                            binding.homeLayout.visibility = View.GONE
                            binding.viewLayout.visibility = View.GONE
                        }
                    }
                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(weatherInfo: WeatherInfo) {
        var data = weatherInfo.weather.get(0).icon
        val resId: Int =
            resources.getIdentifier("icon_$data", "drawable", requireContext().packageName)
        binding.weatherImage.setImageResource(resId)
        if (weatherInfo.weather.get(0).icon == "01d") {
            binding.weatherImage.layoutParams.width = 210
        }
        binding.weatherInfo = weatherInfo
        binding.day =
            weatherInfoViewModel.getDayOfWeekFromDate(weatherInfo.dt_txt, requireContext())

    }

    private fun getTempUnit() {
        when (weatherInfoViewModel.getTemperatureUnit()) {
            Constants.IMPERIAL -> {
                binding.unit = "F"
            }

            Constants.STANDARD -> {
                binding.unit = "K"
            }

            Constants.METRIC -> {
                binding.unit = "C"
            }
        }
    }
}
