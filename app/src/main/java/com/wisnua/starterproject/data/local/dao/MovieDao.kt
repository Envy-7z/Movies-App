package com.wisnua.starterproject.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wisnua.starterproject.data.local.entity.MovieEntity

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%'")
    fun searchMovies(query: String): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies")
    fun getAllMovies(): PagingSource<Int, MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies WHERE title LIKE '%' || :query || '%'")
    suspend fun deleteMoviesByQuery(query: String)
}
