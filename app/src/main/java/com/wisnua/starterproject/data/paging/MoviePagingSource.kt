package com.wisnua.starterproject.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.wisnua.starterproject.data.local.MovieCache
import com.wisnua.starterproject.data.local.entity.MovieEntity
import com.wisnua.starterproject.data.remote.ApiService
import com.wisnua.starterproject.domain.model.MovieResponse
import retrofit2.HttpException
import java.io.IOException

class MoviePagingSource(
    private val apiService: ApiService,
    private val query: String,
    private val movieCache: MovieCache
) : PagingSource<Int, MovieEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieEntity> {
        val page = params.key ?: 1

        return try {
            val response = apiService.searchMovies(query, page)

            if (response.isSuccessful) {
                val responseBody = response.body()

                if (responseBody?.response == "False") {
                    LoadResult.Error(IOException(responseBody.error ?: "Unknown error"))
                } else {
                    val searchResults = responseBody?.search ?: emptyList()

                    if (searchResults.isNotEmpty()) {
                        val movieResponse = MovieResponse(search = searchResults)
                        movieCache.saveMovies(query, movieResponse)
                    }

                    LoadResult.Page(
                        data = searchResults.map {
                            MovieEntity(
                                imdbID = it?.imdbID ?: "",
                                title = it?.title ?: "",
                                year = it?.year ?: "",
                                type = it?.type ?: "",
                                poster = it?.poster ?: ""
                            )
                        },
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (searchResults.isEmpty()) null else page + 1
                    )
                }
            } else {
                LoadResult.Error(IOException("HTTP error ${response.code()}"))
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}