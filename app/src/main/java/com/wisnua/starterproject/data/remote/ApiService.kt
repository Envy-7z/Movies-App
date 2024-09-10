package com.wisnua.starterproject.data.remote

import com.wisnua.starterproject.domain.model.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/")
    suspend fun searchMovies(
        @Query("s") searchQuery: String,
        @Query("page") page: Int,
    ): Response<MovieResponse>

}
