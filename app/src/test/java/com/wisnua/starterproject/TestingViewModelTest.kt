package com.wisnua.starterproject

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.wisnua.starterproject.domain.model.MovieResponse
import com.wisnua.starterproject.domain.model.Search
import com.wisnua.starterproject.domain.repository.TestingRepository
import com.wisnua.starterproject.presentation.viewModel.TestingViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.Response

@ExperimentalCoroutinesApi
class TestingViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: TestingRepository

    private lateinit var viewModel: TestingViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = TestingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchMovies should update movieResponse on success with a list of movies`() = runTest {
        // Given
        val mockMovies = listOf(
            Search("Batman Begins", "2005", "tt0372784", "movie", "https://m.media-amazon.com/images/M/MV5BOTY4YjI2N2MtYmFlMC00ZjcyLTg3YjEtMDQyM2ZjYzQ5YWFkXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_SX300.jpg"),
            Search("The Batman", "2022", "tt1877830", "movie", "https://m.media-amazon.com/images/M/MV5BOGE2NWUwMDItMjA4Yi00N2Y3LWJjMzEtMDJjZTMzZTdlZGE5XkEyXkFqcGdeQXVyODk4OTc3MTY@._V1_SX300.jpg"),
            Search("Batman v Superman: Dawn of Justice", "2016", "tt2975590", "movie", "https://m.media-amazon.com/images/M/MV5BYThjYzcyYzItNTVjNy00NDk0LTgwMWQtYjMwNmNlNWJhMzMyXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_SX300.jpg")
        )
        val mockResponse = MovieResponse(
            search = mockMovies,
            totalResults = "574",
            response = "True"
        )
        val response = Response.success(mockResponse)
        `when`(repository.searchMovies("batman", 1)).thenReturn(response)

        // Observe LiveData
        val movieResponseObserver = Observer<MovieResponse> {}
        val loadingObserver = Observer<Boolean> {}
        viewModel.movieResponse.observeForever(movieResponseObserver)
        viewModel.loading.observeForever(loadingObserver)

        // When
        viewModel.searchMovies("batman", 1)

        // Ensure coroutines are complete
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.loading.value) // Loading should be false after completion
        assertEquals(mockResponse, viewModel.movieResponse.value)
    }

    @Test
    fun `searchMovies should update error on false response with an error message`() = runTest {
        // Given
        val errorMessage = "Movie not found!"
        val prefixedErrorMessage = errorMessage
        val mockResponse = MovieResponse(
            search = emptyList(),
            totalResults = "0",
            response = "False",
            error = errorMessage
        )
        val response = Response.success(mockResponse)
        `when`(repository.searchMovies("batman", 1)).thenReturn(response)

        // Observe LiveData
        val errorObserver = Observer<String> {}
        val loadingObserver = Observer<Boolean> {}
        viewModel.error.observeForever(errorObserver)
        viewModel.loading.observeForever(loadingObserver)

        // When
        viewModel.searchMovies("batman", 1)

        // Ensure coroutines are complete
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.loading.value) // Loading should be false after completion
        assertEquals(prefixedErrorMessage, viewModel.error.value) // Match the actual error message
    }

    @Test
    fun `searchMovies should update error on exception`() = runTest {
        // Given
        val exceptionMessage = "Exception: Network error"
        `when`(repository.searchMovies("batman", 1)).thenThrow(RuntimeException("Network error"))

        // Observe LiveData
        val errorObserver = Observer<String> {}
        val loadingObserver = Observer<Boolean> {}
        viewModel.error.observeForever(errorObserver)
        viewModel.loading.observeForever(loadingObserver)

        // When
        viewModel.searchMovies("batman", 1)

        // Ensure coroutines are complete
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.loading.value) // Loading should be false after completion
        assertEquals(exceptionMessage, viewModel.error.value)
    }
}
