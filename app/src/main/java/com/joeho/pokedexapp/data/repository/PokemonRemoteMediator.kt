package com.joeho.pokedexapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.joeho.pokedexapp.data.local.PokemonEntity
import com.joeho.pokedexapp.data.remote.PokeApiService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val api: PokeApiService,
    private val repository: PokemonRepository
) : RemoteMediator<Int, PokemonEntity>() {

    private val logger = KotlinLogging.logger {}

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.APPEND -> state.pages.sumOf { it.data.size }
                else -> return MediatorResult.Success(endOfPaginationReached = true)
            }

            val response = api.getPokemonList(limit = PAGE_SIZE, offset = offset)
            val pokemonList = coroutineScope {
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
                                logger.error(t) { "Failed to load detail for ${item.name}" }
                                null
                            }
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            repository.savePokemon(
                pokemonList,
                clearExisting = loadType == LoadType.REFRESH
            )

            MediatorResult.Success(endOfPaginationReached = pokemonList.isEmpty())
        } catch (e: Exception) {
            logger.error(e) { "RemoteMediator load failed with ${e.message}" }
            MediatorResult.Error(e)
        }
    }

    companion object {
        private const val PAGE_SIZE = PokemonRepository.PAGE_SIZE
        private const val MAX_CONCURRENT_REQUESTS = 4
    }
}
