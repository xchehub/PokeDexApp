package com.joeho.pokedexapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val name: String,
    val prevOffset: Int?,
    val nextOffset: Int?
)


