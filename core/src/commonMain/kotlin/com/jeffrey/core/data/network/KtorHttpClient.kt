package com.jeffrey.core.data.network

import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.flatMapSingle
import com.badoo.reaktive.observable.observableOf
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.DeserializationStrategy

class KtorHttpClient(
    private val baseUrl: String,
) : HttpClient {

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }

        /**
         * Remove logging as the latest ktor release has changed
         * coroutines pattern usage and AwaitAll bug,
         * Activating logging will cause crash on K/N awaitAll
         */
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
    }

    private fun HttpRequestBuilder.apiUrl(path: String? = null) {
        header(HttpHeaders.CacheControl, "no-cache")
        url {
            takeFrom(baseUrl)
        }
    }

    override fun <T> get(
        path: String,
        deserializationStrategy: DeserializationStrategy<T>,
        queryParameters: Map<String, String>?,
        headers: Map<String, String>?
    ): Observable<T> {
        return observableOf(true).flatMapSingle {
            return@flatMapSingle singleFromCoroutine {
                val httpResponse = client.get<HttpStatement> {
                    apiUrl()
                    queryParameters?.let {
                        it.keys.forEach { key ->
                            parameter(key, it[key])
                        }
                    }
                    headers?.let { addedHeaders ->
                        addedHeaders.forEach {
                            this.headers.append(it.key, it.value)
                        }
                    }
                    println("GET Api Call: ${this.url.buildString()}")
                }

                val json = httpResponse.execute().readText()
                return@singleFromCoroutine kotlinx.serialization.json.Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                }.decodeFromString(deserializationStrategy, json)
            }
        }

    }

    override fun <T, U> post(path: String,
                             deserializationStrategy: DeserializationStrategy<U>,
                             body: T?,
                             headers: Map<String, String>?
    ): Observable<U> {
        return observableOf(true).flatMapSingle {
            return@flatMapSingle singleFromCoroutine {
                val httpResponse = client.get<HttpStatement> {
                    apiUrl()
                    body?.let {
                        this.body = it
                    }
                    headers?.let { addedHeaders ->
                        addedHeaders.forEach {
                            this.headers.append(it.key, it.value)
                        }
                    }
                    println("POST Api Call: ${this.url.buildString()}")
                }

                val json = httpResponse.execute().readText()
                return@singleFromCoroutine kotlinx.serialization.json.Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                }.decodeFromString(deserializationStrategy, json)
            }
        }
    }
}