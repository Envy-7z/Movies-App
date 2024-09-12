package com.wisnua.starterproject.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.wisnua.starterproject.domain.model.Search
import com.wisnua.starterproject.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val defaultSearchTerm = "batman"

    fun getMovies(query: String? = null): Flow<PagingData<Search>> {
        val searchQuery = query ?: defaultSearchTerm
        return movieRepository.getMovies(searchQuery).cachedIn(viewModelScope)
    }

    fun getLocalMovies(): Flow<PagingData<Search>> {
        return movieRepository.getCachedMovies().cachedIn(viewModelScope)
    }
}
