package com.nada.weatherapp.weather_info.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
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
import com.google.android.material.snackbar.Snackbar
import com.nada.weatherapp.MainActivity
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.Utils.State
import com.nada.weatherapp.data.local.WeatherLocalDataSourceImpl
import com.nada.weatherapp.data.local.WeatherDatabase
import com.nada.weatherapp.databinding.FragmentHomeBinding
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModel
import com.nada.weatherapp.weather_info.viewmodel.WeatherInfoViewModelFactory
import com.nada.weatherapp.data.model.WeatherInfo
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import java.util.Locale

private const val TAG = "here"
const val REQUEST_LOCATION_CODE: Int = 2002

class Home : Fragment(), OnWeatherInfoClickListener {
    private lateinit var weatherInfoViewModel: WeatherInfoViewModel
    private lateinit var weatherInfoViewModelFactory: WeatherInfoViewModelFactory

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: TodayForeCastListAdapter
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var longitude: Double? = null
    var latitude: Double? = null
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
        adapter = TodayForeCastListAdapter({
            this.onClick(it)
        }, requireContext())
        binding.adapter = adapter
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected)) {
            checkForLocationAndGetTheData()
        } else {
            checkInternetConnection()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideActionBarAndDrawer()
        binding.btnOpenDrawer.setOnClickListener {
            (activity as? MainActivity)?.openDrawerLayout()

        }
        if(weatherInfoViewModel.getLanguage() == Constants.ARABIC){
            binding.tempUnit.visibility = View.GONE
        }
        binding.btnFiveDaysForecast.setOnClickListener {
            Navigation.findNavController(view).navigate(
                HomeDirections.actionHome2ToFiveDaysForecast2(
                    weatherInfoViewModel.getLongitude().toString(),
                    weatherInfoViewModel.getLatitude().toString(),
                    true
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
                                getTempUnit()
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
                                        binding.weatherImage.layoutParams.height = 400
                                    } else {
                                        binding.weatherImage.layoutParams.width = 150.dpToPx()
                                        binding.weatherImage.layoutParams.height = 150.dpToPx()
                                    }
//
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
            binding.weatherImage.layoutParams.height = 400
        } else {
            binding.weatherImage.layoutParams.width = 150.dpToPx()
            binding.weatherImage.layoutParams.height = 150.dpToPx()
        }
    }

    // Location
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (grantResults.size > 1 && grantResults.get(0) == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    getFreshLocation()
                } else {
                    enableLocationServices()
                }
            } else {
                binding.locationLayout.visibility = View.VISIBLE
                binding.loadingLayout.visibility = View.GONE
                binding.btnAllow.setOnClickListener {
                    checkForLocationAndGetTheData()
                }
            }
        }
    }

    private fun enableLocationServices() {
        Toast.makeText(requireContext(), "Turn On Location", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    //Location
    private fun checkPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    @SuppressLint("MissingPermission")
    private fun getFreshLocation() {
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
                    weatherInfoViewModel.setLatitude(latitude.toString())
                    weatherInfoViewModel.setLongitude(longitude.toString())
                    Log.i(
                        TAG,
                        "getFreshLocation: location ${location} long $longitude lati $latitude "
                    )
                    weatherInfoViewModel.getWeatherInfoOverNetwork(
                        latitude = latitude!!,
                        longitude = longitude!!,
                        lang = weatherInfoViewModel.getLanguage()!!,
                    )
                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            },
            Looper.myLooper()
        )
    }

    private fun getCountryNameFromCode(context: Context, countryCode: String): String? {
        val locale = Locale("", countryCode)
        return locale.displayCountry
    }

    private fun checkForLocationAndGetTheData() {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if ((activeNetworkInfo != null && activeNetworkInfo.isConnected) && !weatherInfoViewModel.isDataCached()!!) {
            if (weatherInfoViewModel.getLocation() == Constants.GPS) {
                if (checkPermissions(requireContext())) {
                    if (isLocationEnabled()) {
                        getFreshLocation()
                    } else {
                        enableLocationServices()
                    }
                } else {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        REQUEST_LOCATION_CODE
                    )
                }
            } else {
                weatherInfoViewModel.getWeatherInfoOverNetwork(
                    latitude = weatherInfoViewModel.getLatitude()!!.toDouble(),
                    longitude = weatherInfoViewModel.getLongitude()!!.toDouble(),
                    lang = weatherInfoViewModel.getLanguage()!!,
                )
                weatherInfoViewModel.setDataCached(true)
            }
        } else {
            Log.i(TAG, "from Db: ")
            weatherInfoViewModel.getWeatherFromDB(weatherInfoViewModel.getLanguage()!!)
        }

    }

    private fun checkInternetConnection() {
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
                    binding.networkLayout.visibility = View.GONE
                    binding.loadingLayout.visibility = View.VISIBLE
                    checkForLocationAndGetTheData()
                }
            }
        }

        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun getTempUnit() {
        when (weatherInfoViewModel.getTemperatureUnit()) {
            Constants.IMPERIAL -> {
                binding.unit = getString(R.string.F)
            }

            Constants.STANDARD -> {
                binding.unit = getString(R.string.K)
            }

            Constants.METRIC -> {
                binding.unit = getString(R.string.C)
            }
        }
    }

    fun Int.dpToPx(): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}