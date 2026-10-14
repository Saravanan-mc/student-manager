package com.example.data.remote

import com.example.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface SheetsApiService {

    @GET("exec")
    suspend fun getStudents(
        @Query("action") action: String = "getStudents"
    ): Response<List<Student>>

    @GET("exec")
    suspend fun getPoints(
        @Query("action") action: String = "getPoints",
        @Query("id") id: String
    ): Response<List<PointResponse>>

    @POST("exec")
    suspend fun registerStudent(
        @Body request: RegisterRequest
    ): Response<ResponseBodyWrapper>

    @POST("exec")
    suspend fun addPoints(
        @Body request: AddPointsRequest
    ): Response<ResponseBodyWrapper>

    companion object {
        private const val BASE_URL = "https://script.google.com/macros/s/AKfycbyjxXUxJi6Bm5YdENxNXEoqrNerTOW_ONLq2jI2qcyZbaVzw9gRBhRfy_UvFzJ8zWQ/"

        fun create(): SheetsApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Google Sheets returns redirects (302) on POST requests. OkHttp follows redirects by default,
            // but we configure it to be robust with reasonable timeouts.
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            return retrofit.create(SheetsApiService::class.java)
        }
    }
}

// A simple fallback class because Google Apps Script may return HTML redirection pages or empty body
class ResponseBodyWrapper
