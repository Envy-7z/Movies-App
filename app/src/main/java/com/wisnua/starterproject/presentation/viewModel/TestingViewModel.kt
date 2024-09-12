package com.wisnua.starterproject.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisnua.starterproject.domain.model.MovieResponse
import com.wisnua.starterproject.domain.repository.TestingRepository
import kotlinx.coroutines.launch

// MovieViewModel.kt
class TestingViewModel(private val repository: TestingRepository) : ViewModel() {

    private val _movieResponse = MutableLiveData<MovieResponse>()
    val movieResponse: LiveData<MovieResponse> get() = _movieResponse

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun searchMovies(searchQuery: String, page: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.searchMovies(searchQuery, page)
                if (response.isSuccessful) {
                    _movieResponse.value = response.body()
                } else {
                    _error.value = "Error: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Exception: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
