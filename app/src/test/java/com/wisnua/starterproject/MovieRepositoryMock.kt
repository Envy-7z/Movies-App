package com.wisnua.starterproject

import com.wisnua.starterproject.domain.model.Search
import com.wisnua.starterproject.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import androidx.paging.PagingData
import javax.inject.Inject

class MovieRepositoryMock @Inject constructor() : MovieRepository {
    override fun getMovies(query: String): Flow<PagingData<Search>> {
        val movieList = listOf(
            Search("tt0372784", "poster_url", "Batman Begins", "movie", "2005")
        )
        return flowOf(PagingData.from(movieList))
    }

    override fun getCachedMovies(): Flow<PagingData<Search>> {
        val movieList = listOf(
            Search("tt0372784", "poster_url", "Batman Begins", "movie", "2005")
        )
        return flowOf(PagingData.from(movieList))
    }
}
