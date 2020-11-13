package com.jeffrey.core.data.repository

import com.jeffrey.core.data.mapper.MoviesMapper
import com.jeffrey.core.data.entity.Movie
import com.jeffrey.core.data.provider.OmdbProvider
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map

class DefaultMovieRepository(
    private val omdbProvider: OmdbProvider
) : MovieRepository {
    override fun search(query: String, page: Int): Observable<List<Movie>> {
        return omdbProvider.searchMovie(query, page).map {
            return@map MoviesMapper().transform(it)
        }
    }
}