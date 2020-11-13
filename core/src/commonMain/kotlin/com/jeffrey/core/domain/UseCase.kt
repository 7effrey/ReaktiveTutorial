package com.jeffrey.core.domain

/**
 * Not to use UseCase at the moment to reduce boilerplate code
 */
interface UseCase<in R, T> {
    suspend fun execute(request: R?): T
}