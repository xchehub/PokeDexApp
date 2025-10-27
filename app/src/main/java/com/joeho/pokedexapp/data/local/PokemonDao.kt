package com.joeho.pokedexapp.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query(
        "SELECT * FROM pokemon WHERE name LIKE '%' || :query || '%' ORDER BY name"
    )
    fun searchPokemon(query: String): PagingSource<Int, PokemonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemon: List<PokemonEntity>)

    @Query("DELETE FROM pokemon")
    suspend fun clearAll()

    @Query("SELECT * FROM pokemon WHERE name IN (:names)")
    suspend fun getPokemonByNames(names: List<String>): List<PokemonEntity>

    @Query("SELECT * FROM pokemon WHERE name = :name LIMIT 1")
    suspend fun getPokemon(name: String): PokemonEntity?

    @Query("SELECT * FROM pokemon WHERE name = :name LIMIT 1")
    fun observePokemon(name: String): Flow<PokemonEntity?>

    @Query("UPDATE pokemon SET isFavorite = :state WHERE name = :name")
    suspend fun updateFavorite(name: String, state: Boolean)

    @Query("SELECT * FROM pokemon WHERE isFavorite = 1 ORDER BY name")
    fun getFavoritePokemon(): PagingSource<Int, PokemonEntity>
}
