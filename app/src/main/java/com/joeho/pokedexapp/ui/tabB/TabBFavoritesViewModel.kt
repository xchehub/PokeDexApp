package com.joeho.pokedexapp.ui.tabB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.joeho.pokedexapp.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabBFavoritesViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    val favorites = repository.getFavorites().cachedIn(viewModelScope)

    fun toggleFavorite(name: String) {
        viewModelScope.launch {
            repository.toggleFavorite(name)
        }
    }
}
