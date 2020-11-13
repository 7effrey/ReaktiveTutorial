package com.jeffrey.core.presentation

import com.jeffrey.core.data.mapper.Mapper
import com.jeffrey.core.data.entity.Movie
import com.jeffrey.core.data.repository.MovieRepository
import com.jeffrey.core.util.reaktive.PublishSubjectWrapper
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.badoo.reaktive.subject.replay.ReplaySubject

class DefaultMovieViewModel<O>(
    private val movieRepository: MovieRepository,
    private val mapper: Mapper<List<Movie>, List<O>>?
) : MovieViewModel<O> {

    override val output: ObservableWrapper<MovieViewModel.Output>
        get() = oMain.wrap()

    override val input: PublishSubjectWrapper<MovieViewModel.Input>
        get() = iMain

    private val oMain: ReplaySubject<MovieViewModel.Output> = ReplaySubject(1)

    private val iMain = PublishSubjectWrapper<MovieViewModel.Input>()

    private val vdLoading: BehaviorSubject<Boolean> = BehaviorSubject(false)
    private val vdResult: BehaviorSubject<List<O>> = BehaviorSubject(listOf())

    private var prevRequest = ""
    private var pageSubject = BehaviorSubject(1)

    override fun set(viewEvent: MovieViewModel.ViewEvent): Disposable {
        var disposable = CompositeDisposable()

        disposable.add(merge(
            viewEvent.get.map {
                pageSubject.onNext(1)
                return@map it
            },
            viewEvent.loadMore,
        )
            .debounce(200, mainScheduler)
            .filter { !vdLoading.value }
            .flatMap { query ->
                vdLoading.onNext(true)
                return@flatMap movieRepository.search(query, pageSubject.value)
                    .onErrorReturnValue(listOf())
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

        disposable.add(viewEvent.pressNext.subscribe {
            oMain.onNext(MovieViewModel.Output.NavigateToDetail)
        })

        return disposable
    }

    override fun generateViewData(): MovieViewModel.ViewData<O> {
        return object: MovieViewModel.ViewData<O> {
            override val loading: ObservableWrapper<Boolean>
                get() = vdLoading.wrap()
            override val result: ObservableWrapper<List<O>>
                get() = vdResult.wrap()

        }
    }
}