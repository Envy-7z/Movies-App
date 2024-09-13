package com.wisnua.starterproject.di.modules

import android.content.Context
import androidx.room.Room
import com.wisnua.starterproject.data.local.MovieCache
import com.wisnua.starterproject.data.local.MovieDatabase
import com.wisnua.starterproject.data.local.dao.MovieDao
import com.wisnua.starterproject.data.remote.ApiService
import com.wisnua.starterproject.data.repository.MovieRepositoryImpl
import com.wisnua.starterproject.domain.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://www.omdbapi.com/"
    private const val API_KEY = "6038cc6a"
    private const val DATABASE_NAME = "movie_database"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val apiKeyInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val originalHttpUrl = originalRequest.url

            val url = originalHttpUrl.newBuilder()
                .addQueryParameter("apikey", API_KEY)
                .build()

            val requestBuilder = originalRequest.newBuilder()
                .url(url)

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMovieDatabase(@ApplicationContext context: Context): MovieDatabase {
        return Room.databaseBuilder(
            context,
            MovieDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMovieDao(movieDatabase: MovieDatabase): MovieDao {
        return movieDatabase.movieDao()
    }

    @Provides
    @Singleton
    fun provideMovieCache(movieDao: MovieDao): MovieCache {
        return MovieCache(movieDao)
    }

    @Provides
    @Singleton
    fun provideMovieRepository(apiService: ApiService, movieCache: MovieCache): MovieRepository {
        return MovieRepositoryImpl(apiService, movieCache)
    }
}
