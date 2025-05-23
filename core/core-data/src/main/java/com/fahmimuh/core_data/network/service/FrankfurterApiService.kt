package com.fahmimuh.core_data.network.service

import com.fahmimuh.core_data.network.dto.ExchangeRateResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FrankfurterApiService {

    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") fromCurrency: String,
        @Query("to") toCurrencies: String? = null
    ): Response<ExchangeRateResponseDto>

    @GET("{date}")
    suspend fun getHistoricalRates(
        @Path("date") date: String,
        @Query("from") fromCurrency: String,
        @Query("to") toCurrencies: String? = null
    ): Response<ExchangeRateResponseDto>

    @GET("currencies")
    suspend fun getAvailableCurrencies(): Response<Map<String, String>>
}