package com.nada.weatherapp.favorites.view

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.nada.weatherapp.MainActivity
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.Source
import com.nada.weatherapp.data.local.WeatherLocalDataSourceImpl
import com.nada.weatherapp.data.local.WeatherDatabase
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import com.nada.weatherapp.databinding.FragmentFavoriteBinding
import com.nada.weatherapp.favorites.viewmodel.FavoriteViewModel
import com.nada.weatherapp.favorites.viewmodel.FavoriteViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Favorite : Fragment(), onFavoriteWeatherClickListener, onRemoveClickListener {
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var favoriteViewModelFactory: FavoriteViewModelFactory

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var adapter: FavoriteListAdapter
    private lateinit var dialog: Dialog
    private lateinit var btnDelete: TextView
    private lateinit var btnCancel: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
//        inflater.inflate(R.layout.fragment_favorite, container, false)
        (activity as? MainActivity)?.showActionBarAndDrawer()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite, container, false)
        adapter = FavoriteListAdapter({ this.onClick(it) }, this)
        binding.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDialog()
        binding.btnAddToFav.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(FavoriteDirections.actionFavoriteToGoogleMapFragment(Source.FAVORITES))
        }
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if ((activeNetworkInfo != null && activeNetworkInfo.isConnected)) {
            connected()
            lifecycleScope.launch(Dispatchers.Main) {
                favoriteViewModel.favWeatherInfo.collect {
                    if (it.isNotEmpty()){
                        binding.emptyState.visibility =View.GONE
                        adapter.submitList(it)
                    }else{
                        binding.emptyState.visibility =View.VISIBLE
                    }
                }
            }
        } else {
            noConnection()
        }
    }

    override fun onStart() {
        super.onStart()
        checkInternetConnection()
    }

    override fun onClick(favoriteWeather: FavoriteWeather) {
        Log.i(
            "nada",
            "from navigation:long ${favoriteWeather.longitude} lat${favoriteWeather.latitude} "
        )
        Navigation.findNavController(requireView()).navigate(
            FavoriteDirections.actionFavoriteToFavoriteDetails(
                favoriteWeather.longitude.toString(),
                favoriteWeather.latitude.toString(),
            )
        )
    }

    override fun onClickRemove(favoriteWeather: FavoriteWeather) {
        dialog.show()
        btnDelete.setOnClickListener {
            favoriteViewModel.removeWeather(favoriteWeather)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
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
                    connected()
                    lifecycleScope.launch(Dispatchers.Main) {
                        favoriteViewModel.favWeatherInfo.collect {
                            adapter.submitList(it)
                        }
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                mHandler.post {
                    noConnection()
                }
            }
        }

        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun noConnection() {
        binding.networkLayout.visibility = View.VISIBLE
        binding.favoriteLayout.visibility = View.GONE
        binding.viewLayout.visibility = View.GONE
        binding.btnAddToFav.visibility = View.GONE
    }

    private fun connected() {
        binding.networkLayout.visibility = View.GONE
        binding.favoriteLayout.visibility = View.VISIBLE
        binding.viewLayout.visibility = View.VISIBLE
        binding.btnAddToFav.visibility = View.VISIBLE
    }

    private fun setUpDialog() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.confirmation_dialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dialog_bg
            )
        )
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        btnDelete = dialog.findViewById(R.id.btnOk)
        btnCancel = dialog.findViewById(R.id.cancel)
    }


}