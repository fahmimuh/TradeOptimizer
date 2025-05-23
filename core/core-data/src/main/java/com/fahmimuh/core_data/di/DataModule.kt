package com.fahmimuh.core_data.di

import com.fahmimuh.core_data.network.service.FrankfurterApiService
import com.fahmimuh.core_data.repository.DefaultTradeRepository
import com.fahmimuh.core_domain.repository.TradeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    private const val FRANKFURTER_BASE_URL = "https://api.frankfurter.app/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideFrankfurterApiService(okHttpClient: OkHttpClient): FrankfurterApiService {
        return Retrofit.Builder()
            .baseUrl(FRANKFURTER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FrankfurterApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTradeRepository(
        apiService: FrankfurterApiService
    ): TradeRepository {
        return DefaultTradeRepository(apiService)
    }
}