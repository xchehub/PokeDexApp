package com.joeho.pokedexapp.ui.tabB

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joeho.pokedex.R
import com.joeho.pokedexapp.ui.tabA.PokemonAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TabBFragment : Fragment(R.layout.fragment_tab_b) {
    private val viewModel: TabBFavoritesViewModel by viewModels()
    private lateinit var adapter: PokemonAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerFavorites)
        val emptyView = view.findViewById<TextView>(R.id.emptyFavorites)
        val progress = view.findViewById<ProgressBar>(R.id.progressBarFavorites)

        adapter = PokemonAdapter(
            onClick = { pokemon ->
                findNavController().navigate(
                    R.id.action_tabBFragment_to_pokemonDetailFragment,
                    Bundle().apply { putString("name", pokemon.name) }
                )
            },
            onFavoriteClick = { pokemon ->
                viewModel.toggleFavorite(pokemon.name)
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favorites.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        adapter.addLoadStateListener { state ->
            progress.isVisible = state.refresh is LoadState.Loading
            val isEmpty = state.refresh is LoadState.NotLoading && adapter.itemCount == 0
            emptyView.isVisible = isEmpty
        }
    }
}
