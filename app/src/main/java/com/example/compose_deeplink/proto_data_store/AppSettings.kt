package com.example.compose_deeplink.proto_data_store

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val language: Language = Language.ENGLISH,
    val knownLocation: PersistentList<Location> = persistentListOf()
) {
}

enum class Language { ENGLISH, GERMAN, SPANISH }

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)


