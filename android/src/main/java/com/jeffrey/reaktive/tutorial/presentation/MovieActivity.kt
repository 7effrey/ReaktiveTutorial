package com.jeffrey.reaktive.tutorial.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.adrena.core.data.cache.DatabaseHelper
import com.jeffrey.core.data.network.HttpClient
import com.jeffrey.core.data.network.KtorHttpClient
import com.jeffrey.core.data.provider.DefaultOmdbProvider
import com.jeffrey.core.data.provider.OmdbProvider
import com.jeffrey.core.data.repository.DefaultMovieRepository
import com.jeffrey.core.data.repository.MovieRepository
import com.jeffrey.core.presentation.DefaultMovieViewModel
import com.jeffrey.core.presentation.MovieViewModel
import com.adrena.core.sql.MoviesDatabase
import com.jeffrey.reaktive.tutorial.presentation.mapper.MovieModelsMapper
import com.jeffrey.reaktive.tutorial.presentation.adapter.MoviesAdapter
import com.jeffrey.reaktive.tutorial.R
import com.jeffrey.reaktive.tutorial.presentation.model.MovieModel
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.subject.publish.PublishSubject
import com.facebook.stetho.Stetho
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.android.synthetic.main.activity_main.*

class MovieActivity : AppCompatActivity() {

    private lateinit var mMoviesAdapter: MoviesAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRefreshLayout: SwipeRefreshLayout

    private var mIsRefreshing = false

    private var query = "sesame street"

    // This should be singleton
    private val dbHelper: DatabaseHelper by lazy {
        val driver = AndroidSqliteDriver(
            schema = MoviesDatabase.Schema,
            context = this,
            name = "movie.db"
        )
        DatabaseHelper("movie.db", driver)
    }

    private val mViewModel: MovieViewModel<MovieModel> by lazy {
        val httpClient: HttpClient = KtorHttpClient("https://www.omdbapi.com/")

        val service: OmdbProvider = DefaultOmdbProvider(httpClient, "b445ca0b")

        val repository: MovieRepository = DefaultMovieRepository(service)

        val modelMapper = MovieModelsMapper()

        return@lazy DefaultMovieViewModel(repository, modelMapper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Stetho.initializeWithDefaults(this)

        setContentView(R.layout.activity_main)

        mRecyclerView = findViewById(R.id.listing)
        mRefreshLayout = findViewById(R.id.refresh_layout)

        mMoviesAdapter = MoviesAdapter()

        mRecyclerView.layoutManager = GridLayoutManager(this, 2)
        mRecyclerView.adapter = mMoviesAdapter
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val manager = listing.layoutManager as LinearLayoutManager

                val totalItemCount = manager.itemCount
                val lastVisibleItem = manager.findLastVisibleItemPosition()

                if (!mIsRefreshing && totalItemCount <= lastVisibleItem + 2) {
                    loadMore()
                }
            }
        })

        binding()

        mRefreshLayout.setOnRefreshListener {
            get()
        }

        btnNext.setOnClickListener {
            Toast.makeText(this, "Press Next", Toast.LENGTH_SHORT).show()
            pressNext()
        }

        get()
    }

    override fun onDestroy() {
        disposables?.dispose()

        super.onDestroy()
    }

    private var disposables: CompositeDisposable? = null

    private var veGet = PublishSubject<String>()
    private var veLoadMore = PublishSubject<String>()
    private var vePressNext = PublishSubject<Unit>()

    private fun binding() {
        disposables = CompositeDisposable()

        disposables?.add(mViewModel.set(object: MovieViewModel.ViewEvent {
            override val get: Observable<String> = veGet
            override val loadMore: Observable<String> = veLoadMore
            override val pressNext: Observable<Unit> = vePressNext

        }))

        val viewData = mViewModel.generateViewData()
        disposables?.add(
            viewData.loading.subscribe(true, onNext = ::loading)
        )
        disposables?.add(
            viewData.result.subscribe(true, onNext = ::result)
        )
        disposables?.add(mViewModel.output
            .subscribe(true) {
                if (it is MovieViewModel.Output.NavigateToDetail)
                    Toast.makeText(this, "Navigate to Detail", Toast.LENGTH_SHORT).show()
            })
    }

    private fun loading(isLoading: Boolean) {

        mIsRefreshing = isLoading

        refresh_layout.isRefreshing = isLoading
    }

    private fun result(movies: List<MovieModel>) {
        mMoviesAdapter.setList(movies)
    }

    private fun loadMore() {
        veLoadMore.onNext(query)
    }

    private fun get() {
        veGet.onNext(query)
    }

    private fun pressNext() {
        vePressNext.onNext(Unit)
    }
}