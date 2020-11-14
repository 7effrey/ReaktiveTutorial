package com.jeffrey.core.data.network

import InjectMocksRule
import com.badoo.reaktive.test.base.assertError
import com.badoo.reaktive.test.base.assertNotError
import com.badoo.reaktive.test.observable.test
import com.jeffrey.core.data.entity.MoviesResponse
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.http.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KtorHttpClientTest {

    private lateinit var httpClient: io.ktor.client.HttpClient

    private lateinit var ktorHttpClient: HttpClient

    private var defaultBaseUrl = "https://www.omdbapi.com/"

    private var defaultApiKey = "12345"

    private var defaultQuery = "test"

    private lateinit var moviesResponse1: MoviesResponse

    private lateinit var defaultParams: Map<String, String>

    @BeforeTest
    fun setup() {
        InjectMocksRule.createMockK(this)

        defaultParams = mapOf<String, String>(
            Pair("s", defaultQuery),
            Pair("page", "1"),
            Pair("apiKey", defaultApiKey)
        )

        // Mock HTTP Response
        httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/" -> {
                            val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json .toString()))
                            val queryParams = request.url.parameters
                            if (queryParams["apiKey"] != defaultApiKey) {
                                respond("Unauthorized", HttpStatusCode.Unauthorized)
                            } else {
                                var json = "{\"Response\":\"False\",\"Error\":\"Movie not found!\"}"
                                if (queryParams["s"] == defaultQuery && queryParams["page"] == "1") {
                                    json =
                                        "{\"Search\":[{\"Title\":\"The Secret Life of Frogs\",\"Year\":\"2019\",\"imdbID\":\"tt10220248\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BZTIxMTVlOTUtM2Y4NC00YjgxLWI1MDctMmJmYzk4NWJiNGM2XkEyXkFqcGdeQXVyMTAxNTAzNjIz._V1_SX300.jpg\"}],\"totalResults\":\"231\",\"Response\":\"True\"}"
                                }
                                respond(json, headers = responseHeaders)
                            }
                        }
                        else -> respond("Unauthorized", HttpStatusCode.NotFound)
                    }
                }
            }
        }
        ktorHttpClient = KtorHttpClient(defaultBaseUrl, true, httpClient)
    }

    @Test
    fun `searchMovie should trigger HttpClient to make request and return MoviesResponse`() {
        val observer = ktorHttpClient.get("", MoviesResponse.serializer(), defaultParams).test(false)

        observer.assertNotError()
        val actualMovies = observer.values[0]
        assertEquals(actualMovies.items!!.size, 1)
        val actualMovie = actualMovies.items!![0]
        assertEquals(actualMovie.title, "The Secret Life of Frogs")
        assertEquals(actualMovie.year, "2019")
        assertEquals(actualMovie.poster, "https://m.media-amazon.com/images/M/MV5BZTIxMTVlOTUtM2Y4NC00YjgxLWI1MDctMmJmYzk4NWJiNGM2XkEyXkFqcGdeQXVyMTAxNTAzNjIz._V1_SX300.jpg")
    }

    @Test
    fun `searchMovie with wrong apiKey will return an error`() {
        defaultParams = mapOf<String, String>(
            Pair("s", defaultQuery),
            Pair("page", "1"),
            Pair("apiKey", "abcd")
        )
        val observer = ktorHttpClient.get("", MoviesResponse.serializer(), defaultParams).test(false)

        observer.assertError {
            it is ClientRequestException && it.response.status == HttpStatusCode.Unauthorized
        }
    }

}