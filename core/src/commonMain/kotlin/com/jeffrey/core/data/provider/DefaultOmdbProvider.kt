package com.jeffrey.core.data.provider

import com.jeffrey.core.data.entity.MoviesResponse
import com.jeffrey.core.data.network.HttpClient
import com.badoo.reaktive.observable.Observable

class DefaultOmdbProvider(
    private val httpClient: HttpClient,
    private val apiKey: String
) : OmdbProvider {
    override fun searchMovie(query: String, page: Int): Observable<MoviesResponse> {
        val params = mapOf<String, String>(
            Pair("s", query),
            Pair("page", page.toString()),
            Pair("apiKey", apiKey)
        )
        return httpClient.get("", MoviesResponse.serializer(), params)
    }
}