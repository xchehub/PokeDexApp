package com.joeho.pokedexapp.model

data class PokemonListResponse(
    val results: List<PokemonItem>
)

data class PokemonItem(
    val name: String,
    val url: String
)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val sprites: Sprites,
    val types: List<TypeSlot>,
    val abilities: List<AbilitySlot>
)

data class Sprites(val front_default: String?)
data class TypeSlot(val type: NamedResource)
data class AbilitySlot(val ability: NamedResource)
data class NamedResource(val name: String)
