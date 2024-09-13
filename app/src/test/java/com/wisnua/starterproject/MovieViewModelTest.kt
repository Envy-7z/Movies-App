package com.wisnua.starterproject

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.wisnua.starterproject.domain.model.Search
import com.wisnua.starterproject.domain.repository.MovieRepository
import com.wisnua.starterproject.presentation.viewModel.MovieViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


@ExperimentalCoroutinesApi
class MovieViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var movieRepository: MovieRepository

    @InjectMocks
    private lateinit var movieViewModel: MovieViewModel

    private val testPagingData: PagingData<Search> = PagingData.from(
        listOf(
            Search("1", "Superman", "2006", "movie"),
            Search("2", "Batman", "2008", "movie")
        )
    )
    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `getMovies should return data from repository when query is provided`() = runBlockingTest {
        val query = "superman"
        whenever(movieRepository.getMovies(query)).thenReturn(flowOf(testPagingData))

        val result: Flow<PagingData<Search>> = movieViewModel.getMovies(query)

        verify(movieRepository).getMovies(query)

        val resultSnapshot = result.cachedIn(movieViewModel.viewModelScope).first()

        // Snapshot assertion
        val expectedSnapshot = listOf(
            Search("1", "Superman", "2006", "movie"),
            Search("2", "Batman", "2008", "movie")
        )

        assertEquals(expectedSnapshot, resultSnapshot) // Convert PagingData to list and compare
    }

    @Test
    fun `getMovies should return data from repository with default search term when no query is provided`() = runBlockingTest {
        whenever(movieRepository.getMovies("batman")).thenReturn(flowOf(testPagingData))

        val result: Flow<PagingData<Search>> = movieViewModel.getMovies()

        verify(movieRepository).getMovies("batman")
        result.cachedIn(movieViewModel.viewModelScope)
        // Tambahkan assertions atau verifikasi lain yang diperlukan
    }

    @Test
    fun `getLocalMovies should return cached movies from repository`() = runBlockingTest {
        whenever(movieRepository.getCachedMovies()).thenReturn(flowOf(testPagingData))

        val result: Flow<PagingData<Search>> = movieViewModel.getLocalMovies()

        verify(movieRepository).getCachedMovies()
        result.cachedIn(movieViewModel.viewModelScope)
        // Tambahkan assertions atau verifikasi lain yang diperlukan
    }

    @Test
    fun `refreshMovies should return refreshed data from repository`() = runBlockingTest {
        val query = "superman"
        whenever(movieRepository.getMovies(query)).thenReturn(flowOf(testPagingData))

        val result: Flow<PagingData<Search>> = movieViewModel.refreshMovies(query)

        verify(movieRepository).getMovies(query)
        result.cachedIn(movieViewModel.viewModelScope)
        // Tambahkan assertions atau verifikasi lain yang diperlukan
    }

    @Test
    fun `refreshMovies should use default search term when no query is provided`() = runBlockingTest {
        whenever(movieRepository.getMovies("batman")).thenReturn(flowOf(testPagingData))

        val result: Flow<PagingData<Search>> = movieViewModel.refreshMovies()

        verify(movieRepository).getMovies("batman")
        result.cachedIn(movieViewModel.viewModelScope)
        // Tambahkan assertions atau verifikasi lain yang diperlukan
    }
}