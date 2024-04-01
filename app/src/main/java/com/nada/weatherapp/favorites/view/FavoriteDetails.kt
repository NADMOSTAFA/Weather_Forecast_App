package com.nada.weatherapp.favorites.view

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.nada.weatherapp.MainActivity
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.Utils.State
import com.nada.weatherapp.data.local.WeatherLocalDataSourceImpl
import com.nada.weatherapp.data.local.WeatherDatabase
import com.nada.weatherapp.data.model.WeatherInfo
import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import com.nada.weatherapp.databinding.FragmentHomeBinding
import com.nada.weatherapp.weather_info.view.OnWeatherInfoClickListener
import com.nada.weatherapp.weather_info.view.TodayForeCastListAdapter
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModel
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class FavoriteDetails : Fragment(), OnWeatherInfoClickListener {
    private lateinit var weatherInfoViewModel: WeatherInfoViewModel
    private lateinit var weatherInfoViewModelFactory: WeatherInfoViewModelFactory

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: TodayForeCastListAdapter

    var longitude: Double? = null
    var latitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = FavoriteDetailsArgs.fromBundle(arguments ?: Bundle())
        longitude = args.longitude.toDouble()
        latitude = args.latitude.toDouble()

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(container!!.context),
            R.layout.fragment_home,
            container,
            false
        )


        adapter = TodayForeCastListAdapter({
            this.onClick(it)
        }, requireContext())
        binding.adapter = adapter

        weatherInfoViewModel.getWeatherInfoOverNetwork(
            latitude = latitude!!,
            longitude = longitude!!,
            lang = weatherInfoViewModel.getLanguage()!!,
            source = Constants.TODAY_FORECAST
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideActionBarAndDrawer()
        binding.btnOpenDrawer.visibility = View.GONE
        binding.btnFiveDaysForecast.setOnClickListener {
            Navigation.findNavController(view).navigate(
                FavoriteDetailsDirections.actionFavoriteDetailsToFiveDaysForecast2(
                    longitude.toString(),
                    latitude.toString(),
                    false
                )
            )
        }
        lifecycleScope.launch(Dispatchers.IO) {
            weatherInfoViewModel.weatherInfo.collectLatest {
                when (it) {
                    is State.Success<*> -> {
                        when (it.type) {
                            WeatherResponse::class.java -> {
                                val weatherResponse: WeatherResponse = it.data as WeatherResponse
                                var weatherInfo =
                                    weatherResponse.list.get(0)
                                weatherInfo!!.date = weatherInfoViewModel.getCurrentDate()
                                withContext(Dispatchers.Main) {
                                    var data = weatherInfo.weather.get(0).icon
                                    val resId: Int = resources.getIdentifier(
                                        "icon_$data",
                                        "drawable",
                                        requireContext().packageName
                                    )
                                    binding.weatherInfo = weatherInfo
                                    weatherResponse.city.country = getCountryNameFromCode(
                                        requireContext(),
                                        weatherResponse.city.country
                                    )!!
                                    binding.city = weatherResponse.city
                                    binding.weatherImage.setImageResource(resId)
                                    adapter.submitList(weatherResponse.list)
                                    if (weatherInfo.weather.get(0).icon == "01d") {
                                        binding.weatherImage.layoutParams.width = 700
                                        binding.weatherImage.layoutParams.height = 500
                                    } else {
                                        binding.weatherImage.layoutParams.width = 150.dpToPx()
                                        binding.weatherImage.layoutParams.height = 150.dpToPx()
                                    }
                                    binding.loadingLayout.visibility = View.GONE
                                    binding.networkLayout.visibility = View.GONE
                                    binding.locationLayout.visibility = View.GONE
                                    binding.homeLayout.visibility = View.VISIBLE
                                    binding.viewLayout.visibility = View.VISIBLE
                                }
                            }
                            // Handle other success types if needed
                        }
                    }

                    is State.Failure -> {
                        withContext(Dispatchers.Main) {
                            binding.loadingLayout.visibility = View.GONE
                            binding.networkLayout.visibility = View.VISIBLE
                            binding.homeLayout.visibility = View.GONE
                            binding.viewLayout.visibility = View.GONE
                            binding.locationLayout.visibility = View.GONE
                        }
                        val throwable: Throwable = it.msg
                    }

                    is State.Loading -> {
                        withContext(Dispatchers.Main) {
                            binding.loadingLayout.visibility = View.VISIBLE
                            binding.networkLayout.visibility = View.GONE
                            binding.homeLayout.visibility = View.GONE
                            binding.viewLayout.visibility = View.GONE
                            binding.locationLayout.visibility = View.GONE

                        }
                    }
                }
            }

        }
    }


    override fun onClick(weatherInfo: WeatherInfo) {
        weatherInfo!!.date = weatherInfoViewModel.getCurrentDate()
        binding.weatherInfo = weatherInfo
        var data = weatherInfo.weather.get(0).icon
        val resId: Int =
            resources.getIdentifier("icon_$data", "drawable", requireContext().packageName)
        binding.weatherImage.setImageResource(resId)
        if (weatherInfo.weather.get(0).icon == "01d") {
            binding.weatherImage.layoutParams.width = 700
            binding.weatherImage.layoutParams.height = 500
        } else {
            binding.weatherImage.layoutParams.width = 150.dpToPx()
            binding.weatherImage.layoutParams.height = 150.dpToPx()
        }
    }

    private fun getCountryNameFromCode(context: Context, countryCode: String): String? {
        val locale = Locale("", countryCode)
        return locale.displayCountry
    }

    private fun Int.dpToPx(): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

}