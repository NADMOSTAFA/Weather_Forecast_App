package com.nada.weatherapp.weather_info.view

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nada.weatherapp.R
import com.nada.weatherapp.databinding.ItemFourDaysWeatherBinding
import com.nada.weatherapp.data.model.WeatherInfo
import java.text.SimpleDateFormat
import java.util.Calendar

class NextDaysForecastListAdapter(private val listener: (WeatherInfo) -> Unit,
                                  private var context: Context
) :
    ListAdapter<WeatherInfo, NextDaysForecastListAdapter.WeatherInfoViewHolder>(
        NextDaysWeatherInfoDiffUtil()
    ) {
    private lateinit var binding: ItemFourDaysWeatherBinding

    inner class WeatherInfoViewHolder(var binding: ItemFourDaysWeatherBinding) :
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
        binding = DataBindingUtil.inflate(inflater, R.layout.item_four_days_weather, parent, false)
        return WeatherInfoViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: WeatherInfoViewHolder, position: Int) {
        val current = getItem(position)
        holder.binding.weatherInfo = current
        holder.binding.day = getDayOfWeekFromDate(current.dt_txt, context)
        var data = current.weather.get(0).icon
        val resId: Int = holder.itemView.resources.getIdentifier(
            "icon_$data",
            "drawable",
            holder.itemView.context.packageName
        )
        binding.weatherImage.setImageResource(resId)
    }


    fun getDayOfWeekFromDate(dateString: String, context: Context): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)

        val parsedDate = sdf.parse(dateString)
        calendar.time = parsedDate

        val inputDay = calendar.get(Calendar.DAY_OF_YEAR)

        return when {
            inputDay == today -> context.getString(R.string.today)
            inputDay == today + 1 -> context.getString(R.string.tomorrow)
            else -> SimpleDateFormat("EEEE").format(parsedDate)
        }
    }


}

class NextDaysWeatherInfoDiffUtil : DiffUtil.ItemCallback<WeatherInfo>() {
    override fun areItemsTheSame(oldItem: WeatherInfo, newItem: WeatherInfo): Boolean {
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: WeatherInfo, newItem: WeatherInfo): Boolean {
        return oldItem == newItem
    }
}