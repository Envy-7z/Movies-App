package com.wisnua.starterproject.domain.repository

import com.wisnua.starterproject.data.remote.ApiService
import com.wisnua.starterproject.domain.model.MovieResponse
import retrofit2.Response

class TestingRepository(private val movieApiService: ApiService) {
    suspend fun searchMovies(searchQuery: String, page: Int): Response<MovieResponse> {
        return movieApiService.searchMovies(searchQuery, page)
    }
}