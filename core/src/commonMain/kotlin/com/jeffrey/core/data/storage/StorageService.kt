package com.jeffrey.core.data.storage

import com.badoo.reaktive.observable.Observable

interface StorageService {
    fun getString(key: String): Observable<String>
    fun setString(key: String, value: String): Observable<Unit>
    fun getInt(key: String): Observable<Int>
    fun setInt(key: String, value: Int): Observable<Unit>
}