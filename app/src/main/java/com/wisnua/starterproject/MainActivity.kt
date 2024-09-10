package com.wisnua.starterproject

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wisnua.starterproject.databinding.ActivityMainBinding
import com.wisnua.starterproject.presentation.adapter.MovieAdapter
import com.wisnua.starterproject.presentation.viewModel.MovieViewModel
import com.wisnua.starterproject.utils.MovieLoadStateAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MovieViewModel by viewModels()
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
        binding.swipeRefresh.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        adapter.refresh()
    }

    private fun setupRecyclerView() {
        adapter = MovieAdapter()
        binding.rvAllMovies.layoutManager = LinearLayoutManager(this)
        binding.rvAllMovies.adapter = adapter.withLoadStateFooter(MovieLoadStateAdapter { adapter.retry() })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.getMovies("Batman").collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        adapter.addLoadStateListener { loadState ->
            val isLoading = loadState.source.refresh is LoadState.Loading
            val isError = loadState.source.refresh is LoadState.Error
            val isEmpty = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0

            binding.swipeRefresh.isRefreshing = isLoading
            binding.tvLoadState.isVisible = isError || isEmpty
            binding.rvAllMovies.isVisible = !isError && !isEmpty
        }
    }
}
