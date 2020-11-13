package com.jeffrey.core.data.network

import com.badoo.reaktive.observable.Observable
import kotlinx.serialization.DeserializationStrategy

interface HttpClient {
    fun <T> get(path: String,
                deserializationStrategy: DeserializationStrategy<T>,
                queryParameters: Map<String, String>? = null,
                headers: Map<String, String>? = null
                ): Observable<T>

    fun <T, U> post(path: String,
                    deserializationStrategy: DeserializationStrategy<U>,
                    body: T? = null,
                    headers: Map<String, String>? = null): Observable<U>
}