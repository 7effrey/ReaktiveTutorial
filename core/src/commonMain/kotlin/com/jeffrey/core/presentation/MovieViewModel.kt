package com.jeffrey.core.presentation

import com.jeffrey.core.util.reaktive.PublishSubjectWrapper
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper

interface MovieViewModel<O> {

    val output: ObservableWrapper<Output>
    val input: PublishSubjectWrapper<Input>
    fun set(viewEvent: ViewEvent): Disposable
    fun generateViewData(): ViewData<O>

    interface ViewEvent {
        val get: Observable<String>
        val loadMore: Observable<String>
        val pressNext: Observable<Unit>
    }

    interface ViewData<O> {
        val loading: ObservableWrapper<Boolean>
        val result: ObservableWrapper<List<O>>
    }

    sealed class Output {
        object NavigateToDetail : Output()
    }

    sealed class Input {
        object ScrollToTop : Input()
    }
}