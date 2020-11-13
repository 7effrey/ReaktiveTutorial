package com.jeffrey.core.presentation

import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.badoo.reaktive.subject.publish.PublishSubject
import com.badoo.reaktive.subject.replay.ReplaySubject
import com.jeffrey.core.data.entity.Movie
import com.jeffrey.core.data.mapper.Mapper
import com.jeffrey.core.data.repository.MovieRepository

class DefaultTestViewModel<O>(
    private val movieRepository: MovieRepository,
    private val mapper: Mapper<List<Movie>, List<O>>?
) : TestViewModel<O> {

    override val output: Observable<TestViewModel.Output>
        get() = oMain

    override val outputW: ObservableWrapper<TestViewModel.Output>
        get() = output.wrap()

    override val input: TestViewModel.Input = object: TestViewModel.Input {
        override fun scrollToTop() = iScrollToTop.onNext(Unit)
    }

    override val viewEvent: TestViewModel.ViewEvent = object: TestViewModel.ViewEvent {
        override fun get(query: String) = veGet.onNext(query)

        override fun loadMore(query: String) = veLoadMore.onNext(query)

        override fun pressNext() = vePressNext.onNext(Unit)

    }

    private val oMain: ReplaySubject<TestViewModel.Output> = ReplaySubject(1)

    private val iScrollToTop = PublishSubject<Unit>()

    private val veGet = PublishSubject<String>()
    private val veLoadMore = PublishSubject<String>()
    private val vePressNext = PublishSubject<Unit>()

    private val vdLoading: BehaviorSubject<Boolean> = BehaviorSubject(false)
    private val vdResult: BehaviorSubject<List<O>> = BehaviorSubject(listOf())

    private var prevRequest = ""
    private var pageSubject = BehaviorSubject(1)

    override fun set(): Disposable {
        var disposable = CompositeDisposable()

        disposable.add(merge(
                veGet.map {
                    pageSubject.onNext(1)
                    return@map it
                },
                veLoadMore,
            )
            .debounce(200, mainScheduler)
            .filter { !vdLoading.value }
            .flatMap { query ->
                vdLoading.onNext(true)
                return@flatMap movieRepository.search(query, pageSubject.value)
            }
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .doOnAfterNext {
                vdLoading.onNext(false)
            }
            .subscribe {
                val newItems = mutableListOf<O>()

                if (pageSubject.value > 1) {
                    newItems.addAll(vdResult.value)
                }
                pageSubject.onNext(pageSubject.value + 1)

                @Suppress("UNCHECKED_CAST")
                val list = mapper?.transform(it) ?: it as List<O>

                newItems.addAll(list)

                vdResult.onNext(newItems)
            })

        disposable.add(vePressNext.subscribe {
            oMain.onNext(TestViewModel.Output.NavigateToDetail)
        })

        return disposable
    }

    override fun generateViewData(): TestViewModel.ViewData<O> {
        return object: TestViewModel.ViewData<O> {
            override val loading: Observable<Boolean>
                get() = vdLoading
            override val result: Observable<List<O>>
                get() = vdResult
            override val loadingW: ObservableWrapper<Boolean>
                get() = loading.wrap()
            override val resultW: ObservableWrapper<List<O>>
                get() = result.wrap()

        }
    }
}