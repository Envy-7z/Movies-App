package com.wisnua.starterproject.data.local

import com.wisnua.starterproject.data.local.dao.MovieDao
import com.wisnua.starterproject.data.local.entity.MovieEntity
import com.wisnua.starterproject.domain.model.MovieResponse

class MovieCache(private val movieDao: MovieDao) {

    /**
     * Save movies to the cache.
     * @param query The query used for fetching the movies.
     * @param movies The movie response containing the movies to cache.
     */
    suspend fun saveMovies(query: String, movies: MovieResponse) {
        // Convert from MovieResponse to MovieEntity
        val movieEntities = movies.search?.map {
            MovieEntity(
                imdbID = it?.imdbID ?: "",
                title = it?.title ?: "",
                year = it?.year ?: "",
                type = it?.type ?: "",
                poster = it?.poster ?: ""
            )
        } ?: emptyList()

        // Delete old cache for the given query and insert new data
        movieDao.deleteMoviesByQuery(query)
        movieDao.insertMovies(movieEntities)
    }

    /**
     * Get cached movies based on the query.
     * @param query The query used for fetching the cached movies.
     * @return A PagingSource that provides the cached movies.
     */
    fun getMoviesPagingSource(query: String) = movieDao.searchMovies(query)
}
