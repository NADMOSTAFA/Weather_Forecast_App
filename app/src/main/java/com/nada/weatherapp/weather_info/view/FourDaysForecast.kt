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
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.ApiState
import com.nada.weatherapp.databinding.FragmentFourDaysForecastBinding
import com.nada.weatherapp.data.model.WeatherInfo
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModel
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FourDaysForecast : Fragment() ,OnWeatherInfoClickListener{
    private lateinit var weatherInfoViewModel: WeatherInfoViewModel
    private lateinit var weatherInfoViewModelFactory: WeatherInfoViewModelFactory
    private lateinit var binding: FragmentFourDaysForecastBinding

    private lateinit var weatherInfoList: MutableList<WeatherInfo>
    private lateinit var adapter: NextDaysForecastListAdapter
    var longitude: Double? = null
    var latitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherInfoViewModelFactory = WeatherInfoViewModelFactory(
            WeatherInfoRepositoryImpl.getInstance(
                ForecastRemoteDataSourceImpl.getInstance(),
                SharedPreferencesDataSourceImpl.getInstance(requireContext())
            )
        )

        weatherInfoViewModel = ViewModelProvider(
            this,
            weatherInfoViewModelFactory
        ).get(WeatherInfoViewModel::class.java)

        val args = FourDaysForecastArgs.fromBundle(arguments ?: Bundle())
        longitude = args.longitude.toDouble()
        latitude = args.latitude.toDouble()

        weatherInfoViewModel.getUserLocation(latitude!!,longitude!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        inflater.inflate(R.layout.fragment_five_days_forecast, container, false)
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
        super.onViewCreated(view, savedInstanceState)
        var navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding.btnBack.setOnClickListener {
            navController.navigate(
                R.id.home2,
                null,
                NavOptions.Builder().setPopUpTo(R.id.home2, false).build()
            )
        }
        lifecycleScope.launch {
            weatherInfoViewModel.weatherInfo.collectLatest {
                when (it) {
                    is ApiState.Success<*> -> {
                        when (it.type) {
                            WeatherResponse::class.java -> {
                                val weatherResponse: WeatherResponse = it.data as WeatherResponse
                                weatherInfoList = mutableListOf()
                                weatherInfoList = weatherInfoViewModel.getFiveDaysForecast(
                                    weatherInfoList,
                                    weatherResponse.list
                                )
                                adapter.submitList(weatherInfoList)
                                val weatherInfo = weatherInfoList.get(0)
                                var data = weatherInfo.weather.get(0).icon
                                val resId: Int =resources.getIdentifier("icon_$data" , "drawable",requireContext().packageName)
                                binding.weatherImage.setImageResource(resId)
                                if(weatherInfo.weather.get(0).icon == "01d"){
                                    binding.weatherImage.layoutParams.width = 210
                                }
                                binding.weatherInfo = weatherInfo
                                binding.day = weatherInfoViewModel.getDayOfWeekFromDate(weatherInfo.dt_txt,requireContext())
                                Log.i("here", "onViewCreated: ${weatherInfo.dt_txt}")
                            }
                            // Handle other success types if needed
                        }
                    }

                    is ApiState.Failure -> {
                        val throwable: Throwable = it.msg
                        // Handle failure state here
                    }

                    is ApiState.Loading -> {
                        // Handle loading state here
                    }
                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(weatherInfo: WeatherInfo) {
        var data = weatherInfo.weather.get(0).icon
        val resId: Int =resources.getIdentifier("icon_$data" , "drawable",requireContext().packageName)
        binding.weatherImage.setImageResource(resId)
        if(weatherInfo.weather.get(0).icon == "01d"){
            binding.weatherImage.layoutParams.width = 210
        }
        binding.weatherInfo = weatherInfo
        binding.day = weatherInfoViewModel.getDayOfWeekFromDate(weatherInfo.dt_txt,requireContext())

    }
}
