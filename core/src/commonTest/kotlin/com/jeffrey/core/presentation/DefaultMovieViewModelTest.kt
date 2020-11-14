package com.jeffrey.core.presentation

import InjectMocksRule
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.observableTimer
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.subject.publish.PublishSubject
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.assertValues
import com.badoo.reaktive.test.observable.test
import com.jeffrey.core.data.entity.Movie
import com.jeffrey.core.data.repository.MovieRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DefaultMovieViewModelTest {

    lateinit var viewModel: MovieViewModel<Movie>

    @MockK
    lateinit var repository: MovieRepository

    private var listMovies1: List<Movie> = listOf()
    private var listMovies2: List<Movie> = listOf()

    private val veGet = PublishSubject<String>()
    private val veLoadMore = PublishSubject<String>()
    private val vePressNext = PublishSubject<Unit>()

    private lateinit var viewData: MovieViewModel.ViewData<Movie>

    private var disposable: CompositeDisposable? = null

    private var defaultQuery = "test"

    @BeforeTest
    fun setup() {
        InjectMocksRule.createMockK(this)

        val list = mutableListOf<Movie>()
        for (x in 1..5) {
            val movie = mockk<Movie>()
            list.add(movie)
        }
        listMovies1 = list
        every { repository.search(defaultQuery, 1) } returns observableOf(listMovies1)

        val list2 = mutableListOf<Movie>()
        for (x in 1..5) {
            val movie = mockk<Movie>()
            list2.add(movie)
        }
        listMovies2 = list2
        every { repository.search(defaultQuery, 2) } returns observableOf(listMovies2)

        disposable = CompositeDisposable()

        viewModel = DefaultMovieViewModel<Movie>(repository, null)

        disposable?.add(viewModel.set(object: MovieViewModel.ViewEvent {
            override val get: Observable<String> = veGet
            override val loadMore: Observable<String> = veLoadMore
            override val pressNext: Observable<Unit> = vePressNext
        }))

        viewData = viewModel.generateViewData()
    }

    @AfterTest
    fun tearDown() {
        disposable?.dispose()
    }

    @Test
    fun `viewEvent get will show Loader and then return result`() {

        val obsLoading = viewData.loading.test(false)
        val obsResult = viewData.result.test(false)

        veGet.onNext(defaultQuery)

        // Wait for debounce because Reaktive Testing doesn't support .await()
        observableTimer(250, mainScheduler).subscribe(true) {
            obsLoading.assertValues(false, true, false)
            obsResult.assertValue(listMovies1)

            verify {
                repository.search(defaultQuery, 1)
            }
        }
    }
}