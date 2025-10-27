package com.joeho.pokedexapp.ui.tabA

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.joeho.pokedex.databinding.ItemPokemonBinding
import com.joeho.pokedexapp.data.local.PokemonEntity

class PokemonAdapter(
    private val onClick: (PokemonEntity) -> Unit
) : PagingDataAdapter<PokemonEntity, PokemonAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PokemonEntity) {
            binding.textName.text = item.name.replaceFirstChar { it.uppercase() }
            binding.textType.text = item.types
            binding.imageView.load(item.imageUrl)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class DiffCallback : DiffUtil.ItemCallback<PokemonEntity>() {
        override fun areItemsTheSame(oldItem: PokemonEntity, newItem: PokemonEntity) =
            oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: PokemonEntity, newItem: PokemonEntity) = oldItem == newItem
    }
}
