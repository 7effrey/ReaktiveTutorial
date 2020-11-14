package com.jeffrey.core.presentation

import InjectMocksRule
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.subject.publish.PublishSubject
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.assertValues
import com.badoo.reaktive.test.observable.test
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.jeffrey.core.data.entity.Movie
import com.jeffrey.core.data.repository.MovieRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

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

    private var testScheduler: TestScheduler = TestScheduler()

    @BeforeTest
    fun setup() {
        InjectMocksRule.createMockK(this)

        listMovies1 = createMockListOfMovies(5)
        every { repository.search(defaultQuery, 1) } returns observableOf(listMovies1)

        listMovies2 = createMockListOfMovies(5)
        every { repository.search(defaultQuery, 2) } returns observableOf(listMovies2)

        disposable = CompositeDisposable()

        viewModel = DefaultMovieViewModel(repository, null, testScheduler, testScheduler)

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

        testScheduler.timer.advanceBy(250)

        obsLoading.assertValues(false, true, false)
        assertTrue { obsResult.values[0].isEmpty() }
        assertTrue { obsResult.values[1].size == listMovies1.size }

        verify {
            repository.search(defaultQuery, 1)
        }
    }

    @Test
    fun `viewEvent get will show Loader and return result1 and then viewEvent loadMore will showLoader and return result2`() {

        val obsLoading = viewData.loading.test(false)
        val obsResult = viewData.result.test(false)

        veGet.onNext(defaultQuery)

        testScheduler.timer.advanceBy(250)

        obsLoading.assertValues(false, true, false)
        assertTrue { obsResult.values[0].isEmpty() }
        assertTrue { obsResult.values[1].size == listMovies1.size }

        verify {
            repository.search(defaultQuery, 1)
        }

        veLoadMore.onNext(defaultQuery)

        testScheduler.timer.advanceBy(250)

        obsLoading.assertValues(false, true, false, true, false)
        assertTrue { obsResult.values[2].size == listMovies1.size + listMovies2.size}

        verify {
            repository.search(defaultQuery, 2)
        }
    }

    @Test
    fun `viewEvent pressNext will emit Output event NavigateToDetails`() {
        val obsOutput = viewModel.output.test(false)

        vePressNext.onNext(Unit)

        obsOutput.assertValue(MovieViewModel.Output.NavigateToDetail)
    }

    private fun createMockListOfMovies(numOfItems: Int = 0): List<Movie> {
        val list2 = mutableListOf<Movie>()
        for (x in 1..numOfItems) {
            val movie = mockk<Movie>()
            list2.add(movie)
        }
        return list2
    }
}