package com.nada.weatherapp.alerts.viewmodel

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nada.weatherapp.R
import com.nada.weatherapp.Utils.Constants
import com.nada.weatherapp.data.model.WeatherResponse
import com.nada.weatherapp.data.remote.RetrofitHelper
import com.nada.weatherapp.data.remote.WeatherApiService
import kotlinx.coroutines.runBlocking
import java.util.Locale


class WeatherAlertWorker(var context: Context, params: WorkerParameters) : Worker(context, params) {
    private lateinit var mediaPlayer: MediaPlayer

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun doWork(): Result {
        try {
            //Network Call
            val weatherApiService: WeatherApiService by lazy {
                RetrofitHelper.getInstance().create(WeatherApiService::class.java)
            }

            // longitude - latitude - lang
            val latitude = inputData.getDouble(Constants.LATITUDE, 0.0)
            val longitude = inputData.getDouble(Constants.LONGITUDE, 0.0)
            val language = inputData.getString(Constants.LANGUAGE)
            val alertType = inputData.getString(Constants.TYPE)


            //call method
            val weatherResponse: WeatherResponse = runBlocking {
                weatherApiService.getForecast(
                    latitude = latitude,
                    longitude = longitude,
                    lang = language!!
                )
            }


            if (alertType == Constants.NOTIFICATION) {
                return if (checkForNotificationPermission(applicationContext)) {
                    sendNotification(
                        "Weather Alert Notification",
                        weatherResponse.list.get(0).weather.get(0).description
                    )
                    Result.success()
                } else {
                    Result.failure()
                }
            } else {
                return if (Settings.canDrawOverlays(applicationContext)) {
                    Handler(Looper.getMainLooper()).post {
                        showDialog(weatherResponse)
                    }
                    Result.success()
                } else {
                    Result.failure()
                }


            }

        } catch (e: Exception) {
            return Result.failure(workDataOf(Constants.FAILURE_REASON to e.message))
        }
        return Result.failure()
    }


    private fun sendNotification(title: String, message: String) {
        val channelId = "weather_alerts_channel"
        val notificationId = 123

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notifications_fill)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weather Alerts Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkForNotificationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialog(weatherResponse: WeatherResponse) {
        val inflater = LayoutInflater.from(applicationContext)
        val dialogView = inflater.inflate(R.layout.alert_dialog, null)

        val photo = dialogView.findViewById<ImageView>(R.id.photo)
        val tvMessage = dialogView.findViewById<TextView>(R.id.message)
        val tvCountry = dialogView.findViewById<TextView>(R.id.country)
        val btnOk = dialogView.findViewById<TextView>(R.id.btnOk)

        val resId: Int = applicationContext.resources.getIdentifier(
            "icon_${weatherResponse.list.get(0).weather.get(0).icon}",
            "drawable",
            applicationContext.packageName
        )
        photo.setImageResource(resId)
        tvMessage.text = weatherResponse.list.get(0).weather.get(0).description

        val locale = Locale("", weatherResponse.city.country)
        tvCountry.text = locale.displayCountry



        val alertDialog = AlertDialog.Builder(applicationContext)
            .setView(dialogView)
            .create()

        alertDialog.window?.apply {
            setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            setGravity(Gravity.CENTER)
        }

        btnOk.setOnClickListener {
            stopSound()
            alertDialog.dismiss()
        }



        // Show the dialog
        alertDialog.show()
        playSound()
    }

    private fun playSound() {
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.rain)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun stopSound() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }


}
