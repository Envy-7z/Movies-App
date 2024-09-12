package com.wisnua.starterproject.domain.repository

import androidx.paging.PagingData
import com.wisnua.starterproject.domain.model.Search
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getMovies(query: String): Flow<PagingData<Search>>
    fun getCachedMovies(): Flow<PagingData<Search>>
}

