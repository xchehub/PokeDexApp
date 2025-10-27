package com.joeho.pokedexapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val name: String,
    val imageUrl: String?,
    val types: String,
    val isFavorite: Boolean = false
)
