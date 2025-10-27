package com.joeho.pokedexapp.ui.tabA

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joeho.pokedex.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TabAFragment : Fragment(R.layout.fragment_tab_a) {

    private val viewModel: TabAViewModel by viewModels()
    private lateinit var adapter: PokemonAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerView)
        val search = view.findViewById<EditText>(R.id.searchInput)
        val progress = view.findViewById<ProgressBar>(R.id.progressBar)

        adapter = PokemonAdapter(
            onClick = { pokemon ->
                findNavController().navigate(
                    R.id.action_tabAFragment_to_pokemonDetailFragment,
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
            viewModel.pokemon.collectLatest {
                adapter.submitData(it)
            }
        }

        search.addTextChangedListener { text ->
            viewModel.updateQuery(text.toString())
        }

        adapter.addLoadStateListener { state ->
            progress.isVisible = state.refresh is LoadState.Loading
        }
    }
}
