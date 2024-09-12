package com.wisnua.starterproject.data.local

import com.wisnua.starterproject.data.local.dao.MovieDao
import com.wisnua.starterproject.data.local.entity.MovieEntity
import com.wisnua.starterproject.domain.model.MovieResponse

class MovieCache(private val movieDao: MovieDao) {

    suspend fun saveMovies(query: String, movies: MovieResponse) {
        val movieEntities = movies.search?.map {
            MovieEntity(
                imdbID = it?.imdbID ?: "",
                title = it?.title ?: "",
                year = it?.year ?: "",
                type = it?.type ?: "",
                poster = it?.poster ?: ""
            )
        } ?: emptyList()

        movieDao.deleteMoviesByQuery(query)
        movieDao.insertMovies(movieEntities)
    }

    // get data cache
    fun getMoviesPagingSource(query: String) = movieDao.searchMovies(query)

    // get all movies from cache
    fun getAllMoviesPagingSource() = movieDao.getAllMovies()
}
