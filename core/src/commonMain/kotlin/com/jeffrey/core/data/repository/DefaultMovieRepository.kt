package com.jeffrey.core.data.repository

import com.jeffrey.core.data.mapper.MoviesMapper
import com.jeffrey.core.data.entity.Movie
import com.jeffrey.core.data.provider.OmdbProvider
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.flatMap
import com.badoo.reaktive.observable.map
import com.jeffrey.core.data.storage.StorageService

class DefaultMovieRepository(
    private val omdbProvider: OmdbProvider,
    private val storageService: StorageService
) : MovieRepository {
    override fun search(query: String, page: Int): Observable<List<Movie>> {
        return omdbProvider.searchMovie(query, page)
            .flatMap { response ->
                return@flatMap storageService.getInt(MovieRepository.KEY_NUM_OF_SEARCH)
                    .flatMap { counter ->
                        storageService.setInt(MovieRepository.KEY_NUM_OF_SEARCH, counter + 1)
                    }
                    .map { MoviesMapper().transform(response) }
            }
    }

    override fun getNumberOfSearches(): Observable<Int> {
        return storageService.getInt(MovieRepository.KEY_NUM_OF_SEARCH)
    }
}