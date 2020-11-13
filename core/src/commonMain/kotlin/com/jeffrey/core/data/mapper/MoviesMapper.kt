package com.jeffrey.core.data.mapper

import com.jeffrey.core.data.entity.Movie
import com.jeffrey.core.data.entity.MoviesResponse

class MoviesMapper: Mapper<MoviesResponse, List<Movie>> {

    override fun transform(response: MoviesResponse): List<Movie> {
        if (!response.success || response.items == null)
            return listOf()
        return response.items.map {
            Movie(
                it.title,
                it.imdbID,
                it.year,
                it.type,
                it.poster
            )
        }
    }
}