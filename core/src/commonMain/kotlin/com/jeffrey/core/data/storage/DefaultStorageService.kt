package com.jeffrey.core.data.storage

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableFromFunction
import com.badoo.reaktive.observable.observableOf
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class DefaultStorageService : StorageService {

    private var settings: Settings = Settings()

    fun setSettings(settings: Settings) {
        this.settings = settings
    }

    override fun getString(key: String): Observable<String> = observableOf(settings.getString(key, ""))

    override fun setString(key: String, value: String): Observable<Unit> {
        return observableFromFunction {
            settings[key] = value
        }
    }

    override fun getInt(key: String): Observable<Int> = observableOf(settings.getInt(key, 0))

    override fun setInt(key: String, value: Int): Observable<Unit> {
        return observableFromFunction {
            settings[key] = value
        }
    }

}