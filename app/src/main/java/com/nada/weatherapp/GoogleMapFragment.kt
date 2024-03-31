package com.nada.weatherapp

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.Utils.Source
import com.nada.weatherapp.data.local.WeatherLocalDataSourceImpl
import com.nada.weatherapp.data.local.WeatherDatabase
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.databinding.FragmentMapBinding
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.settings.viewModel.SettingsViewModel
import com.nada.weatherapp.settings.viewModel.SettingsViewModelFactory
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import com.nada.weatherapp.favorites.viewmodel.FavoriteViewModel
import com.nada.weatherapp.favorites.viewmodel.FavoriteViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GoogleMapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory

    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var favoriteViewModelFactory: FavoriteViewModelFactory

    private lateinit var mGoogleGoogleMap: GoogleMap
    lateinit var geocoder: Geocoder
    var longitude: Double? = null
    var latitude: Double? = null

    var city: String? = null
    var country: String? = null

    private var marker: Marker? = null

    private lateinit var binding: FragmentMapBinding
    private lateinit var source: Source
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = GoogleMapFragmentArgs.fromBundle(arguments ?: Bundle())
        source = args.source

        geocoder = Geocoder(requireActivity())

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

        favoriteViewModelFactory = FavoriteViewModelFactory(
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

        favoriteViewModel =
            ViewModelProvider(this, favoriteViewModelFactory).get(FavoriteViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMapBinding.inflate(inflater, container, false)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.btnSave.setOnClickListener {
            Log.i("here", "Map: lat $latitude long $longitude")
            when (source) {
                Source.SETTINGS -> {
                    settingsViewModel.setSettingConfiguration(Constants.LATITUDE,latitude.toString())
                    settingsViewModel.setSettingConfiguration(Constants.LONGITUDE,longitude.toString())
                    Navigation.findNavController(requireView())
                        .navigate(GoogleMapFragmentDirections.actionGoogleMapFragmentToHome2())
                }

                Source.FAVORITES -> {
                    val favoriteWeather =
                        if (city == null) {
                            city = ""
                            if(country == null){
                                country =  requireContext().getString(R.string.unknow)
                            }
                            FavoriteWeather(latitude!!, longitude!!, country!!, city!!)
                        } else {
                            FavoriteWeather(latitude!!, longitude!!, country!!, city!!)
                        }

                    favoriteViewModel.insertWeather(favoriteWeather)

//                    Navigation.findNavController(requireView()).navigate(
//                        R.id.googleMapFragment,
//                        null,
//                        NavOptions.Builder().setPopUpTo(R.id.favorite, false).build())
                    Navigation.findNavController(requireView())
                        .navigate(GoogleMapFragmentDirections.actionGoogleMapFragmentToFavorite())
                }

                Source.ALERTS -> {

                    val action = GoogleMapFragmentDirections.actionGoogleMapFragmentToAlert()
                    action.setLongitude(longitude.toString())
                    action.setLatitude(latitude.toString())
                    Navigation.findNavController(requireView())
                        .navigate(action)
                }
            }

        }
        return binding.root
    }

    override fun onMapReady(gGoogleMap: GoogleMap) {
        mGoogleGoogleMap = gGoogleMap

        mGoogleGoogleMap.setOnMapClickListener { latLng ->
            binding.btnSave.visibility = View.VISIBLE
            latitude = latLng.latitude
            longitude = latLng.longitude
            lifecycleScope.launch(Dispatchers.IO) {
                getCountryName(latLng.latitude, latLng.longitude)
                Log.d(
                    "nada",
                    "Latitude: $latitude, Longitude: $longitude country $country city $city"
                )
            }


            // Remove previous marker
            marker?.remove()

            // Add marker at clicked location
            val markerOptions = MarkerOptions().position(latLng)
            marker = mGoogleGoogleMap.addMarker(markerOptions)
        }
    }

    fun getCountryName(latitude: Double, longitude: Double) {
        val maxResults = 1

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, maxResults)
            if (addresses!!.isNotEmpty()) {
                city = addresses[0].locality
                country = addresses[0].countryName
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}