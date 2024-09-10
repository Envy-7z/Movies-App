package com.wisnua.starterproject.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.wisnua.starterproject.data.local.MovieCache
import com.wisnua.starterproject.data.remote.ApiService
import com.wisnua.starterproject.domain.model.Search
import retrofit2.HttpException
import java.io.IOException

class MoviePagingSource(
    private val apiService: ApiService,
    private val query: String,
    private val movieCache: MovieCache
) : PagingSource<Int, Search>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Search> {
        val page = params.key ?: 1
        return try {
            val response = apiService.searchMovies(query, page)
            val searchResults = response.body()?.search ?: emptyList()

            // Save to cache
            movieCache.saveMovies(query, response.body()!!)

            LoadResult.Page(
                data = searchResults.filterNotNull(),
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (searchResults.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Search>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
