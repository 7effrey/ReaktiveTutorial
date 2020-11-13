package com.jeffrey.core.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesResponse(
    @SerialName("Search")
    val items: List<MovieResponse>? = null,
    @SerialName("Response")
    val success: Boolean
)

@Serializable
data class MovieResponse(
    @SerialName("Title") val title: String,
    @SerialName("imdbID") val imdbID: String,
    @SerialName("Year") val year: String,
    @SerialName("Type") val type: String,
    @SerialName("Poster") val poster: String
)