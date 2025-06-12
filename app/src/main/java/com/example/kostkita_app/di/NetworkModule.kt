package com.example.kostkita_app.di

import android.content.Context
import android.util.Log
import com.example.kostkita_app.data.remote.api.AuthApiService
import com.example.kostkita_app.data.remote.api.KostKitaApiService
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
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("auth_token", null)

            Log.d("NetworkModule", "Request URL: ${request.url}")
            Log.d("NetworkModule", "Token from prefs: $token") // Debug token
            Log.d("NetworkModule", "Token available: ${!token.isNullOrEmpty()}")

            val newRequest = if (token != null) {
                Log.d("NetworkModule", "Adding Authorization header")
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()
            } else {
                Log.w("NetworkModule", "No token found, sending request without auth")
                request.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
            }

            val response = chain.proceed(newRequest)
            Log.d("NetworkModule", "Response code: ${response.code}")

            if (!response.isSuccessful) {
                Log.e("NetworkModule", "HTTP Error: ${response.code} - ${response.message}")
            }

            response
        }
    }

    @Provides
    @Singleton
    @Named("AuthOkHttpClient")
    fun provideAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("ApiOkHttpClient")
    fun provideApiOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(@Named("ApiOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(@Named("AuthOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideKostKitaApiService(retrofit: Retrofit): KostKitaApiService {
        return retrofit.create(KostKitaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("AuthRetrofit") retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}