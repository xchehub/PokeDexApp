package com.joeho.pokedexapp.data.repository

import androidx.paging.*
import com.joeho.pokedexapp.data.local.AppDatabase
import com.joeho.pokedexapp.data.local.PokemonEntity
import com.joeho.pokedexapp.data.remote.PokeApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepository @Inject constructor(
    private val api: PokeApiService,
    private val db: AppDatabase
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getPagedPokemon(): Flow<PagingData<PokemonEntity>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = PokemonRemoteMediator(api, db),
            pagingSourceFactory = { db.pokemonDao().searchPokemon("") }
        ).flow

    fun searchPokemon(query: String): Flow<PagingData<PokemonEntity>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { db.pokemonDao().searchPokemon(query) }
        ).flow

    suspend fun getPokemonDetail(name: String) = api.getPokemonDetail(name)
}
