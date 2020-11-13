package com.jeffrey.core.data.provider

import com.jeffrey.core.data.entity.MoviesResponse
import com.badoo.reaktive.observable.Observable

interface OmdbProvider {
    fun searchMovie(query: String, page: Int = 1): Observable<MoviesResponse>
}