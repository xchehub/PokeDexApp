package com.joeho.pokedexapp.ui.detail

import androidx.lifecycle.ViewModel
import com.joeho.pokedexapp.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    suspend fun getDetail(name: String) = repository.getPokemonDetail(name)
}
