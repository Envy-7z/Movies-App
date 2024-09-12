package com.wisnua.starterproject

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.wisnua.starterproject.domain.model.MovieResponse
import com.wisnua.starterproject.domain.model.Search
import com.wisnua.starterproject.domain.repository.TestingRepository
import com.wisnua.starterproject.presentation.viewModel.TestingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

@ExperimentalCoroutinesApi
class MovieViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var movieRepository: TestingRepository
    private lateinit var movieViewModel: TestingViewModel
    // Use TestDispatcher for testing coroutine dispatching
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)  // Set test dispatcher for Dispatchers.Main
        movieRepository = Mockito.mock(TestingRepository::class.java)
        movieViewModel = TestingViewModel(movieRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()  // Reset Dispatchers.Main after the test

    }
    @Test
    fun `searchMovies should update LiveData with result`() = runBlockingTest {
        // Arrange
        val mockMovies = listOf(
            Search("tt0372784", "https://m.media-amazon.com/images/M/MV5BOTY4YjI2N2MtYmFlMC00ZjcyLTg3YjEtMDQyM2ZjYzQ5YWFkXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_SX300.jpg", "Batman Begins", "movie", "2005"),
            Search("tt1877830", "https://m.media-amazon.com/images/M/MV5BOGE2NWUwMDItMjA4Yi00N2Y3LWJjMzEtMDJjZTMzZTdlZGE5XkEyXkFqcGdeQXVyODk4OTc3MTY@._V1_SX300.jpg", "The Batman", "movie", "2022")
        )
        val mockResponse = MovieResponse("True", mockMovies, "2")

        // Wrap mockResponse in a Response object
        val response = Response.success(mockResponse)

        // Mock the repository method to return a Response<MovieResponse>
        whenever(movieRepository.searchMovies("query", 1)).thenReturn(response)

        val observer = Mockito.mock(Observer::class.java) as Observer<MovieResponse>
        movieViewModel.movieResponse.observeForever(observer)

        // Act
        movieViewModel.searchMovies("query", 1)

        // Assert
        verify(observer).onChanged(mockResponse)
    }

    @Test
    fun `searchMovies should handle error`() = runBlockingTest {
        // Arrange
        val mockError = "Error"
        whenever(movieRepository.searchMovies("query", 1)).thenThrow(RuntimeException(mockError))

        val errorObserver = Mockito.mock(Observer::class.java) as Observer<String>
        movieViewModel.error.observeForever(errorObserver)

        // Act
        movieViewModel.searchMovies("query", 1)

        // Assert
        verify(errorObserver).onChanged("Exception: $mockError")
    }

    @Test
    fun `searchMovies should show loading state`() = runBlockingTest {
        // Arrange
        val movieResponse = MovieResponse("True", listOf(), "0")

        // Return a Response<MovieResponse> instead of MovieResponse
        val response = Response.success(movieResponse)

        whenever(movieRepository.searchMovies("query", 1)).thenReturn(response)

        val loadingObserver = Mockito.mock(Observer::class.java) as Observer<Boolean>
        movieViewModel.loading.observeForever(loadingObserver)

        // Act
        movieViewModel.searchMovies("query", 1)

        // Assert
        verify(loadingObserver).onChanged(true)  // Check loading started
        verify(loadingObserver).onChanged(false) // Check loading finished
    }
}
