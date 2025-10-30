package com.joeho.pokedexapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.joeho.pokedexapp.data.local.AppDatabase
import com.joeho.pokedexapp.data.local.PokemonEntity
import com.joeho.pokedexapp.data.local.RemoteKeys
import com.joeho.pokedexapp.data.remote.PokeApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val api: PokeApiService,
    private val db: AppDatabase,
    private val repository: PokemonRepository
) : RemoteMediator<Int, PokemonEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prev = remoteKeys?.prevOffset
                    if (prev == null) return MediatorResult.Success(endOfPaginationReached = true)
                    prev
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val next = remoteKeys?.nextOffset
                    if (next == null) return MediatorResult.Success(endOfPaginationReached = true)
                    next
                }
            }

            val response = api.getPokemonList(limit = PAGE_SIZE, offset = offset)
            val pokemonList = coroutineScope {
                // Limit concurrent detail requests to avoid rate limiting
                val semaphore = Semaphore(permits = MAX_CONCURRENT_REQUESTS)
                response.results.map { item ->
                    async {
                        semaphore.withPermit {
                            try {
                                val detail = api.getPokemonDetail(item.name)
                                PokemonEntity(
                                    name = detail.name,
                                    imageUrl = detail.sprites.front_default,
                                    types = detail.types.joinToString(", ") { it.type.name }
                                )
                            } catch (t: Throwable) {
                                null
                            }
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            // Determine keys
            val endOfPaginationReached = pokemonList.isEmpty()
            val prevOffset = if (offset == 0) null else maxOf(0, offset - PAGE_SIZE)
            val nextOffset = if (endOfPaginationReached) null else offset + pokemonList.size

            // Save data and keys
            if (loadType == LoadType.REFRESH) {
                db.withTransaction {
                    db.remoteKeysDao().clearRemoteKeys()
                }
            }

            repository.savePokemon(
                pokemonList,
                clearExisting = loadType == LoadType.REFRESH
            )

            db.withTransaction {
                val keys = pokemonList.map { entity ->
                    RemoteKeys(
                        name = entity.name,
                        prevOffset = prevOffset,
                        nextOffset = nextOffset
                    )
                }
                if (keys.isNotEmpty()) db.remoteKeysDao().insertAll(keys)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PokemonEntity>): RemoteKeys? {
        val lastItem = state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
        return lastItem?.let { db.remoteKeysDao().remoteKeysByName(it.name) }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, PokemonEntity>): RemoteKeys? {
        val firstItem = state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
        return firstItem?.let { db.remoteKeysDao().remoteKeysByName(it.name) }
    }

    companion object {
        private const val PAGE_SIZE = PokemonRepository.PAGE_SIZE
        private const val MAX_CONCURRENT_REQUESTS = 4
    }
}
