package com.jeffrey.core.data.mapper

interface Mapper<in T, out E> {
    fun transform(response: T): E
}