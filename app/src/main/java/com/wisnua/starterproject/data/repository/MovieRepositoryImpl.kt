package com.wisnua.starterproject.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.wisnua.starterproject.data.local.MovieCache
import com.wisnua.starterproject.data.local.entity.MovieEntity
import com.wisnua.starterproject.data.remote.ApiService
import com.wisnua.starterproject.domain.model.Search
import com.wisnua.starterproject.domain.repository.MovieRepository
import com.wisnua.starterproject.data.paging.MoviePagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val movieCache: MovieCache
) : MovieRepository {

    override fun getMovies(query: String): Flow<PagingData<Search>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                MoviePagingSource(apiService, query, movieCache)
            }
        ).flow.map { pagingData ->
            pagingData.map { movieEntity ->
                movieEntity.toSearch() // Convert MovieEntity to Search
            }
        }
    }

    override fun getCachedMovies(): Flow<PagingData<Search>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                movieCache.getAllMoviesPagingSource() // Fetch all local data
            }
        ).flow.map { pagingData ->
            pagingData.map { movieEntity ->
                movieEntity.toSearch() // Convert MovieEntity to Search
            }
        }
    }
}
