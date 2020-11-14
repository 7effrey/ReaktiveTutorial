package com.jeffrey.reaktive.tutorial.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.adrena.core.data.cache.DatabaseHelper
import com.jeffrey.core.presentation.DefaultTestViewModel
import com.jeffrey.core.presentation.TestViewModel
import com.adrena.core.sql.MoviesDatabase
import com.jeffrey.reaktive.tutorial.presentation.mapper.MovieModelsMapper
import com.jeffrey.reaktive.tutorial.presentation.adapter.MoviesAdapter
import com.jeffrey.reaktive.tutorial.R
import com.jeffrey.reaktive.tutorial.presentation.model.MovieModel
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.mainScheduler
import com.facebook.stetho.Stetho
import com.jeffrey.core.data.network.HttpClient
import com.jeffrey.core.data.network.KtorHttpClient
import com.jeffrey.core.data.provider.DefaultOmdbProvider
import com.jeffrey.core.data.provider.OmdbProvider
import com.jeffrey.core.data.repository.DefaultMovieRepository
import com.jeffrey.core.data.repository.MovieRepository
import com.jeffrey.core.data.storage.DefaultStorageService
import com.jeffrey.core.data.storage.StorageService
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.android.synthetic.main.activity_main.*

class TestActivity : AppCompatActivity() {

    private lateinit var mMoviesAdapter: MoviesAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRefreshLayout: SwipeRefreshLayout

    private var mIsRefreshing = false

    private var query = "secret life"

    // This should be singleton
    private val dbHelper: DatabaseHelper by lazy {
        val driver = AndroidSqliteDriver(
            schema = MoviesDatabase.Schema,
            context = this,
            name = "movie.db")
        DatabaseHelper("movie.db", driver)
    }

    private val mViewModel: TestViewModel<MovieModel> by lazy {

//        val cache = MovieSqlCache(dbHelper)

        val httpClient: HttpClient = KtorHttpClient("https://www.omdbapi.com/")

        val provider: OmdbProvider = DefaultOmdbProvider(httpClient, "b445ca0b")
        val storageService: StorageService = DefaultStorageService()

        val repository: MovieRepository = DefaultMovieRepository(provider, storageService)

        val modelMapper = MovieModelsMapper()

        return@lazy DefaultTestViewModel(repository, modelMapper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding()

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

    private fun binding() {
        disposables = CompositeDisposable()

        disposables?.add(mViewModel.set())

        val viewData = mViewModel.generateViewData()
        disposables?.add(viewData.loading.observeOn(mainScheduler).subscribe(true, onNext = ::loading))
        disposables?.add(viewData.result.observeOn(mainScheduler).subscribe(true, onNext = ::result))
        disposables?.add(mViewModel.output.observeOn(mainScheduler)
            .filter { it is TestViewModel.Output.NavigateToDetail }
            .subscribe(true) {
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
        mViewModel.viewEvent.loadMore(query)
    }

    private fun get() {
        mViewModel.viewEvent.get(query)
    }

    private fun pressNext() {
        mViewModel.viewEvent.pressNext()
    }
}
