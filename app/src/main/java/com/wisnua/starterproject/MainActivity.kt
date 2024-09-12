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
                // If there is internet, refresh from the API
                viewModel.refreshMovies().collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            } else {
                // If there is no internet, check if there is local data
                if (adapter.itemCount == 0) {
                    showOfflineMessage()
                } else {
                    // Show local data if available
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
                // If there is no active search and local data is available, display local data
                if (!isSearching && adapter.itemCount == 0) {
                    adapter.submitData(pagingData)
                    binding.tvLoadState.goGone() // Hide the offline message if local data is present
                }

                Log.d("MainActivity", "Local movies data received: $pagingData")
            }
        }

        adapter.addLoadStateListener { loadState ->
            val isLoading = loadState.source.refresh is LoadState.Loading
            val isNotLoading = loadState.source.refresh is LoadState.NotLoading
            val isError = loadState.source.refresh is LoadState.Error

            if (isLoading) {
                // Start shimmer effect
                binding.shimmerContainer.startShimmer()
                binding.shimmerContainer.goVisible()
                binding.rvAllMovies.goGone()
            } else if (isNotLoading) {
                lifecycleScope.launch {
                    binding.shimmerContainer.stopShimmer()
                    binding.shimmerContainer.goGone()
                    binding.rvAllMovies.goVisible()
                    binding.swipeRefresh.isRefreshing = false
                }
            }

            // Show error message if loading failed
            if (isError) {
                binding.tvLoadState.isVisible = true
                binding.tvLoadState.text = getString(R.string.label_empty_data)
                binding.rvAllMovies.goGone()
                binding.swipeRefresh.isRefreshing = false
            } else {
                binding.tvLoadState.isVisible = false
            }

            // If data is empty, show "No Data" message
            if (isNotLoading && adapter.itemCount == 0) {
                binding.tvLoadState.isVisible = true
                binding.tvLoadState.text = getString(R.string.label_empty_data)
                binding.rvAllMovies.goGone()
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
                .debounce(1000L) // Debounce for 1 second
                .collectLatest { query ->
                    searchText = query?.toString() ?: ""
                    changeIconEtSearch() // Update search icon
                    isSearching = searchText.isNotEmpty() // Set searching mode
                    doSearch() // Trigger search
                }
        }

        // Handle search button click (keyboard search action)
        binding.etSearch.onSearch {
            hideKeyboard()
            searchText = binding.etSearch.text.toString().ifEmpty { "" }
            isSearching = searchText.isNotEmpty() // Set searching mode
            doSearch() // Trigger search on enter
        }

        // Handle clear search action
        binding.etSearch.onClickIconRightEditText {
            if (searchText.isNotEmpty()) {
                searchText = ""
                binding.etSearch.setText("") // Clear text
                isSearching = false // Reset searching mode
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

            adapter.submitData(PagingData.empty())


            viewModel.getMovies(searchText.ifEmpty { "batman" }).collectLatest { pagingData ->
                Log.d("MainActivity", "New paging data received: $pagingData")
                adapter.submitData(pagingData)
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
