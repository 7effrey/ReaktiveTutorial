package com.jeffrey.core.data.repository

import InjectMocksRule
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.test.base.assertNotError
import com.badoo.reaktive.test.observable.test
import com.jeffrey.core.data.entity.MovieResponse
import com.jeffrey.core.data.entity.MoviesResponse
import com.jeffrey.core.data.provider.OmdbProvider
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultMovieRepositoryTest {

    lateinit var repository: MovieRepository

    @MockK
    lateinit var omdbProvider: OmdbProvider

    private var defaultQuery = "test"

    private lateinit var movieResponse1: MoviesResponse
    private lateinit var movieResponse2: MoviesResponse

    @BeforeTest
    fun setup() {
        InjectMocksRule.createMockK(this)

        movieResponse1 = createMockMoviesResponse(true, 5)
        every { omdbProvider.searchMovie(defaultQuery, 1) } returns observableOf(movieResponse1)
        movieResponse2 = createMockMoviesResponse(false)
        every { omdbProvider.searchMovie(defaultQuery, 2) } returns observableOf(movieResponse2)

        repository = DefaultMovieRepository(omdbProvider)
    }

    @Test
    fun `repositorySearch should trigger omdbProvider to get MoviesResponse and transform it into List of Movies`() {

        val obsSearch1 = repository.search(defaultQuery, 1).test(false)
        obsSearch1.assertNotError()
        val actualMovies1 = obsSearch1.values[0]
        for (x in 1..actualMovies1.size) {
            val expectedMovie = movieResponse1.items!![x - 1]
            val actualMovie = actualMovies1[x - 1]
            assertEquals(actualMovie.imdbID, expectedMovie.imdbID)
            assertEquals(actualMovie.poster, expectedMovie.poster)
            assertEquals(actualMovie.title, expectedMovie.title)
            assertEquals(actualMovie.type, expectedMovie.type)
            assertEquals(actualMovie.year, expectedMovie.year)
        }
        verify { omdbProvider.searchMovie(defaultQuery, 1) }
    }

    private fun createMockMoviesResponse(success: Boolean, numOfItems: Int = 0): MoviesResponse {
        if (!success)
            return MoviesResponse(null, false)
        val list = mutableListOf<MovieResponse>()
        for (x in 1..numOfItems) {
            val movie = mockk<MovieResponse>()
            every { movie.title } returns "Title"
            every { movie.imdbID } returns "imdbId"
            every { movie.poster } returns "poster"
            every { movie.type } returns "type"
            every { movie.year } returns "year"
            list.add(movie)
        }
        return MoviesResponse(list, success)
    }

}