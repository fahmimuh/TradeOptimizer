package com.fahmimuh.core_domain.repository

import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.model.ExchangeRate

interface TradeRepository {

    suspend fun getExchangeRate(
        homeCurrency: String,
        targetCurrency: String
    ): Resource<ExchangeRate>

    suspend fun getExchangeRatesForCountries(
        homeCurrency: String,
        countryCurrencies: List<String>
    ): Resource<Map<String, ExchangeRate>>

    suspend fun getHistoricalExchangeRates(
        date: String,
        homeCurrency: String,
        countryCurrencies: List<String>
    ): Resource<Map<String, ExchangeRate>>

    suspend fun getAvailableCurrencies(): Resource<Map<String, String>>
}