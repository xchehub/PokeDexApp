package com.joeho.pokedexapp.ui.tabA

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.joeho.pokedexapp.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class TabAViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val pokemon = _query
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) repository.getPagedPokemon()
            else repository.searchPokemon(q)
        }
        .cachedIn(viewModelScope)

    fun updateQuery(text: String) {
        _query.value = text
    }
}
