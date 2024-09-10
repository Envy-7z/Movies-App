package com.wisnua.starterproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wisnua.starterproject.domain.model.Search

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val imdbID: String,
    val poster: String?,
    val title: String?,
    val type: String?,
    val year: String?
) {
    fun toSearch(): Search {
        return Search(
            imdbID = imdbID,
            poster = poster,
            title = title,
            type = type,
            year = year
        )
    }

    companion object {
        fun fromSearch(search: Search): MovieEntity {
            return MovieEntity(
                imdbID = search.imdbID ?: "",
                poster = search.poster,
                title = search.title,
                type = search.type,
                year = search.year
            )
        }
    }
}
