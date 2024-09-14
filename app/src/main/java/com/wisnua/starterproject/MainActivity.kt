package com.wisnua.starterproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wisnua.starterproject.databinding.ActivityMainBinding
import com.wisnua.starterproject.presentation.adapter.MovieAdapter
import com.wisnua.starterproject.presentation.viewModel.MovieViewModel
import com.wisnua.starterproject.data.paging.MovieLoadStateAdapter
import com.wisnua.starterproject.utils.goGone
import com.wisnua.starterproject.utils.goVisible
import com.wisnua.starterproject.utils.isNetworkAvailable
import com.wisnua.starterproject.utils.onClickIconRightEditText
import com.wisnua.starterproject.utils.onSearch
import com.wisnua.starterproject.utils.setupDrawableRightEditText
import com.wisnua.starterproject.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MovieViewModel by viewModels()
    private lateinit var adapter: MovieAdapter

    private var searchText = ""
    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeLocalMovies() // Observe local data when the app starts
        binding.swipeRefresh.setOnRefreshListener(this)
        setupEtSearch()
        if (isNetworkAvailable(this@MainActivity)){
            fetchDefaultMovies() // Start with default search
        }
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            if (isNetworkAvailable(this@MainActivity)) {
                // If there's internet, refresh data from API
                viewModel.refreshMovies().collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            } else {
                // If there's no internet, check for local data
                if (adapter.itemCount == 0) {
                    showOfflineMessage()
                } else {
                    // Display local data if available
                    viewModel.getLocalMovies().collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        adapter = MovieAdapter()
        binding.rvAllMovies.layoutManager = LinearLayoutManager(this)
        binding.rvAllMovies.adapter = adapter.withLoadStateFooter(MovieLoadStateAdapter { adapter.retry() })
    }

    private fun observeLocalMovies() {
        lifecycleScope.launch {
            viewModel.getLocalMovies().collectLatest { pagingData ->
                // Check if a search is ongoing
                if (!isSearching) {
                    adapter.submitData(pagingData)
                }

                // Previous logic remains, to handle offline state
                if (!isNetworkAvailable(this@MainActivity)) {
                    if (adapter.itemCount == 0 && !isSearching) {
                        showOfflineMessage()
                    } else if (adapter.itemCount > 0) {
                        binding.tvLoadState.goGone()
                    }
                } else {
                    binding.tvLoadState.goGone()
                }

                Log.d("MainActivity", "Local movies data received: $pagingData")
            }
        }

        adapter.addLoadStateListener { loadState ->
            // Same as before
            val isLoading = loadState.source.refresh is LoadState.Loading
            val isNotLoading = loadState.source.refresh is LoadState.NotLoading
            val isError = loadState.source.refresh is LoadState.Error

            if (isLoading) {
                binding.shimmerContainer.startShimmer()
                binding.shimmerContainer.goVisible()
                binding.rvAllMovies.goGone()
            } else {
                binding.shimmerContainer.stopShimmer()
                binding.shimmerContainer.goGone()

                if (isNotLoading) {
                    binding.rvAllMovies.goVisible()
                    binding.swipeRefresh.isRefreshing = false
                }

                if (isError) {
                    binding.tvLoadState.isVisible = true
                    binding.tvLoadState.text = getString(R.string.label_error_data)
                    binding.rvAllMovies.goGone()
                    binding.swipeRefresh.isRefreshing = false
                } else {
                    if (adapter.itemCount == 0) {
                        binding.tvLoadState.isVisible = true
                        binding.tvLoadState.text = getString(R.string.label_empty_data)
                        binding.rvAllMovies.goGone()
                    } else {
                        binding.tvLoadState.isVisible = false
                    }
                }
            }
        }
    }

    private fun fetchDefaultMovies() {
        doSearch()
    }

    private fun setupEtSearch() {
        binding.etSearch.clearFocus()

        lifecycleScope.launch {
            binding.etSearch.textChanges()
                .debounce(1000L)
                .collectLatest { query ->
                    searchText = query?.toString() ?: ""
                    changeIconEtSearch()
                    isSearching = searchText.isNotEmpty()
                    doSearch() // Perform search based on query
                }
        }

        // Handle when the user presses the search button on the keyboard
        binding.etSearch.onSearch {
            hideKeyboard()
            searchText = binding.etSearch.text.toString().ifEmpty { "" }
            isSearching = searchText.isNotEmpty()
            doSearch() // Perform the search
        }

        // Handle clear search
        binding.etSearch.onClickIconRightEditText {
            if (searchText.isNotEmpty()) {
                searchText = ""
                binding.etSearch.setText("") // Clear text
                isSearching = false
                doSearch() // Clear search results
            }
        }
    }

    private fun changeIconEtSearch() {
        val icon = if (binding.etSearch.text.toString().isEmpty()) {
            R.drawable.ic_search
        } else {
            R.drawable.ic_close_black_24dp
        }
        binding.etSearch.setupDrawableRightEditText(icon)
    }

    private fun hideKeyboard() {
        currentFocus?.let { view ->
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun doSearch() {
        lifecycleScope.launch {
            binding.shimmerContainer.startShimmer()
            binding.shimmerContainer.goVisible()
            binding.rvAllMovies.goGone()

            adapter.submitData(PagingData.empty()) // Clear adapter when a new search is initiated

            viewModel.getMovies(searchText.ifEmpty { "batman" }).collectLatest { pagingData ->
                Log.d("MainActivity", "New paging data received: $pagingData")
                binding.shimmerContainer.stopShimmer()
                binding.shimmerContainer.goGone()
                adapter.submitData(pagingData)

                // Display search results
                binding.rvAllMovies.goVisible()
            }
        }
    }

    private fun showOfflineMessage() {
        binding.tvLoadState.goVisible()
        binding.tvLoadState.text = getString(R.string.error_no_internet)
        binding.rvAllMovies.goGone()
        binding.shimmerContainer.goGone()
        binding.swipeRefresh.isRefreshing = false
    }
}
