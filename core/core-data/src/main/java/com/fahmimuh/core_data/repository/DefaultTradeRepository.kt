package com.fahmimuh.core_data.repository

import android.util.Log
import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_data.mappers.toDomainExchangeRate
import com.fahmimuh.core_data.mappers.toDomainExchangeRatesMap
import com.fahmimuh.core_data.network.service.FrankfurterApiService
import com.fahmimuh.core_domain.model.ExchangeRate
import com.fahmimuh.core_domain.repository.TradeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultTradeRepository @Inject constructor(
    private val apiService: FrankfurterApiService
) : TradeRepository {

    override suspend fun getExchangeRate(
        homeCurrency: String,
        targetCurrency: String
    ): Resource<ExchangeRate> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLatestRates(
                    fromCurrency = homeCurrency,
                    toCurrencies = targetCurrency
                )
                if (response.isSuccessful) {
                    val dto = response.body()
                    val domainModel = dto?.toDomainExchangeRate(targetCurrency)
                    if (domainModel != null) {
                        Resource.Success(domainModel)
                    } else {
                        Resource.Error("Failed to parse exchange rate data or rate for $targetCurrency not found.")
                    }
                } else {
                    Resource.Error("API Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                 Log.e("TradeRepository", "Exception fetching single exchange rate", e)
                Resource.Error("Network error or exception: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    override suspend fun getExchangeRatesForCountries(
        homeCurrency: String,
        countryCurrencies: List<String>
    ): Resource<Map<String, ExchangeRate>> {
        return withContext(Dispatchers.IO) {
            try {
                val toCurrenciesQueryParam = if (countryCurrencies.isEmpty()) null else countryCurrencies.joinToString(",")
                val response = apiService.getLatestRates(
                    fromCurrency = homeCurrency,
                    toCurrencies = toCurrenciesQueryParam
                )
                if (response.isSuccessful) {
                    val dto = response.body()
                    if (dto != null) {
                        Resource.Success(dto.toDomainExchangeRatesMap())
                    } else {
                        Resource.Error("Failed to parse exchange rates data.")
                    }
                } else {
                    Resource.Error("API Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                 Log.e("TradeRepository", "Exception fetching multiple exchange rates", e)
                Resource.Error("Network error or exception: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    override suspend fun getHistoricalExchangeRates(
        date: String,
        homeCurrency: String,
        countryCurrencies: List<String>
    ): Resource<Map<String, ExchangeRate>> {
        return withContext(Dispatchers.IO) {
            try {
                val toCurrenciesQueryParam = if (countryCurrencies.isEmpty()) null else countryCurrencies.joinToString(",")
                val response = apiService.getHistoricalRates(
                    date = date,
                    fromCurrency = homeCurrency,
                    toCurrencies = toCurrenciesQueryParam
                )
                if (response.isSuccessful) {
                    val dto = response.body()
                    if (dto != null) {
                        Resource.Success(dto.toDomainExchangeRatesMap())
                    } else {
                        Resource.Error("Failed to parse historical exchange rates data.")
                    }
                } else {
                    Resource.Error("API Error for historical rates: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                 Log.e("TradeRepository", "Exception fetching historical exchange rates", e)
                Resource.Error("Network error or exception for historical rates: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    override suspend fun getAvailableCurrencies(): Resource<Map<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAvailableCurrencies()
                if (response.isSuccessful) {
                    val currenciesMap = response.body()
                    if (currenciesMap != null) {
                        Resource.Success(currenciesMap)
                    } else {
                        Resource.Error("Failed to parse available currencies data.")
                    }
                } else {
                    Resource.Error("API Error fetching available currencies: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                 Log.e("TradeRepository", "Exception fetching available currencies", e)
                Resource.Error("Network error or exception fetching available currencies: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }
}