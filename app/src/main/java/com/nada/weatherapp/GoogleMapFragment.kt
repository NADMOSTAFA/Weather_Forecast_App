package com.nada.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.nada.weatherapp.databinding.FragmentMapBinding
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.settings.viewModel.SettingsViewModel
import com.nada.weatherapp.settings.viewModel.SettingsViewModelFactory
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl

class GoogleMapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory
    private lateinit var mGoogleGoogleMap: GoogleMap
    var longitude: Double? = null
    var latitude: Double? = null
    private var marker: Marker? = null

    private lateinit var binding: FragmentMapBinding
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
        binding = FragmentMapBinding.inflate(inflater, container, false)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.btnSave.setOnClickListener {
            Log.i("here", "Map: lat $latitude long $longitude")
            settingsViewModel.setLatitude(latitude.toString())
            settingsViewModel.setLongitude(longitude.toString())
            Navigation.findNavController(requireView())
                .navigate(GoogleMapFragmentDirections.actionGoogleMapFragmentToHome2())
        }
        return binding.root
    }

    override fun onMapReady(gGoogleMap: GoogleMap) {
        mGoogleGoogleMap = gGoogleMap

        mGoogleGoogleMap.setOnMapClickListener { latLng ->
            binding.btnSave.visibility = View.VISIBLE
             latitude = latLng.latitude
             longitude = latLng.longitude
            // Handle latitude and longitude
            Log.d("MapClick", "Latitude: $latitude, Longitude: $longitude")

            // Remove previous marker
            marker?.remove()

            // Add marker at clicked location
            val markerOptions = MarkerOptions().position(latLng)
            marker = mGoogleGoogleMap.addMarker(markerOptions)
        }
    }
}