package com.nada.weatherapp.weather_info.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.ApiState
import com.nada.weatherapp.databinding.FragmentHomeBinding
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModel
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModelFactory
import com.nada.weatherapp.data.model.WeatherInfo
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.settings.view.GPS
import com.nada.weatherapp.settings.view.MAP
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException

private const val TAG = "here"
const val REQUEST_LOCATION_CODE: Int = 2002

class Home : Fragment(), OnWeatherInfoClickListener {
    private lateinit var weatherInfoViewModel: WeatherInfoViewModel
    private lateinit var weatherInfoViewModelFactory: WeatherInfoViewModelFactory

    private lateinit var binding: FragmentHomeBinding
    private lateinit var weatherInfoList: MutableList<WeatherInfo>
    private lateinit var adapter: TodayForeCastListAdapter
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
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

    }

    override fun onStart() {
        super.onStart()
        Log.i(
            "here",
            "onCreateView: shared pref : ${weatherInfoViewModel.getLocation() == GPS} value:${weatherInfoViewModel.getLocation()} "
        )
    }

    @SuppressLint("MissingPermissions")
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


        adapter = TodayForeCastListAdapter {
            this.onClick(it)
        }
        binding.adapter = adapter


        if (weatherInfoViewModel.getLocation() == GPS || weatherInfoViewModel.getLocation() == "" || weatherInfoViewModel.getLocation() == null) {
            if (checkPermissions()) {
                Log.i(TAG, "onCreateView: 1")
                if (isLocationEnabled()) {
                    Log.i(TAG, "onCreateView: 2")
                    getFreshLocation()
                } else {
                    Log.i(TAG, "onCreateView: 3")
                    enableLocationServices()
                }
            } else {
                Log.i(TAG, "onCreateView: 4")

                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    REQUEST_LOCATION_CODE
                )
            }
        } else {
            Log.i(TAG, "onCreateView: 5")

            weatherInfoViewModel.getLocationFromMap()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnFiveDaysForecast.setOnClickListener {
            if (weatherInfoViewModel.getLocation() == MAP){
                Navigation.findNavController(view).navigate(
                    HomeDirections.actionHome2ToFiveDaysForecast2(
                        weatherInfoViewModel.getLongitude().toString(),
                        weatherInfoViewModel.getLatitude().toString()
                    )
                )
            }else{
                Navigation.findNavController(view).navigate(
                    HomeDirections.actionHome2ToFiveDaysForecast2(
                        longitude.toString(),
                        latitude.toString()
                    )
                )
            }
        }
        lifecycleScope.launch {
            weatherInfoViewModel.weatherInfo.collectLatest {
                when (it) {
                    is ApiState.Success<*> -> {
                        when (it.type) {
                            WeatherResponse::class.java -> {
                                val weatherResponse: WeatherResponse = it.data as WeatherResponse
                                Log.i(TAG, "onViewCreated: Response  ${weatherResponse.cod}")
                                weatherInfoList = mutableListOf()
                                weatherInfoList = weatherInfoViewModel.getTodayForecast(
                                    weatherInfoList,
                                    weatherResponse.list
                                )
                                adapter.submitList(weatherInfoList)
                                weatherResponse.city.country = weatherInfoViewModel.getCountryName(
                                    requireContext(),
                                    weatherResponse.city.coord.lat,
                                    weatherResponse.city.coord.lon
                                ).toString()
                                var weatherInfo =
                                    weatherInfoViewModel.getCurrentWeather(weatherInfoList)
                                weatherInfo!!.date = weatherInfoViewModel.getCurrentDate()
                                binding.weatherInfo = weatherInfo
                                binding.city = weatherResponse.city
                                var data = weatherInfo.weather.get(0).icon
                                val resId: Int = resources.getIdentifier(
                                    "icon_$data",
                                    "drawable",
                                    requireContext().packageName
                                )
                                binding.weatherImage.setImageResource(resId)
                                if (weatherInfo.weather.get(0).icon == "01d") {
                                    binding.weatherImage.layoutParams.width = 700
                                    binding.weatherImage.layoutParams.height = 500
                                }


                                Log.i("here", "onViewCreated: ${weatherInfo.dt_txt}")
                            }
                            // Handle other success types if needed
                        }
                    }

                    is ApiState.Failure -> {
                        val throwable: Throwable = it.msg
                        Log.i(TAG, "onViewCreated: ${it.msg}")
                        // Handle failure state here
                    }

                    is ApiState.Loading -> {
                        // Handle loading state here
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
        }
    }

    // Location
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionsResult: Entered 0")
        if (requestCode == REQUEST_LOCATION_CODE) {
            Log.i(TAG, "onRequestPermissionsResult: Entered 1")
            if (grantResults.size > 1 && grantResults.get(0) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Entered 2")
                getFreshLocation()
            }
        }
    }

    private fun enableLocationServices() {
        Toast.makeText(requireContext(), "Turn On Location", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    //Location
//    private fun checkPermissions(context: Context): Boolean {
//        Log.i("here", "checkPermissions: Entered ")
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//    }
    private fun checkPermissions():Boolean{
        var status:Boolean = false
        if((ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED )
            || (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED )){

            status = true
        }
        return status
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    } ///////////////////////////


    @SuppressLint("MissingPermission")
    private fun getFreshLocation() {
        Log.i(TAG, "getFreshLocation: Enteeeeeeeeeeeeered ")
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.requestLocationUpdates(
//            request
            LocationRequest.Builder(0).apply {
                setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            }.build(),
//            Callback
            object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    val location = p0.lastLocation
                    longitude = location?.longitude
                    latitude = location?.latitude
                    Log.i(
                        TAG,
                        "getFreshLocation: location ${location} long $longitude lati $latitude "
                    )
                    weatherInfoViewModel.getUserLocation(
                        latitude!!,
                        longitude!!
                    )
                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            },
            Looper.myLooper()
        )
    }/////////////////////////////////

    private fun geocodeLocation(location: Location?) {
        location?.let {
            val geocoder = Geocoder(requireContext())
            try {
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                Log.i(TAG, "geocodeLocation: " + addresses.toString())
                if (addresses!!.isNotEmpty()) {
                    val address =
                        "Country Name: ${addresses[0].countryName} \nCountry Code: ${addresses[0].countryCode}" +
                                "\nPostal Code: ${addresses[0].postalCode}"
                }
            } catch (e: IOException) {
                Log.i(TAG, "geocodeLocation: " + e.message)
                e.printStackTrace()
            }
        }
    }
}