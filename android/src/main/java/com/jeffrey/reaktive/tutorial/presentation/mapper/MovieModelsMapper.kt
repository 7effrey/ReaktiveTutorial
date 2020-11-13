package com.jeffrey.reaktive.tutorial.presentation.mapper

import com.jeffrey.core.data.mapper.Mapper
import com.jeffrey.core.data.entity.Movie
import com.jeffrey.reaktive.tutorial.presentation.model.MovieModel

class MovieModelsMapper: Mapper<List<Movie>, List<MovieModel>> {

    override fun transform(response: List<Movie>): List<MovieModel> {
        return response.map {
            MovieModel(
                it.title,
                it.imdbID,
                it.year,
                it.type,
                it.poster
            )
        }
    }
}