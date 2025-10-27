package com.joeho.pokedexapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joeho.pokedexapp.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    suspend fun getDetail(name: String) = repository.getPokemonDetail(name)

    fun observePokemon(name: String) = repository.observePokemon(name)

    fun toggleFavorite(name: String) {
        viewModelScope.launch {
            repository.toggleFavorite(name)
        }
    }
}
