package com.wisnua.starterproject

import com.wisnua.starterproject.data.local.MovieCache
import com.wisnua.starterproject.data.remote.ApiService
import com.wisnua.starterproject.data.repository.MovieRepositoryImpl
import com.wisnua.starterproject.domain.model.MovieResponse
import com.wisnua.starterproject.domain.model.Search
import com.wisnua.starterproject.domain.repository.MovieRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import retrofit2.Response

class MovieRepositoryImplTest {

    private lateinit var movieRepository: MovieRepository
    private lateinit var apiService: ApiService
    private lateinit var movieCache: MovieCache

    @Before
    fun setUp() {
        apiService = Mockito.mock(ApiService::class.java)
        movieCache = Mockito.mock(MovieCache::class.java)
        movieRepository = MovieRepositoryImpl(apiService, movieCache)
    }

    @Test
    fun `searchMovies should return MovieResponse`() = runBlocking {
        // Arrange
        val mockQuery = "Batman"
        val mockSearchList = listOf(
            Search("tt0372784", "Batman Begins", "movie", "2005", "https://image.url/poster1.jpg"),
            Search("tt1877830", "The Batman", "movie", "2022", "https://image.url/poster2.jpg")
        )

        val mockMovieResponse = MovieResponse("True", mockSearchList, "2")
        val mockResponse = Response.success(mockMovieResponse)

        // Mock the API response
        whenever(apiService.searchMovies(mockQuery, 1)).thenReturn(mockResponse)

        // Act
        val result = apiService.searchMovies(mockQuery, 1)

        // Assert
        assert(result.isSuccessful)
        assert(result.body()?.search == mockSearchList)
    }
}

