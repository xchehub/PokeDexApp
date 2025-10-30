package com.joeho.pokedexapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.joeho.pokedexapp.data.local.AppDatabase
import com.joeho.pokedexapp.data.local.PokemonEntity
import com.joeho.pokedexapp.data.remote.PokeApiService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepository @Inject constructor(
    private val api: PokeApiService,
    private val db: AppDatabase
) {

    private val pokemonDao = db.pokemonDao()

    @OptIn(ExperimentalPagingApi::class)
    fun getPagedPokemon(): Flow<PagingData<PokemonEntity>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            remoteMediator = PokemonRemoteMediator(api, this),
            pagingSourceFactory = { pokemonDao.searchPokemon("") }
        ).flow

    fun searchPokemon(query: String): Flow<PagingData<PokemonEntity>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { pokemonDao.searchPokemon(query) }
        ).flow

    fun getFavorites(): Flow<PagingData<PokemonEntity>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { pokemonDao.getFavoritePokemon() }
        ).flow

    suspend fun toggleFavorite(name: String) {
        val pokemon = pokemonDao.getPokemon(name) ?: return
        pokemonDao.updateFavorite(name, !pokemon.isFavorite)
    }

    fun observePokemon(name: String): Flow<PokemonEntity?> = pokemonDao.observePokemon(name)

    private val logger = KotlinLogging.logger {}

    suspend fun getPokemonDetail(name: String) = try {
        api.getPokemonDetail(name)
    } catch (e: Exception) {
        logger.error(e) { "getPokemonDetail failed for $name: ${e.message}" }
        throw e
    }

    internal suspend fun savePokemon(
        pokemon: List<PokemonEntity>,
        clearExisting: Boolean
    ) {
        if (pokemon.isEmpty()) {
            if (clearExisting) {
                db.withTransaction { pokemonDao.clearAll() }
            }
            return
        }

        val merged = mergeFavoriteState(pokemon)
        db.withTransaction {
            if (clearExisting) {
                pokemonDao.clearAll()
            }
            pokemonDao.insertAll(merged)
        }
    }

    private suspend fun mergeFavoriteState(
        pokemon: List<PokemonEntity>
    ): List<PokemonEntity> {
        val names = pokemon.map { it.name }
        val existing = pokemonDao.getPokemonByNames(names)
        val favoriteLookup = existing.associateBy { it.name }
        return pokemon.map { entity ->
            val favorite = favoriteLookup[entity.name]?.isFavorite ?: false
            entity.copy(isFavorite = favorite)
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
