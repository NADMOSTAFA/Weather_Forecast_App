package com.nada.weatherapp.favorites.view

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
import com.nada.weatherapp.data.model.FavoriteWeather
import com.nada.weatherapp.databinding.ItemFavoriteBinding


class FavoriteListAdapter (private val itemListener: (FavoriteWeather) -> Unit,
                           private val onclick: onRemoveClickListener
) :
    ListAdapter<FavoriteWeather, FavoriteListAdapter.FavoriteViewHolder>(
        FavoriteDiffUtil()
    ) {
    private lateinit var binding: ItemFavoriteBinding

    inner class FavoriteViewHolder(var binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                itemListener(getItem(adapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate(inflater, R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val current = getItem(position)
        if (current.city != ""){
            current.city += ", "
        }
        holder.binding.weather = current
        holder.binding.remove.setOnClickListener {
            onclick.onClickRemove(current)
        }
    }

}

class FavoriteDiffUtil : DiffUtil.ItemCallback<FavoriteWeather>() {
    override fun areItemsTheSame(oldItem: FavoriteWeather, newItem: FavoriteWeather): Boolean {
        return oldItem.city == newItem.city
    }

    override fun areContentsTheSame(oldItem: FavoriteWeather, newItem: FavoriteWeather): Boolean {
        return oldItem == newItem
    }
}