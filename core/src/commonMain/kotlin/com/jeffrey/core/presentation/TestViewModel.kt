package com.jeffrey.core.presentation

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper

interface TestViewModel<O> {

    val output: Observable<Output>
    val outputW: ObservableWrapper<Output>
    val input: Input
    val viewEvent: ViewEvent
    fun set(): Disposable
    fun generateViewData(): ViewData<O>

    interface ViewEvent {
        fun get(query: String)
        fun loadMore(query: String)
        fun pressNext()
    }

    interface ViewData<O> {
        val loading: Observable<Boolean>
        val result: Observable<List<O>>

        val loadingW: ObservableWrapper<Boolean>
        val resultW: ObservableWrapper<List<O>>
    }

    sealed class Output {
        object NavigateToDetail : Output()
    }

    interface Input {
       fun scrollToTop()
    }
}