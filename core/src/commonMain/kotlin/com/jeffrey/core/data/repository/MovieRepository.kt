package com.jeffrey.core.data.repository

import com.jeffrey.core.data.entity.Movie
import com.badoo.reaktive.observable.Observable

interface MovieRepository {
    fun search(query: String, page: Int = 1): Observable<List<Movie>>
}