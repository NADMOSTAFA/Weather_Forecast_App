package com.nada.weatherapp.weather_info.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nada.weatherapp.R
import com.nada.weatherapp.data.model.WeatherInfo
import com.nada.weatherapp.databinding.ItemWeatherBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TodayForeCastListAdapter(private val listener: (WeatherInfo) -> Unit , private val context: Context) :
    RecyclerView.Adapter<TodayForeCastListAdapter.WeatherInfoViewHolder>() {

    private var selectedPosition = RecyclerView.SCROLLBAR_POSITION_DEFAULT
    private var weatherList: List<WeatherInfo> = emptyList()

    inner class WeatherInfoViewHolder(var binding: ItemWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherInfoViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DataBindingUtil.inflate<ItemWeatherBinding>(
            inflater,
            R.layout.item_weather,
            parent,
            false
        )
        return WeatherInfoViewHolder(binding)
    }

    override fun getItemCount(): Int = weatherList.size

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: WeatherInfoViewHolder, position: Int) {
        val current = weatherList[position]
        holder.binding.weatherInfo = current
        var data = current.weather.get(0).icon
        val resId: Int = holder.itemView.resources.getIdentifier(
            "icon_$data",
            "drawable",
            holder.itemView.context.packageName
        )
        holder.binding.image.setImageResource(resId)

        if (position == selectedPosition) {
            holder.binding.weatherLayout.setBackgroundResource(R.drawable.bg_adapter)
            holder.binding.tvTem.setTextColor(Color.WHITE)
            holder.binding.tvTime.setTextColor(Color.WHITE)
        }
        else {
            holder.binding.weatherLayout.setBackgroundColor(Color.WHITE)
            holder.binding.tvTem.setTextColor(ContextCompat.getColor(context, R.color.grayText));
            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.lightGrayText));
        }

        if (position != 0) {
            holder.binding.time = getTime(current)
        } else {
            holder.binding.time = context.getString(R.string.now)
        }

        holder.binding.weatherLayout.setOnClickListener {
            listener(current)
            val previousSelectedPosition = selectedPosition
            selectedPosition =holder. adapterPosition
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    fun submitList(list: List<WeatherInfo>) {
        weatherList = list
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTime(current: WeatherInfo): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dtObject = LocalDateTime.parse(current.dt_txt, formatter)
        return dtObject.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

}