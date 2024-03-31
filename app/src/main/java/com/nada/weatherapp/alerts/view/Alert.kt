package com.nada.weatherapp.alerts.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.nada.weatherapp.MainActivity
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.Utils.Source
import com.nada.weatherapp.alerts.viewmodel.AlertViewModel
import com.nada.weatherapp.alerts.viewmodel.AlertViewModelFactory
import com.nada.weatherapp.data.local.WeatherLocalDataSourceImpl
import com.nada.weatherapp.data.local.WeatherDatabase
import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.data.remote.ForecastRemoteDataSourceImpl
import com.nada.weatherapp.data.repo.WeatherInfoRepositoryImpl
import com.nada.weatherapp.data.shared_pref.SharedPreferencesDataSourceImpl
import com.nada.weatherapp.databinding.FragmentAlertBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class Alert : Fragment(), OnAlertRemoveListener {
    private lateinit var alertViewModel: AlertViewModel
    private lateinit var alertViewModelFactory: AlertViewModelFactory

    private lateinit var binding: FragmentAlertBinding
    private lateinit var adapter: AlertsListAdapter

    private lateinit var dialog: Dialog

    var longitude: Double? = null
    var latitude: Double? = null

    var time: String? = null
    var date: String? = null
    var rawDate: String? = null
    var rawTime: String? = null

    //Dialog
    private lateinit var tvTime: TextView
    private lateinit var tvDate: TextView
    private lateinit var cardTime: CardView
    private lateinit var cardDate: CardView
    private lateinit var btnCancelAlert: TextView
    private lateinit var btnSaveAlert: TextView
    private lateinit var alertTypeRadioGroup: RadioGroup
    private lateinit var confirmDialog: Dialog
    private lateinit var btnDelete: TextView
    private lateinit var btnCancel: TextView


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        alertViewModelFactory = AlertViewModelFactory(
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

        alertViewModel =
            ViewModelProvider(this, alertViewModelFactory).get(AlertViewModel::class.java)

        val args = AlertArgs.fromBundle(arguments ?: Bundle())
        Log.i("alert", "onCreate: enter ${args.longitude.toDouble()}")

        if (args.longitude != "0" && args.latitude != "0") {
            Log.i("alert", "onCreate: enter ")
            longitude = args.longitude.toDouble()
            latitude = args.latitude.toDouble()

            initDialog()

            cardDate.setOnClickListener {
                showDatePickerDialog { year, month, dayOfMonth ->
                    date = formatDate(dayOfMonth, month)
                    rawDate = "$year-${month + 1}-$dayOfMonth"
                    tvDate.text = "$date"
                }
            }

            cardTime.setOnClickListener {
                showTimePickerDialog { hour, minute, amPm ->
                    time = "${String.format("%02d", hour)}:${String.format("%02d", minute)} $amPm"
                    rawTime = "$hour:$minute ampm"
                    dialog.show()
                    Log.i("time", "onCreate: $time")
                    tvTime.text = "$time"
                }
            }


            showDatePickerDialog { year, month, dayOfMonth ->
                date = formatDate(dayOfMonth, month)
                rawDate = "$year-${month + 1}-$dayOfMonth"
                tvDate.text = "$date"
                showTimePickerDialog { hour, minute, amPm ->
                    time = "${String.format("%02d", hour)}:${String.format("%02d", minute)} $amPm"
                    rawTime = "$hour:$minute ampm"
                    dialog.show()
                    Log.i("time", "onCreate: $time")
                    tvTime.text = "$time"
                }

            }

            var alertType = Constants.NOTIFICATION
            alertTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->

                when (checkedId) {
                    R.id.rbNotification -> {
                        val notificationManager =
                            requireActivity().getSystemService(NotificationManager::class.java) as NotificationManager
                        if (!notificationManager.areNotificationsEnabled()) {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                100
                            )
                        }
                    }

                    else -> {
                        if (!Settings.canDrawOverlays(requireContext())) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + requireContext().packageName)
                            )
                            startActivityForResult(intent, 120)
                        }
                        alertType = Constants.ALARM
                    }
                }
            }

            btnSaveAlert.setOnClickListener {
                Log.i("alert", "onCreate: enter1")
                alertViewModel.setAlert(
                    requireContext(),
                    rawDate!!,
                    time!!,
                    latitude!!,
                    longitude!!,
                    alertType
                )
                dialog.dismiss()
            }

            btnCancelAlert.setOnClickListener {
                dialog.dismiss()
            }

        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initDialog() {
        dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.custom_data_time_dialog)
        dialog.window!!
            .setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        dialog.window!!.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.dialog_bg))
        tvTime = dialog.findViewById<TextView>(R.id.tvTime)
        tvDate = dialog.findViewById<TextView>(R.id.tvDate)
        cardTime = dialog.findViewById<CardView>(R.id.cardTime)
        cardDate = dialog.findViewById<CardView>(R.id.cardArabic)
        btnCancelAlert = dialog.findViewById<TextView>(R.id.btnCancelAlert)
        btnSaveAlert = dialog.findViewById<TextView>(R.id.btnSaveAlert)
        alertTypeRadioGroup = dialog.findViewById<RadioGroup>(R.id.rgAlert)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alert, container, false)
        (activity as? MainActivity)?.showActionBarAndDrawer()
        adapter = AlertsListAdapter(this)
        binding.adapter = adapter
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swiper.setOnRefreshListener {
            alertViewModel.getWeatherAlerts()
            binding.swiper.isRefreshing = false
        }
        setUpDialog()
        binding.btnAddAlert.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(AlertDirections.actionAlertToGoogleMapFragment(Source.ALERTS))
        }
        Log.i("alert", "onViewCreated: lon $longitude , lat $latitude")

        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if ((activeNetworkInfo != null && activeNetworkInfo.isConnected)) {
            connected()
            lifecycleScope.launch(Dispatchers.Main) {
                alertViewModel.weatherAlertInfo.collectLatest {
                    if(it.isNotEmpty()){
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


    override fun onClickRemove(weatherAlert: WeatherAlert) {
        confirmDialog.show()
        btnDelete.setOnClickListener {
            alertViewModel.cancelWeatherAlert(weatherAlert)
            confirmDialog.dismiss()
        }
        btnCancel.setOnClickListener {
            confirmDialog.dismiss()
        }
    }


    private fun showTimePickerDialog(onTimeSelected: (hour: Int, minute: Int, amPm: String) -> Unit) {
        val isSystem24Hours = is24HourFormat(requireContext())
        val clockFormat = when (isSystem24Hours) {
            true -> TimeFormat.CLOCK_24H
            else -> TimeFormat.CLOCK_12H
        }

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Alarm Time")
            .build()

        picker.show(childFragmentManager, "TAG")
        picker.addOnPositiveButtonClickListener {
            val amPm = if (picker.hour >= 12) "PM" else "AM"
            val hour12Format =
                if (picker.hour > 12) picker.hour - 12 else if (picker.hour == 0) 12 else picker.hour
            val minuteFormatted = String.format(
                Locale.getDefault(),
                "%02d",
                picker.minute
            ) // Format minute with leading zero
            onTimeSelected(hour12Format, picker.minute, amPm)
        }
    }

    private fun showDatePickerDialog(onDateSelected: (year: Int, month: Int, dayOfMonth: Int) -> Unit) {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)
        val minDate = currentDate.timeInMillis

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                onDateSelected(year, monthOfYear, dayOfMonth)
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.datePicker.maxDate =
            Long.MAX_VALUE // Set maximum date to a very large value

        datePickerDialog.show()
    }

    private fun formatDate(dayOfMonth: Int, monthOfYear: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.MONTH, monthOfYear)
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(calendar.time)
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
                        alertViewModel.weatherAlertInfo.collectLatest {
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
        binding.alertLayout.visibility = View.GONE
        binding.viewLayout.visibility = View.GONE
        binding.btnAddAlert.visibility = View.GONE
    }

    private fun connected() {
        binding.networkLayout.visibility = View.GONE
        binding.alertLayout.visibility = View.VISIBLE
        binding.viewLayout.visibility = View.VISIBLE
        binding.btnAddAlert.visibility = View.VISIBLE
    }

    private fun setUpDialog() {
        confirmDialog = Dialog(requireContext())
        confirmDialog.setContentView(R.layout.confirmation_dialog)
        confirmDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        confirmDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dialog_bg
            )
        )
        confirmDialog.setCancelable(false)
        confirmDialog.setCanceledOnTouchOutside(true)
        btnDelete = confirmDialog.findViewById(R.id.btnOk)
        btnCancel = confirmDialog.findViewById(R.id.cancel)
    }
}