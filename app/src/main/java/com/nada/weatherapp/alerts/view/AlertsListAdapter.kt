package com.nada.weatherapp.alerts.view

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
import com.nada.weatherapp.data.model.WeatherAlert
import com.nada.weatherapp.databinding.ItemAlertBinding

class AlertsListAdapter(private val itemListener: OnAlertRemoveListener) : ListAdapter<WeatherAlert, AlertsListAdapter.AlertViewHolder>(
        WeatherAlertDiffUtil()
    ) {
    private lateinit var binding: ItemAlertBinding

    inner class AlertViewHolder(var binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {
//        init {
//            itemView.setOnClickListener {
//                itemListener(getItem(adapterPosition))
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate(inflater, R.layout.item_alert, parent, false)
        return AlertViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val current = getItem(position)
        holder.binding.weatherAlert = current
        holder.binding.remove.setOnClickListener {
            itemListener.onClickRemove(current)
        }
    }

}

class WeatherAlertDiffUtil : DiffUtil.ItemCallback<WeatherAlert>() {
    override fun areItemsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert): Boolean {
        return oldItem == newItem
    }

}
