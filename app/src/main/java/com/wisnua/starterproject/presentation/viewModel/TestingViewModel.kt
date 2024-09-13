package com.wisnua.starterproject.presentation.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisnua.starterproject.domain.model.MovieResponse
import com.wisnua.starterproject.domain.repository.TestingRepository
import kotlinx.coroutines.launch

class TestingViewModel(private val repository: TestingRepository) : ViewModel() {

    val movieResponse = MutableLiveData<MovieResponse>()
    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun searchMovies(query: String, page: Int) {
        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.searchMovies(query, page)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.response == "True") {
                        movieResponse.value = body
                    } else {
                        error.value = body?.error ?: "Unknown error"
                    }
                } else {
                    error.value = response.message()
                }
            } catch (e: Exception) {
                error.value = "Exception: ${e.message}"
            } finally {
                loading.value = false
            }
        }
    }
}

