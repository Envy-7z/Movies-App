package com.wisnua.starterproject.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wisnua.starterproject.MyApplication
import com.wisnua.starterproject.data.local.dao.MovieDao
import com.wisnua.starterproject.data.local.entity.MovieEntity

@Database(entities = [MovieEntity::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: MyApplication): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


