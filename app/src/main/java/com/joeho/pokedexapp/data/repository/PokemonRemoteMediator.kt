package com.joeho.pokedexapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.joeho.pokedexapp.data.local.AppDatabase
import com.joeho.pokedexapp.data.local.PokemonEntity
import com.joeho.pokedexapp.data.remote.PokeApiService

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val api: PokeApiService,
    private val db: AppDatabase
) : RemoteMediator<Int, PokemonEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.APPEND -> state.pages.size * 20
                else -> return MediatorResult.Success(endOfPaginationReached = true)
            }

            val response = api.getPokemonList(limit = 20, offset = offset)
            val pokemonList = response.results.map { item ->
                val detail = api.getPokemonDetail(item.name)
                PokemonEntity(
                    name = detail.name,
                    imageUrl = detail.sprites.front_default,
                    types = detail.types.joinToString(", ") { it.type.name }
                )
            }

            db.withTransaction {
                if (loadType == LoadType.REFRESH) db.pokemonDao().clearAll()
                db.pokemonDao().insertAll(pokemonList)
            }

            MediatorResult.Success(endOfPaginationReached = pokemonList.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
