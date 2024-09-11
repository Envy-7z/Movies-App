package com.wisnua.starterproject

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
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
import com.wisnua.starterproject.utils.goGone
import com.wisnua.starterproject.utils.goVisible
import com.wisnua.starterproject.utils.onClickIconRightEditText
import com.wisnua.starterproject.utils.onSearch
import com.wisnua.starterproject.utils.setupDrawableRightEditText
import com.wisnua.starterproject.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MovieViewModel by viewModels()
    private lateinit var adapter: MovieAdapter

    private var searchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
        binding.swipeRefresh.setOnRefreshListener(this)
        setupEtSearch()
    }

    override fun onRefresh() {
        // Refresh the data
        adapter.refresh()
        binding.shimmerContainer.startShimmer()
        binding.shimmerContainer.goVisible()
        binding.rvAllMovies.goGone()
    }

    private fun setupRecyclerView() {
        adapter = MovieAdapter()
        binding.rvAllMovies.layoutManager = LinearLayoutManager(this)
        binding.rvAllMovies.adapter = adapter.withLoadStateFooter(MovieLoadStateAdapter { adapter.retry() })
    }

    private fun observeViewModel() {
        // Launch a coroutine to observe data from the ViewModel
        lifecycleScope.launch {
            viewModel.getMovies(searchText).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        // Add LoadStateListener to observe loading states
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

    private fun setupEtSearch() {
        binding.etSearch.clearFocus()

        lifecycleScope.launch {
            binding.etSearch.textChanges()
                .debounce(1000L) // Debounce for 1 second
                .collectLatest { query ->
                    searchText = query?.toString() ?: ""
                    changeIconEtSearch() // Update search icon
                    if (searchText.isNotEmpty()) {
                        doSearch() // Trigger search
                    }
                }
        }

        // Handle search button click (keyboard search action)
        binding.etSearch.onSearch {
            hideKeyboard()
            searchText = binding.etSearch.text.toString().ifEmpty { "" }
            doSearch() // Trigger search on enter
        }

        // Handle clear search action
        binding.etSearch.onClickIconRightEditText {
            if (searchText.isNotEmpty()) {
                searchText = ""
                binding.etSearch.setText("") // Clear text
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

            viewModel.getMovies(searchText).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

}
