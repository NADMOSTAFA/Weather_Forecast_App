package com.nada.weatherapp.weather_info.view

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nada.weatherapp.R
import com.nada.weatherapp.databinding.ItemWeatherBinding
import com.nada.weatherapp.data.model.WeatherInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TodayForeCastListAdapter(private val listener: (WeatherInfo) -> Unit) :
    ListAdapter<WeatherInfo, TodayForeCastListAdapter.WeatherInfoViewHolder>(WeatherInfoDiffUtil()) {
    private lateinit var binding: ItemWeatherBinding

    inner class WeatherInfoViewHolder(var binding: ItemWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener(getItem(adapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherInfoViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate(inflater, R.layout.item_weather, parent, false)
        return WeatherInfoViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: WeatherInfoViewHolder, position: Int) {
        val current = getItem(position)
        holder.binding.weatherInfo = current
        holder.binding.time = getTime(current)
        var data = current.weather.get(0).icon
        val resId: Int = holder.itemView.resources.getIdentifier("icon_$data" , "drawable", holder.itemView.context.packageName)
        binding.image.setImageResource(resId)

//        holder.binding.button.setOnClickListener {
//            listener(current)
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTime(current: WeatherInfo): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Parse datetime string into a LocalDateTime object
        val dtObject = LocalDateTime.parse(current.dt_txt, formatter)

        // Format the time component as "hh:mm a" (12-hour format with AM/PM indicator)
        val formattedTime = dtObject.format(DateTimeFormatter.ofPattern("hh:mm a"))
        return formattedTime
    }
}

class WeatherInfoDiffUtil : DiffUtil.ItemCallback<WeatherInfo>() {
    override fun areItemsTheSame(oldItem: WeatherInfo, newItem: WeatherInfo): Boolean {
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: WeatherInfo, newItem: WeatherInfo): Boolean {
        return oldItem == newItem
    }
}