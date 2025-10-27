package com.joeho.pokedexapp.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.joeho.pokedex.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokemonDetailFragment : Fragment(R.layout.fragment_pokemon_detail) {

    private val viewModel: PokemonDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nameArg = arguments?.getString("name") ?: return
        val backButton = view.findViewById<ImageButton>(R.id.buttonBack)
        val image = view.findViewById<ImageView>(R.id.imageView)
        val name = view.findViewById<TextView>(R.id.textName)
        val types = view.findViewById<TextView>(R.id.textTypes)
        val abilities = view.findViewById<TextView>(R.id.textAbilities)
        val favoriteIcon = view.findViewById<ImageView>(R.id.favoriteIcon)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        favoriteIcon.setOnClickListener {
            viewModel.toggleFavorite(nameArg)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.observePokemon(nameArg).collectLatest { entity ->
                val isFavorite = entity?.isFavorite == true
                favoriteIcon.isVisible = entity != null
                favoriteIcon.setImageResource(
                    if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border
                )
                favoriteIcon.contentDescription = getString(
                    if (isFavorite) R.string.favorite_remove else R.string.favorite_add
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val detail = viewModel.getDetail(nameArg)
            name.text = detail.name
            image.load(detail.sprites.front_default)
            types.text = detail.types.joinToString(", ") { it.type.name }
            abilities.text = detail.abilities.joinToString(", ") { it.ability.name }
        }
    }
}
