package com.jeffrey.core.data.repository

import com.jeffrey.core.data.entity.Movie
import com.badoo.reaktive.observable.Observable

interface MovieRepository {
    fun search(query: String, page: Int = 1): Observable<List<Movie>>
    fun getNumberOfSearches(): Observable<Int>

    companion object {
        internal val KEY_NUM_OF_SEARCH = "KEY_NUM_OF_SEARCH"
    }
}