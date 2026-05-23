package com.vaastuverse.app.data.network

import com.vaastuverse.app.BuildConfig
import com.vaastuverse.app.data.ApiConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
  private val logging = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
  }

  private val activeRoleInterceptor = Interceptor { chain ->
    val request = chain.request()
    val role = ActiveRoleHolder.activeRole
    val next = if (!role.isNullOrBlank()) {
      request.newBuilder().header("X-Active-Role", role).build()
    } else {
      request
    }
    chain.proceed(next)
  }

  private val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .addInterceptor(activeRoleInterceptor)
    .addInterceptor(logging)
    .build()

  val api: VaastuVerseApi by lazy {
    Retrofit.Builder()
      .baseUrl("${ApiConfig.gatewayBaseUrl}/")
      .client(httpClient)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(VaastuVerseApi::class.java)
  }

  fun bearer(token: String) = "Bearer $token"
}
