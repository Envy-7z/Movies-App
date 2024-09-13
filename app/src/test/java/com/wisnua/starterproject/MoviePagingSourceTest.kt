package com.wisnua.starterproject

import androidx.paging.PagingSource
import com.wisnua.starterproject.data.local.MovieCache
import com.wisnua.starterproject.data.local.entity.MovieEntity
import com.wisnua.starterproject.data.paging.MoviePagingSource
import com.wisnua.starterproject.data.remote.ApiService
import com.wisnua.starterproject.domain.model.MovieResponse
import com.wisnua.starterproject.domain.model.Search
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MoviePagingSourceTest {

    private val apiService = mockk<ApiService>()
    private val movieCache = mockk<MovieCache>()
    private val query = "Marvel"
    private val moviePagingSource = MoviePagingSource(apiService, query, movieCache)

    private fun createLoadParams() = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = 1,
        placeholdersEnabled = false
    )

    @Test
    fun `load returns success result when data is available`() = runBlocking {
        // Arrange
        val movieSearchItem = Search(
            imdbID = "1",
            title = "Iron Man",
            year = "2008",
            type = "movie",
            poster = "url"
        )
        val movieResponse = MovieResponse(search = listOf(movieSearchItem))

        coEvery { apiService.searchMovies(query, 1) } returns Response.success(movieResponse)
        coEvery { movieCache.saveMovies(query, movieResponse) } returns Unit

        // Act
        val result = moviePagingSource.load(createLoadParams())

        // Assert
        val expected = PagingSource.LoadResult.Page(
            data = listOf(
                MovieEntity(
                    imdbID = "1",
                    title = "Iron Man",
                    year = "2008",
                    type = "movie",
                    poster = "url"
                )
            ),
            prevKey = null,
            nextKey = 2
        )
        assertEquals(expected, result)
    }

    @Test
    fun `load returns empty result when no data is available`() = runBlocking {
        // Arrange
        coEvery {
            apiService.searchMovies(
                query,
                1
            )
        } returns Response.success(MovieResponse(search = emptyList()))
        coEvery { movieCache.saveMovies(query, any()) } returns Unit

        // Act
        val result = moviePagingSource.load(createLoadParams())

        // Assert
        val expected = PagingSource.LoadResult.Page(
            data = emptyList(),
            prevKey = null,
            nextKey = null
        )
        assertEquals(expected, result)
    }

    @Test
    fun `load returns error result when exception is thrown`() = runBlocking {
        // Arrange
        coEvery { apiService.searchMovies(query, 1) } throws IOException()
        coEvery { movieCache.saveMovies(query, any()) } returns Unit

        // Act
        val result = moviePagingSource.load(createLoadParams())

        // Assert
        assertTrue(result is PagingSource.LoadResult.Error)
        val errorResult = result as PagingSource.LoadResult.Error
        assertTrue(errorResult.throwable is IOException)
    }

    @Test
    fun `load returns error result when API indicates an error in response body`() = runBlocking {
        // Arrange
        val errorMessage = "Movie not found!"
        val responseBody = MovieResponse(response = "False", error = errorMessage)
        val responseError = Response.success(responseBody)

        coEvery { apiService.searchMovies(query, 1) } returns responseError
        coEvery { movieCache.saveMovies(query, any()) } returns Unit

        // Act
        val result = moviePagingSource.load(createLoadParams())

        // Assert
        assertTrue(result is PagingSource.LoadResult.Error, "Expected LoadResult.Error but got $result")
        val errorResult = result as PagingSource.LoadResult.Error
        assertTrue(errorResult.throwable is IOException, "Expected IOException but got ${errorResult.throwable::class.simpleName}")

        // Check if the message in the IOException matches the expected error message
        assertEquals(errorMessage, errorResult.throwable.message, errorResult.throwable.message)
    }



}
